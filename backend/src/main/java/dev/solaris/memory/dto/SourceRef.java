package dev.solaris.memory.dto;

import dev.solaris.memory.domain.Sensitivity;

import java.time.Instant;
import java.util.UUID;

public record SourceRef(UUID id, String snippet, Instant date, Sensitivity sensitivity) {
}
