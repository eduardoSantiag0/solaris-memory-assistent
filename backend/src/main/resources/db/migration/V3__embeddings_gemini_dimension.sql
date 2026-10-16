-- Switching the embeddings provider to Gemini (15/10) also changes the
-- vector dimension: 768 (truncated via outputDimensionality) measured
-- noticeably worse cosine-distance separation between related and
-- unrelated short Portuguese texts than 1536 in local testing — and
-- pgvector's HNSW index has a hard 2000-dimension limit, so the model's
-- native 3072 isn't an option here. 1536 is the practical middle ground.
--
-- Existing values are dropped outright: a stored vector from one
-- dimension can't be reinterpreted as another, and they're from a
-- different provider's embedding space anyway (not comparable even at
-- the same dimension). POST /api/v1/admin/reembed repopulates them.
DROP INDEX idx_memories_embedding;
ALTER TABLE memories DROP COLUMN embedding;
ALTER TABLE memories ADD COLUMN embedding vector(1536);
CREATE INDEX idx_memories_embedding ON memories USING hnsw (embedding vector_cosine_ops);
