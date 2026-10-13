package dev.solaris.memory.service;

import dev.solaris.memory.dto.ChatQueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Short-lived in-memory cache so a retried {@code POST /chat/query} with the
 * same clientRequestId (e.g. after a dropped connection) returns the same
 * answer instead of recomputing it.
 */
@Component
public class ChatIdempotencyCache {

    private final Clock clock;
    private final Duration ttl;
    private final Map<String, Entry> cache;

    public ChatIdempotencyCache(Clock clock,
                                 @Value("${app.idempotency.chat-cache-max-entries}") int maxEntries,
                                 @Value("${app.idempotency.chat-cache-ttl-seconds}") long ttlSeconds) {
        this.clock = clock;
        this.ttl = Duration.ofSeconds(ttlSeconds);
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public synchronized Optional<ChatQueryResponse> get(String clientRequestId) {
        if (clientRequestId == null || clientRequestId.isBlank()) {
            return Optional.empty();
        }
        Entry entry = cache.get(clientRequestId);
        if (entry == null || entry.expiresAt.isBefore(Instant.now(clock))) {
            return Optional.empty();
        }
        return Optional.of(entry.response);
    }

    public synchronized void put(String clientRequestId, ChatQueryResponse response) {
        if (clientRequestId == null || clientRequestId.isBlank()) {
            return;
        }
        cache.put(clientRequestId, new Entry(response, Instant.now(clock).plus(ttl)));
    }

    private record Entry(ChatQueryResponse response, Instant expiresAt) {
    }
}
