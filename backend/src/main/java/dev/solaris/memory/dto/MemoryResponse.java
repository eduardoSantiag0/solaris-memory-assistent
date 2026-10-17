package dev.solaris.memory.dto;

import dev.solaris.memory.domain.EffectiveStatus;
import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.domain.Sensitivity;

import java.time.Instant;
import java.util.UUID;

public record MemoryResponse(
        UUID id,
        String content,
        Instant createdAt,
        Instant updatedAt,
        Instant eventAt,
        Instant expiresAt,
        EffectiveStatus status,
        Sensitivity sensitivity,
        boolean hasAttachment
) {
    public static MemoryResponse from(Memory memory, Instant now, boolean hasAttachment) {
        return new MemoryResponse(
                memory.getId(),
                memory.getContent(),
                memory.getCreatedAt(),
                memory.getUpdatedAt(),
                memory.getEventAt(),
                memory.getExpiresAt(),
                memory.effectiveStatus(now),
                memory.getSensitivity(),
                hasAttachment
        );
    }
}
