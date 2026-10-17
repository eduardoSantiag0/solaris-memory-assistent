package dev.solaris.memory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A manually-created, titled collection of existing memories (requirement:
 * "Agrupamento de memórias em eventos"). An Event never stores or copies
 * memory content — association lives entirely in {@link EventMemory}.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Event() {
        // JPA
    }

    public Event(UUID id, String title, String description, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void applyUpdate(String newTitle, String newDescription, boolean clearDescription, Instant now) {
        if (newTitle != null) {
            this.title = newTitle;
        }
        if (clearDescription) {
            this.description = null;
        } else if (newDescription != null) {
            this.description = newDescription;
        }
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
