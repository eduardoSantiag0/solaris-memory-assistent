package dev.solaris.memory.exception;

import java.util.UUID;

public class MemoryNotFoundException extends RuntimeException {
    public MemoryNotFoundException(UUID id) {
        super("Memória não encontrada: " + id);
    }
}
