package dev.solaris.memory.dto;

import java.util.List;

public record ChatQueryResponse(
        String answer,
        List<SourceRef> sources,
        boolean found,
        List<String> warnings,
        /** Correções 14/10 item 5: true when {@code answer} is already the full "qual vale?" sentence, so the client must render it as-is (never wrap it, never pick one of {@code sources} as "the" answer). */
        boolean conflict
) {
    public static ChatQueryResponse notFound() {
        return new ChatQueryResponse("Não encontrei essa informação.", List.of(), false, List.of(), false);
    }

    /** Correções 14/10 item 1 (RF-04/CT-05/CT-13): greetings/thanks never consult memories at all. */
    public static ChatQueryResponse conversational() {
        return new ChatQueryResponse(
                "Oi! Pergunte sobre algo que você registrou, ou toque em + para guardar uma memória nova.",
                List.of(), false, List.of(), false);
    }
}
