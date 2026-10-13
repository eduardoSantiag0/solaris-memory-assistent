package dev.solaris.memory.web;

import dev.solaris.memory.dto.LoginRequest;
import dev.solaris.memory.dto.LoginResponse;
import dev.solaris.memory.exception.AuthenticationFailedException;
import dev.solaris.memory.security.AuthService;
import dev.solaris.memory.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String key = clientKey(httpRequest);
        if (rateLimiter.isBlocked(key)) {
            throw new AuthenticationFailedException("Muitas tentativas; aguarde alguns minutos antes de tentar novamente.");
        }
        try {
            LoginResponse response = authService.login(request.username(), request.password());
            rateLimiter.recordSuccess(key);
            return ResponseEntity.ok(response);
        } catch (AuthenticationFailedException ex) {
            rateLimiter.recordFailure(key);
            throw ex;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.revokeAllSessions();
        return ResponseEntity.noContent().build();
    }

    private String clientKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
