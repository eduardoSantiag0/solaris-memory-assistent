package dev.solaris.memory.llm;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin, task-specific wrappers around GeminiClient so ChatQueryService never
 * builds prompts by hand. Both methods are pure "soft enhancements": every
 * caller must already have a correct, deterministic answer in hand before
 * calling {@link #rephrase}, and a correct (possibly empty) result before
 * calling {@link #suggestSynonyms} — Gemini only ever reorders/renames
 * things that are already decided, never decides what the facts are
 * (RF-04 stays enforced entirely outside this class).
 */
@Service
public class GeminiPhrasingService {

    private final GeminiClient client;

    public GeminiPhrasingService(GeminiClient client) {
        this.client = client;
    }

    /**
     * Rephrases an already-retrieved literal fact into a natural reply to
     * the user's question — same content, friendlier wording. Null (soft
     * failure) means the caller must use the literal fact as-is; never
     * called with sensitive content (the caller enforces that).
     */
    public String rephrase(String question, String literalFact) {
        String prompt = """
                Reescreva o FATO abaixo como resposta à PERGUNTA. Regras obrigatórias, sem \
                exceção:
                1. Devolva EXATAMENTE UMA frase. Nunca ofereça opções, alternativas ou uma \
                   lista — nem "aqui estão algumas opções".
                2. Sem markdown: nada de "*", "-", numeração ou quebra de linha.
                3. Não adicione, remova nem troque nenhuma informação do fato.
                4. Não mencione quando/onde foi registrado (isso é adicionado depois, à parte).
                5. Português, sem aspas, sem comentário antes ou depois da frase.

                PERGUNTA: %s
                FATO (use exatamente este conteúdo, nada mais): %s

                Responda só com a frase final, e nada além dela.
                """.formatted(question, literalFact);
        String result = client.generate(prompt);
        return isValidSingleSentence(result) ? result : null;
    }

    /**
     * Modelos pequenos/gratuitos nem sempre obedecem o prompt — já vimos o
     * Gemini devolver uma lista de opções com marcadores em vez de uma
     * frase só. Mais seguro descartar e cair no texto literal do que
     * mostrar markdown quebrado ou múltiplas opções pro usuário.
     */
    private boolean isValidSingleSentence(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (text.contains("\n") || text.contains("*") || text.contains("- ")) {
            return false;
        }
        return text.length() <= 500;
    }

    /**
     * Suggests extra Portuguese search keywords (synonyms, singular/plural,
     * related words) for the question, to retry the deterministic keyword
     * search before falling back to semantic (embeddings) search. Empty
     * list (soft failure) means the caller proceeds straight to the
     * existing fallback, unchanged.
     */
    public List<String> suggestSynonyms(String question) {
        String prompt = """
                Extraia de 2 a 6 palavras-chave em português (sinônimos, singular/plural, \
                termos relacionados) que ajudariam a buscar por texto a resposta para esta \
                pergunta num banco de pequenas memórias pessoais. Responda só com as \
                palavras separadas por vírgula, tudo minúsculo, sem numeração, sem frase.

                Pergunta: %s
                """.formatted(question);
        String raw = client.generate(prompt);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> terms = new ArrayList<>();
        for (String piece : raw.split(",")) {
            String term = piece.trim().toLowerCase();
            if (!term.isEmpty() && term.length() <= 40) {
                terms.add(term);
            }
        }
        return terms;
    }
}
