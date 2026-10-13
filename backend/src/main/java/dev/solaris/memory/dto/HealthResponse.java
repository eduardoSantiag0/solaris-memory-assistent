package dev.solaris.memory.dto;

import java.util.Map;

/** Deliberately free of any sensitive content (RF per section 8: health has none). */
public record HealthResponse(String status, Map<String, String> services) {
}
