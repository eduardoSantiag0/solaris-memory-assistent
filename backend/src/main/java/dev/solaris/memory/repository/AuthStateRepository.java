package dev.solaris.memory.repository;

import dev.solaris.memory.domain.AuthState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthStateRepository extends JpaRepository<AuthState, Short> {
}
