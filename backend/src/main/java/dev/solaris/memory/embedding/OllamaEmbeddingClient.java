package dev.solaris.memory.embedding;

import dev.solaris.memory.config.AppEmbeddingsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Talks to a local Ollama instance for embeddings only (PRE-03: local
 * models, no external commercial APIs). Never logs the text being embedded
 * — same content that's encrypted at rest (RNF-05).
 */
@Component
public class OllamaEmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingClient.class);

    private final AppEmbeddingsProperties properties;
    private final RestClient restClient;

    public OllamaEmbeddingClient(AppEmbeddingsProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.getOllamaBaseUrl()).build();
    }

    /** Empty when embeddings are disabled or Ollama is unreachable — callers must treat this as a soft failure (RF-10: never block the write). */
    public float[] embed(String text) {
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
