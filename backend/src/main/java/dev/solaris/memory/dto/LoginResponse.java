package dev.solaris.memory.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
