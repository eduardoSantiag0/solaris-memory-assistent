package dev.solaris.memory.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class TestClockConfig {

    @Bean
    public MutableClock mutableClock() {
        return new MutableClock(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    @Primary
    public java.time.Clock testClock(MutableClock mutableClock) {
        return mutableClock;
    }
}
