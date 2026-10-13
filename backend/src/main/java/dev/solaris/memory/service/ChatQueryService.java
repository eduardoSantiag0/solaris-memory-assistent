package dev.solaris.memory.service;

import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.dto.ChatQueryRequest;
import dev.solaris.memory.dto.ChatQueryResponse;
import dev.solaris.memory.dto.SourceRef;
import dev.solaris.memory.embedding.EmbeddingService;
import dev.solaris.memory.repository.MemoryRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * M1 chat engine: PostgreSQL filters active/non-expired candidates by date
 * and status, and text matching happens here in the application layer, no
 * embeddings and no LLM synthesis (doc section 6 — those can be enabled
 * later without changing this contract). Content is encrypted at rest
 * (AesGcmEncryptor), so SQL can never filter by it — candidates are fetched
 * per the non-encrypted columns, decrypted on load, then matched by term.
 * The "answer" is always the literal content of a stored memory, never a
 * generated sentence, so there is nothing here that can invent facts
 * (RF-04, CT-05, CT-11).
 */
@Service
public class ChatQueryService {

    private static final Set<String> STOPWORDS = Set.of(
            "qual", "quais", "onde", "quando", "como", "quem", "o", "a", "os", "as",
            "de", "do", "da", "dos", "das", "e", "é", "esta", "está", "estava",
            "meu", "minha", "meus", "minhas", "um", "uma", "uns", "umas",
            "no", "na", "nos", "nas", "por", "para", "que", "foi", "ficou", "ficaram",
            "em", "eu", "se", "me", "te", "lhe", "ou", "já", "tá"
    );

    private static final int SEMANTIC_RESULT_LIMIT = 5;

    private final MemoryRepository repository;
    private final TemporalExpressionResolver temporalResolver;
    private final ChatIdempotencyCache idempotencyCache;
    private final EmbeddingService embeddingService;
    private final Clock clock;

    public ChatQueryService(MemoryRepository repository,
                             TemporalExpressionResolver temporalResolver,
                             ChatIdempotencyCache idempotencyCache,
                             EmbeddingService embeddingService,
                             Clock clock) {
        this.repository = repository;
        this.temporalResolver = temporalResolver;
        this.idempotencyCache = idempotencyCache;
        this.embeddingService = embeddingService;
        this.clock = clock;
    }

    public ChatQueryResponse answer(ChatQueryRequest request) {
        var cached = idempotencyCache.get(request.clientRequestId());
        if (cached.isPresent()) {
            return cached.get();
        }

        ChatQueryResponse response = compute(request.question());
        idempotencyCache.put(request.clientRequestId(), response);
        return response;
    }

    private ChatQueryResponse compute(String question) {
        Instant now = Instant.now(clock);
        String lower = question.toLowerCase();

        var temporalWindow = temporalResolver.resolve(lower);
        if (temporalWindow.isPresent()) {
            var window = temporalWindow.get();
            List<Memory> inWindow = repository.findActiveByEventWindow(now, window.from(), window.to());
            if (!inWindow.isEmpty()) {
                return buildResponse(inWindow, List.of());
            }
            // No memory has a confirmed eventAt in that window — fall back to
            // plain text matching, but say so instead of silently guessing (RF-12).
            List<Memory> fallback = searchByKeywords(now, lower);
            if (!fallback.isEmpty()) {
                return buildResponse(fallback, List.of(
                        "Nenhuma memória tem data de evento confirmada para \"" + window.label()
                                + "\"; mostrando correspondência por texto."));
            }
            return searchBySemanticsOrNotFound(now, question, List.of(
                    "Nenhuma memória tem data de evento confirmada para \"" + window.label() + "\"."));
        }

        List<Memory> matches = searchByKeywords(now, lower);
        if (!matches.isEmpty()) {
            return buildResponse(matches, List.of());
        }
        return searchBySemanticsOrNotFound(now, question, List.of());
    }

    /**
     * Only reached once keyword matching has already come up empty (RF-05:
     * vector similarity never overrides a text match, only fills in when
     * there isn't one). Always labeled as a similarity-based guess, never
     * presented the same way as an exact hit (RF-13).
     */
    private ChatQueryResponse searchBySemanticsOrNotFound(Instant now, String question, List<String> baseWarnings) {
        List<Memory> semanticMatches = embeddingService.findSemanticMatches(now, question, SEMANTIC_RESULT_LIMIT);
        if (semanticMatches.isEmpty()) {
            return ChatQueryResponse.notFound();
        }
        List<String> warnings = new ArrayList<>(baseWarnings);
        warnings.add("Nenhuma correspondência exata de texto; esta resposta é por similaridade semântica, confira a fonte.");
        return buildResponse(semanticMatches, warnings);
    }

    private List<Memory> searchByKeywords(Instant now, String lowerQuestion) {
        List<String> terms = extractTerms(lowerQuestion);
        if (terms.isEmpty()) {
            return List.of();
        }

        // One fetch of all active candidates, then score by decrypted content —
        // content is ciphertext in the DB, so matching can't happen in SQL.
        List<Scored> scored = new ArrayList<>();
        for (Memory candidate : repository.findActiveCandidates(now)) {
            String lowerContent = candidate.getContent().toLowerCase();
            long score = terms.stream().filter(lowerContent::contains).count();
            if (score > 0) {
                scored.add(new Scored(candidate, score));
            }
        }
        if (scored.isEmpty()) {
            return List.of();
        }

        scored.sort(Comparator
                .comparingLong(Scored::score).reversed()
                .thenComparing(s -> s.memory().getEventAt() != null ? s.memory().getEventAt() : s.memory().getCreatedAt(), Comparator.reverseOrder()));
        return scored.stream().map(Scored::memory).toList();
    }

    private record Scored(Memory memory, long score) {
    }

    private List<String> extractTerms(String lowerQuestion) {
        List<String> terms = new ArrayList<>();
        for (String raw : lowerQuestion.split("[^a-z0-9áàâãéêíóôõúüç]+")) {
            if (raw.length() >= 2 && !STOPWORDS.contains(raw)) {
                terms.add(raw);
            }
        }
        return terms;
    }

    private ChatQueryResponse buildResponse(List<Memory> ranked, List<String> baseWarnings) {
        Memory best = ranked.get(0);
        List<String> warnings = new ArrayList<>(baseWarnings);

        boolean conflicting = ranked.stream().skip(1)
                .anyMatch(m -> !m.getContent().equals(best.getContent()));
        if (conflicting) {
            warnings.add("Encontrei mais de uma memória relacionada com conteúdos diferentes; "
                    + "a resposta usa a mais recente, especifique uma data para desambiguar.");
        }

        List<SourceRef> sources = ranked.stream()
                .limit(3)
                .map(m -> new SourceRef(m.getId(), snippet(m.getContent()), m.getEventAt() != null ? m.getEventAt() : m.getCreatedAt()))
                .toList();

        return new ChatQueryResponse(best.getContent(), sources, true, warnings);
    }

    private String snippet(String content) {
        return content.length() <= 160 ? content : content.substring(0, 157) + "...";
    }
}
