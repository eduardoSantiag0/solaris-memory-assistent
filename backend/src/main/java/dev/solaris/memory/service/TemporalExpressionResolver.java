package dev.solaris.memory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves a small set of Portuguese relative-date expressions (RF-12) into
 * a concrete {@code eventAt} window in the configured local timezone,
 * converted to UTC instants for querying (PRE-04).
 */
@Component
public class TemporalExpressionResolver {

    private final Clock clock;
    private final ZoneId zone;

    public TemporalExpressionResolver(Clock clock, @Value("${app.timezone}") String timezone) {
        this.clock = clock;
        this.zone = ZoneId.of(timezone);
    }

    public Optional<TemporalWindow> resolve(String questionLowercase) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zone);
        ZonedDateTime startOfToday = now.toLocalDate().atStartOfDay(zone);

        if (questionLowercase.contains("ontem")) {
            ZonedDateTime from = startOfToday.minusDays(1);
            return Optional.of(new TemporalWindow(from.toInstant(), startOfToday.toInstant(), "ontem"));
        }
        if (questionLowercase.contains("hoje")) {
            return Optional.of(new TemporalWindow(startOfToday.toInstant(), startOfToday.plusDays(1).toInstant(), "hoje"));
        }
        if (questionLowercase.contains("semana passada")) {
            ZonedDateTime startOfThisWeek = startOfToday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            ZonedDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);
            return Optional.of(new TemporalWindow(startOfLastWeek.toInstant(), startOfThisWeek.toInstant(), "semana passada"));
        }
        return Optional.empty();
    }
}
