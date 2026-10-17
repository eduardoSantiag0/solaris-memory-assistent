package dev.solaris.memory.exception;

import java.util.UUID;

public class AttachmentNotFoundException extends RuntimeException {
    public AttachmentNotFoundException(UUID memoryId) {
        super("Esta memória não tem anexo: " + memoryId);
    }
}
