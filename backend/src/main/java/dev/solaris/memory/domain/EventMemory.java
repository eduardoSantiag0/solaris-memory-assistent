package dev.solaris.memory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Join row associating one {@link Event} with one existing {@link Memory}. Never duplicates memory data. */
@Entity
@Table(name = "event_memories")
public class EventMemory {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "memory_id", nullable = false)
    private UUID memoryId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected EventMemory() {
        // JPA
    }

    public EventMemory(UUID id, UUID eventId, UUID memoryId, Instant addedAt) {
        this.id = id;
        this.eventId = eventId;
        this.memoryId = memoryId;
        this.addedAt = addedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getMemoryId() {
        return memoryId;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
