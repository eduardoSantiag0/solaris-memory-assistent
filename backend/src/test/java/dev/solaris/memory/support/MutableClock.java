package dev.solaris.memory.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/** Test-only clock so tests can fast-forward "now" without sleeping (e.g. to cross an expiresAt). */
public class MutableClock extends Clock {

    private final AtomicReference<Instant> instant;
    private final ZoneId zone;

    public MutableClock(Instant initial, ZoneId zone) {
        this.instant = new AtomicReference<>(initial);
        this.zone = zone;
    }

    public void advance(java.time.Duration duration) {
        instant.updateAndGet(current -> current.plus(duration));
    }

    public void setInstant(Instant newInstant) {
        instant.set(newInstant);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant.get(), zone);
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
