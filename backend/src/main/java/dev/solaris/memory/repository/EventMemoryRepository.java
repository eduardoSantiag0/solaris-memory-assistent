package dev.solaris.memory.repository;

import dev.solaris.memory.domain.EventMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventMemoryRepository extends JpaRepository<EventMemory, UUID> {

    List<EventMemory> findByEventId(UUID eventId);

    List<EventMemory> findByMemoryId(UUID memoryId);

    Optional<EventMemory> findByEventIdAndMemoryId(UUID eventId, UUID memoryId);

    boolean existsByEventIdAndMemoryId(UUID eventId, UUID memoryId);

    long countByEventId(UUID eventId);

    void deleteByEventIdAndMemoryId(UUID eventId, UUID memoryId);
}
