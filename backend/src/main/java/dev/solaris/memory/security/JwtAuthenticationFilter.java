package dev.solaris.memory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Stateless bearer-token check. Deliberately never logs the token itself or
 * the raw Authorization header (RNF-05).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            jwtService.parse(token).ifPresent(claims -> {
                Instant issuedAt = claims.getIssuedAt().toInstant();
                if (authService.isIssuedAtAllowed(issuedAt)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        }
        filterChain.doFilter(request, response);
    }
}
