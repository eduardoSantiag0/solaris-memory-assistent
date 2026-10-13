package dev.solaris.memory.service;

import dev.solaris.memory.domain.Memory;

public record IdempotentResult(Memory memory, boolean newlyCreated) {
}
