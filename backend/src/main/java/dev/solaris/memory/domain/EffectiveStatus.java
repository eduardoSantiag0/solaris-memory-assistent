package dev.solaris.memory.domain;

/**
 * The status as seen by API clients — combines the persisted
 * {@link MemoryStatus} with a live check of {@code expiresAt} so a stale
 * cleanup job can never make an expired memory look current.
 */
public enum EffectiveStatus {
    ACTIVE,
    EXPIRED,
    DELETED
}
