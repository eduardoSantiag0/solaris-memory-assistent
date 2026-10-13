package dev.solaris.memory.security;

import dev.solaris.memory.config.AppJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final Duration expiration;
    private final Clock clock;

    public JwtService(AppJwtProperties properties, Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(properties.getExpirationMinutes());
        this.clock = clock;
    }

    public String issue(String subject) {
        Instant now = Instant.now(clock);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey)
                .compact();
    }

    public Instant expirationFor(String token) {
        return parse(token).map(Claims::getExpiration).map(Date::toInstant).orElseThrow();
    }

    /** Empty when the token is malformed, expired, or signed with a different key. */
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(signingKey)
                    // Validate "exp" against our injected Clock, not jjwt's own
                    // real-time default — otherwise a test (or any future
                    // time-travel use) that moves this app's notion of "now"
                    // would see every token as expired or not, inconsistently.
                    .clock(() -> Date.from(Instant.now(clock)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
