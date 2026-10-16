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
    private final OllamaEmbeddingClient ollamaClient;
    private final GeminiEmbeddingClient geminiClient;
    private final EmbeddingRepository embeddingRepository;
    private final MemoryRepository memoryRepository;

    public EmbeddingService(AppEmbeddingsProperties properties, OllamaEmbeddingClient ollamaClient,
                             GeminiEmbeddingClient geminiClient, EmbeddingRepository embeddingRepository,
                             MemoryRepository memoryRepository) {
        this.properties = properties;
        this.ollamaClient = ollamaClient;
        this.geminiClient = geminiClient;
        this.embeddingRepository = embeddingRepository;
        this.memoryRepository = memoryRepository;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /** app.embeddings.provider: "gemini" opts in explicitly; anything else (including unset) keeps the local Ollama default. */
    private EmbeddingClient activeClient() {
        return "gemini".equalsIgnoreCase(properties.getProvider()) ? geminiClient : ollamaClient;
    }

    /**
     * Paraphrase-tolerant fallback for when keyword matching finds nothing —
     * never used otherwise (RF-05: exact/text matches must never be
     * second-guessed by vector similarity). Empty whenever embeddings are
     * disabled, the provider is unreachable, or no memory has an embedding yet.
     */
    public List<Memory> findSemanticMatches(Instant now, String queryText, int limit) {
        if (!properties.isEnabled()) {
            return List.of();
        }
        float[] queryVector = activeClient().embed(queryText, EmbeddingClient.TaskType.QUERY);
        if (queryVector == null) {
            return List.of();
        }
        List<EmbeddingRepository.ScoredId> scored = embeddingRepository.findNearestActiveIds(now, queryVector, limit, properties.getMaxDistance());
        if (scored.isEmpty()) {
            return List.of();
        }
        // Achado 15/10: `maxDistance` sozinho deixava passar conteúdo
        // totalmente não relacionado como "candidato" (a distância de
        // cosseno do Gemini não separa bem em termos absolutos pra textos
        // curtos). Só quem fica a até `semanticMargin` do melhor resultado
        // conta como de verdade empatado/relacionado — o resto é
        // descartado mesmo estando dentro do teto de sanidade.
        double best = scored.get(0).distance();
        List<UUID> nearestIds = scored.stream()
                .filter(s -> s.distance() <= best + properties.getSemanticMargin())
                .map(EmbeddingRepository.ScoredId::id)
                .toList();
        Map<UUID, Memory> byId = new LinkedHashMap<>();
        memoryRepository.findAllById(nearestIds).forEach(m -> byId.put(m.getId(), m));
        return nearestIds.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
    }

    public void embedAndStore(UUID memoryId, String content) {
        if (!properties.isEnabled()) {
            return;
        }
        float[] vector = activeClient().embed(content, EmbeddingClient.TaskType.DOCUMENT);
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

    /**
     * Reprocessa o embedding de toda memória ativa com o provider atual —
     * necessário depois de trocar `app.embeddings.provider`, já que
     * embeddings de provedores diferentes não são comparáveis entre si
     * (espaços vetoriais diferentes). Síncrono de propósito: chamado uma
     * vez, manualmente, via AdminController — não é parte do caminho de
     * escrita/busca normal.
     */
    public int reembedAllActive(Instant now) {
        if (!properties.isEnabled()) {
            return 0;
        }
        List<Memory> memories = memoryRepository.findActiveCandidates(now);
        int processed = 0;
        for (Memory memory : memories) {
            embedAndStore(memory.getId(), memory.getContent());
            processed++;
        }
        return processed;
    }
}
