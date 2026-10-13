package dev.solaris.memory.exception;

import dev.solaris.memory.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Entrada inválida", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição malformado", List.of());
    }

    @ExceptionHandler(InvalidCommandException.class)
    public ResponseEntity<ApiError> handleInvalidCommand(InvalidCommandException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MemoryNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(MemoryNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthFailed(AuthenticationFailedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        // The stack trace only ever reveals code location, never request content (RNF-05).
        log.error("Erro inesperado ao processar requisição", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        ApiError body = new ApiError(Instant.now(clock), status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(body);
    }
}
