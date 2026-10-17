package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Event;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt,
        long memoryCount
) {
    public static EventResponse from(Event event, long memoryCount) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                memoryCount
        );
    }
}
