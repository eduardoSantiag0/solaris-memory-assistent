package dev.solaris.memory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One image/PDF + its description, attached to a single memory. The VM this
 * runs on is no longer shared with other accounts (see AesGcmEncryptor's
 * javadoc for why {@code memories.content} is encrypted) — that constraint
 * doesn't apply here, so this data is stored as plain bytes/text, unlike
 * {@code Memory.content}.
 */
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    private UUID id;

    @Column(name = "memory_id", nullable = false)
    private UUID memoryId;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    // No @Lob here on purpose — with Postgres, @Lob on byte[] maps to the
    // `oid` large-object type, not `bytea`; the V4 migration's column is
    // bytea, and plain @Column already maps byte[] to that correctly.
    @Column(nullable = false)
    private byte[] data;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Attachment() {
        // JPA
    }

    public Attachment(UUID id, UUID memoryId, String filename, String contentType,
                       String description, byte[] data, Instant createdAt) {
        this.id = id;
        this.memoryId = memoryId;
        this.filename = filename;
        this.contentType = contentType;
        this.description = description;
        this.data = data;
        this.sizeBytes = data.length;
        this.createdAt = createdAt;
    }

    /** Replaces this memory's one attachment in place — see AttachmentService.createFor for why this isn't a delete-then-insert. */
    public void replace(String filename, String contentType, String description, byte[] data, Instant replacedAt) {
        this.filename = filename;
        this.contentType = contentType;
        this.description = description;
        this.data = data;
        this.sizeBytes = data.length;
        this.createdAt = replacedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemoryId() {
        return memoryId;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getData() {
        return data;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
