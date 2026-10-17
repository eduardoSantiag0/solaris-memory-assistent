package dev.solaris.memory.repository;

import dev.solaris.memory.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findAllByOrderByCreatedAtDesc();
}
