package dev.solaris.memory.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple brute-force guard on {@code POST /auth/login} (RNF-04). Tracks
 * failed attempts per client IP within a sliding window; does not persist
 * across restarts, which is an acceptable MVP tradeoff for a single-user app.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final Clock clock;
    private final ConcurrentHashMap<String, Window> attemptsByKey = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String key) {
        Window window = attemptsByKey.get(key);
        if (window == null) {
            return false;
        }
        if (Instant.now(clock).isAfter(window.windowStart.plus(WINDOW))) {
            attemptsByKey.remove(key);
            return false;
        }
        return window.count.get() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        attemptsByKey.compute(key, (k, existing) -> {
            Instant now = Instant.now(clock);
            if (existing == null || now.isAfter(existing.windowStart.plus(WINDOW))) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(key);
    }

    private record Window(Instant windowStart, AtomicInteger count) {
    }
}
