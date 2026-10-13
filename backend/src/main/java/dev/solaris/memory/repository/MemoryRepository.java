package dev.solaris.memory.repository;

import dev.solaris.memory.domain.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID>, JpaSpecificationExecutor<Memory> {

    Optional<Memory> findByClientRequestId(String clientRequestId);

    /**
     * Active candidates for chat/query and text search (RF-04, RF-06).
     * {@code content} is encrypted at rest (AesGcmEncryptor), so it cannot be
     * filtered in SQL — callers must decrypt (automatic on entity load) and
     * match in the application layer instead.
     */
    @Query("""
            select m from Memory m
            where m.status <> dev.solaris.memory.domain.MemoryStatus.DELETED
              and (m.expiresAt is null or m.expiresAt > :now)
            order by coalesce(m.eventAt, m.createdAt) desc
            """)
    List<Memory> findActiveCandidates(@Param("now") Instant now);

    @Query("""
            select m from Memory m
            where m.status <> dev.solaris.memory.domain.MemoryStatus.DELETED
              and (m.expiresAt is null or m.expiresAt > :now)
              and m.eventAt between :from and :to
            order by m.eventAt desc
            """)
    List<Memory> findActiveByEventWindow(@Param("now") Instant now,
                                          @Param("from") Instant from,
                                          @Param("to") Instant to);
}
