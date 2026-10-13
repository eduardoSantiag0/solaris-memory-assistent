package dev.solaris.memory.embedding;

import com.pgvector.PGvector;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Raw JDBC on purpose, not a Spring Data method: pgvector's {@code <->}
 * operator and the {@link PGvector} binding aren't something JPQL/Criteria
 * can express, and {@code PGvector.registerTypes} has to run per physical
 * connection, which {@link ConnectionCallback} gives direct access to.
 */
@Repository
public class EmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateEmbedding(UUID id, float[] embedding) {
        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            PGvector.registerTypes(conn);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE memories SET embedding = ? WHERE id = ?")) {
                ps.setObject(1, new PGvector(embedding));
                ps.setObject(2, id);
                ps.executeUpdate();
            }
            return null;
        });
    }

    /** Active (not deleted, not expired), non-null-embedding candidates ordered by cosine distance to the query vector, closest first. */
    public List<UUID> findNearestActiveIds(Instant now, float[] queryEmbedding, int limit) {
        return jdbcTemplate.execute((ConnectionCallback<List<UUID>>) conn -> {
            PGvector.registerTypes(conn);
            String sql = """
                    select id from memories
                    where status <> 'DELETED'
                      and (expires_at is null or expires_at > ?)
                      and embedding is not null
                    order by embedding <-> ?
                    limit ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, java.sql.Timestamp.from(now));
                ps.setObject(2, new PGvector(queryEmbedding));
                ps.setInt(3, limit);
                List<UUID> ids = new ArrayList<>();
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add((UUID) rs.getObject("id"));
                    }
                }
                return ids;
            }
        });
    }
}
