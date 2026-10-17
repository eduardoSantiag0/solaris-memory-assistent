package dev.solaris.memory.repository;

import dev.solaris.memory.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    Optional<Attachment> findByMemoryId(UUID memoryId);

    boolean existsByMemoryId(UUID memoryId);

    void deleteByMemoryId(UUID memoryId);

    List<Attachment> findByMemoryIdIn(List<UUID> memoryIds);
}
