package dev.solaris.memory.service;

import dev.solaris.memory.domain.Memory;
import dev.solaris.memory.dto.CreateMemoryRequest;
import dev.solaris.memory.dto.UpdateMemoryRequest;
import dev.solaris.memory.exception.MemoryNotFoundException;
import dev.solaris.memory.repository.MemoryRepository;
import dev.solaris.memory.repository.MemorySpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MemoryService {

    private final MemoryRepository repository;
    private final Clock clock;

    public MemoryService(MemoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public IdempotentResult create(CreateMemoryRequest request) {
        if (request.clientRequestId() != null && !request.clientRequestId().isBlank()) {
            var existing = repository.findByClientRequestId(request.clientRequestId());
            if (existing.isPresent()) {
                return new IdempotentResult(existing.get(), false);
            }
        }

        Instant now = Instant.now(clock);
        Instant expiresAt = request.ttlDays() != null
                ? now.plus(Duration.ofHours(24L * request.ttlDays()))
                : null;

        Memory memory = new Memory(
                UUID.randomUUID(),
                request.content(),
                now,
                request.eventAt(),
                expiresAt,
                blankToNull(request.clientRequestId())
        );
        return new IdempotentResult(repository.save(memory), true);
    }

    /** Returns any non-deleted memory, expired or not — management views need to see and clear expired items too. */
    public Memory getVisible(UUID id) {
        Memory memory = repository.findById(id).orElseThrow(() -> new MemoryNotFoundException(id));
        if (memory.getStatus() == dev.solaris.memory.domain.MemoryStatus.DELETED) {
            throw new MemoryNotFoundException(id);
        }
        return memory;
    }

    @Transactional
    public Memory update(UUID id, UpdateMemoryRequest request) {
        Memory memory = getVisible(id);
        Instant now = Instant.now(clock);

        Instant newEventAt = request.clearEventAt() ? null : (request.eventAt() != null ? request.eventAt() : memory.getEventAt());

        Instant newExpiresAt;
        if (request.makePermanent()) {
            newExpiresAt = null;
        } else if (request.ttlDays() != null) {
            newExpiresAt = now.plus(Duration.ofHours(24L * request.ttlDays()));
        } else {
            newExpiresAt = memory.getExpiresAt();
        }

        memory.applyUpdate(request.content(), newEventAt, newExpiresAt, now);
        return memory;
    }

    @Transactional
    public void delete(UUID id) {
        Memory memory = getVisible(id);
        memory.markDeleted();
    }

    public Page<Memory> list(StatusFilter status, TypeFilter type, String q, Pageable pageable) {
        Instant now = Instant.now(clock);
        Specification<Memory> spec = MemorySpecifications.notDeleted();

        spec = switch (status) {
            case ACTIVE -> spec.and(MemorySpecifications.active(now));
            case EXPIRED -> spec.and(MemorySpecifications.expired(now));
            case ALL -> spec;
        };

        spec = switch (type) {
            case PERMANENT -> spec.and(MemorySpecifications.permanent());
            case TEMPORARY -> spec.and(MemorySpecifications.temporary());
            case ALL -> spec;
        };

        if (q == null || q.isBlank()) {
            return repository.findAll(spec, pageable);
        }

        // content is encrypted at rest and cannot be filtered in SQL (see
        // MemorySpecifications): fetch the status/type-filtered candidates and
        // match decrypted text here. Fine at personal-memory scale.
        String needle = q.trim().toLowerCase();
        List<Memory> matches = repository.findAll(spec).stream()
                .filter(m -> m.getContent().toLowerCase().contains(needle))
                .toList();

        int start = Math.min((int) pageable.getOffset(), matches.size());
        int end = Math.min(start + pageable.getPageSize(), matches.size());
        return new PageImpl<>(matches.subList(start, end), pageable, matches.size());
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public enum StatusFilter { ACTIVE, EXPIRED, ALL }

    public enum TypeFilter { PERMANENT, TEMPORARY, ALL }
}
