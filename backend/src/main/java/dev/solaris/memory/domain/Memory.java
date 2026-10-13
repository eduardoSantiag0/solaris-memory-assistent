package dev.solaris.memory.domain;

import dev.solaris.memory.domain.converter.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memories")
public class Memory {

    @Id
    private UUID id;

    // Stored as ciphertext (AES-256-GCM) — see EncryptedStringConverter. Even an
    // account with direct psql access to this shared server only sees Base64 noise.
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "event_at")
    private Instant eventAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemoryStatus status = MemoryStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Sensitivity sensitivity;

    @Column(name = "client_request_id", length = 100)
    private String clientRequestId;

    protected Memory() {
        // JPA
    }

    public Memory(UUID id, String content, Instant createdAt, Instant eventAt,
                  Instant expiresAt, String clientRequestId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.eventAt = eventAt;
        this.expiresAt = expiresAt;
        this.status = MemoryStatus.ACTIVE;
        this.clientRequestId = clientRequestId;
    }

    public boolean isExpiredAt(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public EffectiveStatus effectiveStatus(Instant now) {
        if (status == MemoryStatus.DELETED) {
            return EffectiveStatus.DELETED;
        }
        return isExpiredAt(now) ? EffectiveStatus.EXPIRED : EffectiveStatus.ACTIVE;
    }

    public void applyUpdate(String newContent, Instant newEventAt, Instant newExpiresAt, Instant now) {
        if (newContent != null) {
            this.content = newContent;
        }
        this.eventAt = newEventAt;
        this.expiresAt = newExpiresAt;
        this.updatedAt = now;
    }

    public void markDeleted() {
        this.status = MemoryStatus.DELETED;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getEventAt() {
        return eventAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public MemoryStatus getStatus() {
        return status;
    }

    public Sensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }
}
