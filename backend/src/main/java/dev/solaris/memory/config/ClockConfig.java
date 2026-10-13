package dev.solaris.memory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /** Injectable system clock so services and tests can control "now" instead of calling Instant.now() directly. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
