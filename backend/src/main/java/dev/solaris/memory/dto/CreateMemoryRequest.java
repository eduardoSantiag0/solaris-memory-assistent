package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Sensitivity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * Body for {@code POST /api/v1/memories}. {@code ttlDays} absent or null
 * means a permanent memory (RF-02); present means temporary (RF-03), and
 * must be a positive integer number of 24h days.
 */
public record CreateMemoryRequest(
        @NotBlank(message = "content não pode ser vazio. Exemplo: {\"content\": \"Minha chave fica na gaveta azul.\"}")
        String content,

        @Positive(message = "ttlDays deve ser um inteiro positivo de dias. Exemplo: {\"content\": \"...\", \"ttlDays\": 2}")
        Integer ttlDays,

        Instant eventAt,

        Sensitivity sensitivity,

        String clientRequestId
) {
}
