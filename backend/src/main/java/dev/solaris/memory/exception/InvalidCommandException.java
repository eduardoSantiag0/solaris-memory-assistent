package dev.solaris.memory.exception;

/** Raised for malformed command-shaped input (RF-15): message should carry a usage example. */
public class InvalidCommandException extends RuntimeException {
    public InvalidCommandException(String message) {
        super(message);
    }
}
