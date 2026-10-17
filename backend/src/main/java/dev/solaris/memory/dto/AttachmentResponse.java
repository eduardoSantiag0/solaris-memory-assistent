package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Attachment;

import java.time.Instant;
import java.util.UUID;

/** Metadata only — raw bytes are served separately via {@code GET /api/v1/memories/{id}/attachment/file}. */
public record AttachmentResponse(
        UUID id,
        UUID memoryId,
        String filename,
        String contentType,
        String description,
        long sizeBytes,
        Instant createdAt
) {
    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getMemoryId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getDescription(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt()
        );
    }
}
