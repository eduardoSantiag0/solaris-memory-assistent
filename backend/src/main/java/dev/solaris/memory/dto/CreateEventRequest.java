package dev.solaris.memory.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank(message = "title não pode ser vazio. Exemplo: {\"title\": \"Viagem a Floripa\"}")
        String title,

        String description
) {
}
