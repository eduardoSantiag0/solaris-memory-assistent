package dev.solaris.memory.dto;

import java.util.List;

public record ChatQueryResponse(
        String answer,
        List<SourceRef> sources,
        boolean found,
        List<String> warnings
) {
    public static ChatQueryResponse notFound() {
        return new ChatQueryResponse("Não encontrei essa informação.", List.of(), false, List.of());
    }
}
