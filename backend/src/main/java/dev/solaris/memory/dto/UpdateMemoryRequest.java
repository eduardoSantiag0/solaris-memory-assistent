package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Sensitivity;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * Body for {@code PATCH /api/v1/memories/{id}}. Every field is optional;
 * an absent field keeps its current value.
 * <p>
 * {@code ttlDays}, when present, resets the expiration to
 * {@code now + ttlDays * 24h}. To turn a temporary memory into a permanent
 * one, set {@code makePermanent: true} instead (it takes precedence over
 * {@code ttlDays}).
 */
public record UpdateMemoryRequest(
        String content,

        @Positive(message = "ttlDays deve ser um inteiro positivo de dias. Exemplo: {\"ttlDays\": 3}")
        Integer ttlDays,

        boolean makePermanent,

        Instant eventAt,

        boolean clearEventAt,

        Sensitivity sensitivity
) {
}
