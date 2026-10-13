package dev.solaris.memory.repository;

import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.domain.MemoryStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/** Dynamic filters for {@code GET /api/v1/memories} (status/type/text query). */
public final class MemorySpecifications {

    private MemorySpecifications() {
    }

    public static Specification<Memory> notDeleted() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), MemoryStatus.DELETED);
    }

    public static Specification<Memory> active(Instant now) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("expiresAt")),
                cb.greaterThan(root.get("expiresAt"), now)
        );
    }

    public static Specification<Memory> expired(Instant now) {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("expiresAt")),
                cb.lessThanOrEqualTo(root.get("expiresAt"), now)
        );
    }

    public static Specification<Memory> temporary() {
        return (root, query, cb) -> cb.isNotNull(root.get("expiresAt"));
    }

    public static Specification<Memory> permanent() {
        return (root, query, cb) -> cb.isNull(root.get("expiresAt"));
    }

    // No contentContains(): content is encrypted at rest (AesGcmEncryptor), so it
    // cannot be matched in SQL. Text search over content is done in MemoryService
    // and ChatQueryService after the rows are loaded and transparently decrypted.
}
