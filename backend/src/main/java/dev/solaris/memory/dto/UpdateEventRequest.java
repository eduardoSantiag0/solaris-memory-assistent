package dev.solaris.memory.dto;

/** Body for {@code PATCH /api/v1/events/{id}}. Every field is optional; an absent field keeps its current value. */
public record UpdateEventRequest(
        String title,
        String description,
        boolean clearDescription
) {
}
