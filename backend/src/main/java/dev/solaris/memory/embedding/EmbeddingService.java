package dev.solaris.memory.embedding;

import dev.solaris.memory.config.AppEmbeddingsProperties;
import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.repository.MemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Best-effort enrichment, always called after a memory write already
 * succeeded — never part of that transaction, never able to fail the
 * write itself (RF-10, and section 6: the model is a helper, never a
 * dependency for the write path to work).
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final AppEmbeddingsProperties properties;
    private final OllamaEmbeddingClient embeddingClient;
    private final EmbeddingRepository embeddingRepository;
    private final MemoryRepository memoryRepository;

    public EmbeddingService(AppEmbeddingsProperties properties, OllamaEmbeddingClient embeddingClient,
                             EmbeddingRepository embeddingRepository, MemoryRepository memoryRepository) {
        this.properties = properties;
        this.embeddingClient = embeddingClient;
        this.embeddingRepository = embeddingRepository;
        this.memoryRepository = memoryRepository;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Paraphrase-tolerant fallback for when keyword matching finds nothing —
     * never used otherwise (RF-05: exact/text matches must never be
     * second-guessed by vector similarity). Empty whenever embeddings are
     * disabled, Ollama is unreachable, or no memory has an embedding yet.
     */
    public List<Memory> findSemanticMatches(Instant now, String queryText, int limit) {
        if (!properties.isEnabled()) {
            return List.of();
        }
        float[] queryVector = embeddingClient.embed(queryText);
        if (queryVector == null) {
            return List.of();
        }
        List<UUID> nearestIds = embeddingRepository.findNearestActiveIds(now, queryVector, limit);
        if (nearestIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, Memory> byId = new LinkedHashMap<>();
        memoryRepository.findAllById(nearestIds).forEach(m -> byId.put(m.getId(), m));
        return nearestIds.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
    }

    public void embedAndStore(UUID memoryId, String content) {
        if (!properties.isEnabled()) {
            return;
        }
        float[] vector = embeddingClient.embed(content);
        if (vector == null) {
            return;
        }
        if (vector.length != properties.getDimensions()) {
            log.warn("Embedding com dimensão inesperada ({} != {}); ignorando", vector.length, properties.getDimensions());
            return;
        }
        try {
            embeddingRepository.updateEmbedding(memoryId, vector);
        } catch (Exception e) {
            log.warn("Falha ao salvar embedding para memória {}: {}", memoryId, e.getClass().getSimpleName());
        }
    }
}
