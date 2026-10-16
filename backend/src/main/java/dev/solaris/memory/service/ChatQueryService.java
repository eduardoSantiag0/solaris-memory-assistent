package dev.solaris.memory.service;

import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.domain.Sensitivity;
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

    /**
     * Correções 14/10 item 1 (RF-04/CT-05/CT-13): greetings and plain
     * courtesy never consult memories, so "Oi" can never come back with an
     * unrelated ficha just because it was the vector index's "least bad"
     * match. Whole-message or lead-phrase matches only — a greeting that's
     * also carrying a real question ("Oi, onde deixei a chave?") must still
     * reach the search below.
     */
    private static final Set<String> CONVERSATIONAL_PHRASES = Set.of(
            "oi", "ola", "olá", "oii", "opa", "alo", "alô", "e ai", "eai", "e aí",
            "bom dia", "boa tarde", "boa noite",
            "obrigado", "obrigada", "obrigadao", "obrigadão", "valeu", "agradecido", "agradecida",
            "tudo bem", "tudo bom", "como vai", "blz", "beleza", "ok"
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

        if (isConversational(lower)) {
            return ChatQueryResponse.conversational();
        }

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
            List<String> contentWords = extractTerms(lowerContent);
            long score = terms.stream().filter(term -> matchesTerm(term, lowerContent, contentWords)).count();
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

    /**
     * Pedido do usuário em 15/10: um typo na pergunta ("apartamrnto" por
     * "apartamento") não pode fazer a busca por texto exato falhar e cair
     * silenciosamente num "não encontrei" — nem toda instância tem Ollama
     * ligado para a busca semântica cobrir esse caso. Tolerância por
     * distância de edição (Levenshtein) contra cada palavra do conteúdo,
     * além do `contains` exato já existente (que ainda cobre
     * prefixos/plurais como "apartamento" dentro de "apartamentos").
     */
    private boolean matchesTerm(String term, String lowerContent, List<String> contentWords) {
        if (lowerContent.contains(term)) {
            return true;
        }
        int threshold = typoThreshold(term.length());
        if (threshold == 0) {
            return false;
        }
        for (String word : contentWords) {
            if (Math.abs(word.length() - term.length()) <= threshold && levenshtein(term, word) <= threshold) {
                return true;
            }
        }
        return false;
    }

    private int typoThreshold(int termLength) {
        if (termLength <= 4) {
            return 0; // termos curtos têm poucos caracteres para "sobrar" margem sem gerar falso positivo
        }
        return termLength <= 7 ? 1 : 2;
    }

    private int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }

    private boolean isConversational(String lowerQuestion) {
        String cleaned = lowerQuestion.replaceAll("[¿?!.,;:]", "").trim();
        if (cleaned.isEmpty()) {
            return false;
        }
        if (CONVERSATIONAL_PHRASES.contains(cleaned)) {
            return true;
        }
        for (String phrase : CONVERSATIONAL_PHRASES) {
            if (cleaned.startsWith(phrase + " ")) {
                String rest = cleaned.substring(phrase.length()).trim();
                // Only filler after the greeting ("oi, tudo bem?") stays conversational;
                // anything longer is treated as a real question riding along with it.
                if (rest.isEmpty() || CONVERSATIONAL_PHRASES.contains(rest) || rest.length() <= 12) {
                    return true;
                }
            }
        }
        return false;
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

    /**
     * Correções 14/10 item 5 (RF-13/CT-06): two or more candidates with
     * different content are never silently resolved by picking the most
     * recent one — the app asks instead, listing every distinct ficha, and
     * never puts a sensitive one's literal content in that question (RNF-04
     * masking has to hold here too, not just in the normal answer path).
     */
    private ChatQueryResponse buildResponse(List<Memory> ranked, List<String> baseWarnings) {
        Memory best = ranked.get(0);

        List<Memory> distinct = new ArrayList<>();
        for (Memory m : ranked) {
            if (distinct.stream().noneMatch(d -> d.getContent().equals(m.getContent()))) {
                distinct.add(m);
            }
        }

        if (distinct.size() > 1) {
            return buildConflictResponse(distinct);
        }

        List<SourceRef> sources = List.of(
                new SourceRef(best.getId(), snippet(best.getContent()), best.getEventAt() != null ? best.getEventAt() : best.getCreatedAt(), best.getSensitivity()));
        return new ChatQueryResponse(best.getContent(), sources, true, baseWarnings, false);
    }

    private ChatQueryResponse buildConflictResponse(List<Memory> unordered) {
        List<Memory> distinct = new ArrayList<>(unordered);
        distinct.sort(Comparator.comparing(
                (Memory m) -> m.getEventAt() != null ? m.getEventAt() : m.getCreatedAt(),
                Comparator.reverseOrder()));

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < distinct.size(); i++) {
            Memory m = distinct.get(i);
            String label = m.getSensitivity() == Sensitivity.SENSITIVE
                    ? "um valor sensível"
                    : "\"" + m.getContent().trim().replaceAll("\\.$", "") + "\"";
            labels.add(i == 0 ? label + " (mais recente)" : label);
        }

        StringBuilder listed = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                listed.append(i == labels.size() - 1 ? " e " : ", ");
            }
            listed.append(labels.get(i));
        }

        String answer = "Encontrei fichas diferentes sobre isso: " + listed + ". Qual vale?";

        List<SourceRef> sources = distinct.stream()
                .map(m -> new SourceRef(m.getId(), snippet(m.getContent()), m.getEventAt() != null ? m.getEventAt() : m.getCreatedAt(), m.getSensitivity()))
                .toList();

        return new ChatQueryResponse(answer, sources, true, List.of(), true);
    }

    private String snippet(String content) {
        return content.length() <= 160 ? content : content.substring(0, 157) + "...";
    }
}
