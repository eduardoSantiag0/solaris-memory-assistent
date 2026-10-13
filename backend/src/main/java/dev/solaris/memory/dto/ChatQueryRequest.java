package dev.solaris.memory.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatQueryRequest(
        @NotBlank(message = "question não pode ser vazio. Exemplo: {\"question\": \"Onde deixei a chave do carro?\"}")
        String question,

        String clientRequestId
) {
}
