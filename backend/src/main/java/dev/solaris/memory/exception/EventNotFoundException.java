package dev.solaris.memory.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(UUID id) {
        super("Evento não encontrado: " + id);
    }
}
