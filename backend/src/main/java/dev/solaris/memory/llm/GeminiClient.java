package dev.solaris.memory.llm;

import dev.solaris.memory.config.AppGeminiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Talks to the Gemini API (generateContent) only for rephrasing/suggestion
 * tasks that never decide a fact by themselves — see ChatQueryService for
 * how callers are required to fall back to the literal/deterministic path
 * on any null here (same resilience contract as OllamaEmbeddingClient).
 * Never called with SENSITIVE memory content (callers must guard that).
 *
 * Two things that bit us in 15/10 testing, both fixed here: (1) the
 * "-latest" flash alias can resolve to a model that spends tokens
 * "thinking" before answering even trivial prompts — fine for chat UIs
 * with a spinner, bad for a backend call the user is waiting on
 * synchronously, so thinking is explicitly disabled; (2) RestClient has no
 * timeout by default, so a slow/hanging call would block the whole chat
 * request indefinitely — now capped, same spirit as RF-10 for embeddings.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(12);

    private final AppGeminiProperties properties;
    private final RestClient restClient;

    public GeminiClient(AppGeminiProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(requestFactory).build();
    }

    /** Empty when Gemini is disabled, unconfigured, too slow, or the call otherwise fails — callers must treat this as a soft failure and fall back. */
    public String generate(String prompt) {
        if (!properties.isEnabled() || properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return null;
        }
        try {
            GenerateResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", properties.getModel())
                    .header("X-goog-api-key", properties.getApiKey())
                    .body(new GenerateRequest(
                            List.of(new Content(List.of(new Part(prompt)))),
                            new GenerationConfig(new ThinkingConfig(0))))
                    .retrieve()
                    .body(GenerateResponse.class);

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                return null;
            }
            Content content = response.candidates().get(0).content();
            if (content == null || content.parts() == null || content.parts().isEmpty()) {
                return null;
            }
            String text = content.parts().get(0).text();
            return text == null || text.isBlank() ? null : text.trim();
        } catch (Exception e) {
            // Nunca loga o prompt/resposta aqui — pode conter o texto da memória (RNF-05).
            log.warn("Falha ao chamar Gemini ({}): {}", properties.getModel(), e.getClass().getSimpleName());
            return null;
        }
    }

    private record GenerateRequest(List<Content> contents, GenerationConfig generationConfig) {
    }

    private record GenerationConfig(ThinkingConfig thinkingConfig) {
    }

    private record ThinkingConfig(int thinkingBudget) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }

    private record GenerateResponse(List<Candidate> candidates) {
    }

    private record Candidate(Content content) {
    }
}
