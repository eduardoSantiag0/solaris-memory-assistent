package dev.solaris.memory.dto;

import java.time.Instant;
import java.util.UUID;

public record SourceRef(UUID id, String snippet, Instant date) {
}
