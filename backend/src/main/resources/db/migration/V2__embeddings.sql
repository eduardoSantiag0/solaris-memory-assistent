CREATE EXTENSION IF NOT EXISTS vector;

-- Nullable and populated best-effort after a memory is written (see
-- EmbeddingService): a failed/disabled embedding step must never block the
-- actual memory write (RF-10). Dimension matches nomic-embed-text (768).
--
-- Note: this vector is derived from the same plaintext the `content` column
-- encrypts (see EncryptedStringConverter), but is stored unencrypted —
-- pgvector's distance operators need the raw floats to work, so there is no
-- way to search it otherwise. It leaks less than plaintext but is not
-- nothing; accepted tradeoff for now, see README.
ALTER TABLE memories ADD COLUMN embedding vector(768);

-- HNSW over an initially empty table (no ivfflat list-training step needed).
CREATE INDEX idx_memories_embedding ON memories USING hnsw (embedding vector_cosine_ops);
