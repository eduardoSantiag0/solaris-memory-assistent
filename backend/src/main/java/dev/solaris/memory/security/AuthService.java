package dev.solaris.memory.security;

import dev.solaris.memory.config.AppAuthProperties;
import dev.solaris.memory.domain.AuthState;
import dev.solaris.memory.dto.LoginResponse;
import dev.solaris.memory.exception.AuthenticationFailedException;
import dev.solaris.memory.repository.AuthStateRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuthService {

    private final AppAuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthStateRepository authStateRepository;
    private final Clock clock;

    public AuthService(AppAuthProperties authProperties, PasswordEncoder passwordEncoder,
                        JwtService jwtService, AuthStateRepository authStateRepository, Clock clock) {
        this.authProperties = authProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authStateRepository = authStateRepository;
        this.clock = clock;
    }

    public LoginResponse login(String username, String password) {
        boolean usernameMatches = authProperties.getUsername().equals(username);
        boolean passwordMatches = passwordEncoder.matches(password, authProperties.getPasswordHash());
        if (!usernameMatches || !passwordMatches) {
            throw new AuthenticationFailedException("Usuário ou senha inválidos");
        }
        String token = jwtService.issue(username);
        return new LoginResponse(token, jwtService.expirationFor(token));
    }

    @Transactional
    public void revokeAllSessions() {
        AuthState state = authStateRepository.findById((short) 1)
                .orElseGet(() -> new AuthState(Instant.now(clock)));
        state.setMinIssuedAt(Instant.now(clock));
        authStateRepository.save(state);
    }

    public boolean isIssuedAtAllowed(Instant issuedAt) {
        return authStateRepository.findById((short) 1)
                .map(AuthState::getMinIssuedAt)
                .map(minIssuedAt -> !issuedAt.isBefore(minIssuedAt))
                .orElse(true);
    }
}
