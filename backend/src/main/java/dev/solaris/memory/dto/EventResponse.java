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
        long memoryCount,
        /** When its earliest/latest memory happened (occurredAt) — null when the event has no memories yet. The event itself never has a time of its own. */
        Instant firstOccurredAt,
        Instant lastOccurredAt
) {
    public static EventResponse from(Event event, long memoryCount, Instant firstOccurredAt, Instant lastOccurredAt) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                memoryCount,
                firstOccurredAt,
                lastOccurredAt
        );
    }
}
