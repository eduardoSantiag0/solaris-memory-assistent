package dev.solaris.memory.embedding;

import dev.solaris.memory.config.AppEmbeddingsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Talks to a local Ollama instance for embeddings only (PRE-03: local
 * models, no external commercial APIs). Never logs the text being embedded
 * — same content that's encrypted at rest (RNF-05).
 */
@Component
public class OllamaEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingClient.class);

    // Sem isto, o RestClient fica sem timeout (espera indefinida) — Ollama
    // roda com mem_limit apertado no compose (achado: se ele travar ou
    // ficar sem memória, isto pendurava pra sempre a única thread do worker
    // de embeddings, ou a resposta do chat no fallback semântico síncrono).
    // Mesmos valores do GeminiEmbeddingClient.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);

    private final AppEmbeddingsProperties properties;
    private final RestClient restClient;

    public OllamaEmbeddingClient(AppEmbeddingsProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(properties.getOllamaBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /** Empty when embeddings are disabled or Ollama is unreachable — callers must treat this as a soft failure (RF-10: never block the write). Ollama has no document/query distinction, so `taskType` is ignored. */
    @Override
    public float[] embed(String text, TaskType taskType) {
        try {
            EmbedResponse response = restClient.post()
                    .uri("/api/embed")
                    .body(new EmbedRequest(properties.getModel(), text))
                    .retrieve()
                    .body(EmbedResponse.class);

            if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
                throw new IllegalStateException("Resposta vazia do Ollama");
            }
            List<Double> vector = response.embeddings().get(0);
            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            log.warn("Falha ao gerar embedding via Ollama ({}): {}", properties.getOllamaBaseUrl(), e.getClass().getSimpleName());
            return null;
        }
    }

    private record EmbedRequest(String model, String input) {
    }

    private record EmbedResponse(String model, List<List<Double>> embeddings) {
    }
}
