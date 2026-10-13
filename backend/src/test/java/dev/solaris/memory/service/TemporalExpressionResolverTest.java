package dev.solaris.memory.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalExpressionResolverTest {

    // Wednesday, 2026-01-07 15:00 in America/Sao_Paulo (UTC-3, no DST at this date).
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-07T18:00:00Z"), ZoneId.of("UTC"));
    private final TemporalExpressionResolver resolver =
            new TemporalExpressionResolver(fixedClock, "America/Sao_Paulo");

    @Test
    void ontemResolvesToYesterdayLocalDay() {
        var window = resolver.resolve("onde deixei a chave ontem?").orElseThrow();

        assertThat(window.from()).isEqualTo(Instant.parse("2026-01-06T03:00:00Z"));
        assertThat(window.to()).isEqualTo(Instant.parse("2026-01-07T03:00:00Z"));
    }

    @Test
    void hojeResolvesToTodayLocalDay() {
        var window = resolver.resolve("o que anotei hoje?").orElseThrow();

        assertThat(window.from()).isEqualTo(Instant.parse("2026-01-07T03:00:00Z"));
        assertThat(window.to()).isEqualTo(Instant.parse("2026-01-08T03:00:00Z"));
    }

    @Test
    void semanaPassadaResolvesToPreviousMondayToMonday() {
        var window = resolver.resolve("o que aconteceu na semana passada?").orElseThrow();

        assertThat(window.from()).isEqualTo(Instant.parse("2025-12-29T03:00:00Z"));
        assertThat(window.to()).isEqualTo(Instant.parse("2026-01-05T03:00:00Z"));
    }

    @Test
    void noMatchReturnsEmpty() {
        assertThat(resolver.resolve("onde deixei minhas chaves?")).isEmpty();
        assertThat(resolver.resolve("qual é meu rg?")).isEmpty();
    }
}
