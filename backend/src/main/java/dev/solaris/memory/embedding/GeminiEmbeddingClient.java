package dev.solaris.memory.embedding;

import dev.solaris.memory.config.AppEmbeddingsProperties;
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
 * Pedido do usuário em 15/10: embeddings "mais inteligentes" via Gemini em
 * vez do Ollama local — em particular, embeddings assimétricos
 * (document/query) pra aproximar termos relacionados mas diferentes
 * ("apartamento" ~ "residência") melhor do que o modelo local pequeno
 * conseguia. Mesma API key do GEMINI_API_KEY, modelo e dimensão
 * independentes do app.gemini.model usado pra reformular texto (aquele é
 * generateContent; este é embedContent).
 *
 * Timeout curto igual ao GeminiClient: isto só é chamado de forma síncrona
 * no caminho de busca quando o texto exato já falhou (RF-05) — nunca pode
 * travar a resposta do chat por mais que alguns segundos. No caminho de
 * escrita (embedAndStore) já roda em background, então o timeout aqui é
 * só uma rede de segurança extra.
 */
@Component
public class GeminiEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiEmbeddingClient.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String MODEL = "gemini-embedding-001";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);

    private final AppGeminiProperties geminiProperties;
    private final AppEmbeddingsProperties embeddingsProperties;
    private final RestClient restClient;

    public GeminiEmbeddingClient(AppGeminiProperties geminiProperties, AppEmbeddingsProperties embeddingsProperties) {
        this.geminiProperties = geminiProperties;
        this.embeddingsProperties = embeddingsProperties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(requestFactory).build();
    }

    @Override
    public float[] embed(String text, TaskType taskType) {
        String apiKey = geminiProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            String geminiTaskType = taskType == TaskType.QUERY ? "RETRIEVAL_QUERY" : "RETRIEVAL_DOCUMENT";
            EmbedResponse response = restClient.post()
                    .uri("/models/{model}:embedContent", MODEL)
                    .header("X-goog-api-key", apiKey)
                    .body(new EmbedRequest(new Content(List.of(new Part(text))), geminiTaskType, embeddingsProperties.getDimensions()))
                    .retrieve()
                    .body(EmbedResponse.class);

            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                return null;
            }
            List<Double> values = response.embedding().values();
            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = values.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            // Nunca loga o texto embedado — é o mesmo conteúdo criptografado em repouso (RNF-05).
            log.warn("Falha ao gerar embedding via Gemini ({}): {}", MODEL, e.getClass().getSimpleName());
            return null;
        }
    }

    private record EmbedRequest(Content content, String taskType, Integer outputDimensionality) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }

    private record EmbedResponse(Embedding embedding) {
    }

    private record Embedding(List<Double> values) {
    }
}
