package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Memories here are never copies — {@code memories} is just the current,
 * live {@link MemoryResponse} for each memory associated with this event,
 * ordered chronologically (occurredAt == {@code eventAt}, falling back to
 * {@code createdAt} — see EventService.getDetail).
 */
public record EventDetailResponse(
        UUID id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt,
        List<MemoryResponse> memories
) {
    public static EventDetailResponse from(Event event, List<MemoryResponse> memories) {
        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                memories
        );
    }
}
