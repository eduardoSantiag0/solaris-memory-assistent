package dev.solaris.memory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Single-row table holding the token revocation watermark for the one
 * application user (PRE-05). Any JWT issued before {@code minIssuedAt} is
 * rejected, which is what gives us "logout everywhere" without refresh-token
 * infrastructure (RNF-04).
 */
@Entity
@Table(name = "auth_state")
public class AuthState {

    @Id
    private short id = 1;

    @Column(name = "min_issued_at", nullable = false)
    private Instant minIssuedAt;

    protected AuthState() {
    }

    public AuthState(Instant minIssuedAt) {
        this.minIssuedAt = minIssuedAt;
    }

    public short getId() {
        return id;
    }

    public Instant getMinIssuedAt() {
        return minIssuedAt;
    }

    public void setMinIssuedAt(Instant minIssuedAt) {
        this.minIssuedAt = minIssuedAt;
    }
}
