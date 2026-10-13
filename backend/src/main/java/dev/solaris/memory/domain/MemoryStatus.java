package dev.solaris.memory.domain;

/**
 * Persisted lifecycle of a memory. EXPIRED is never written by the write
 * path — a memory becomes expired purely by its {@code expiresAt} being in
 * the past, so that read-time filtering keeps working even if a cleanup job
 * never runs (doc section 5.1).
 */
public enum MemoryStatus {
    ACTIVE,
    DELETED
}
