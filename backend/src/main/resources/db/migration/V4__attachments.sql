CREATE TABLE attachments (
    id            UUID PRIMARY KEY,
    memory_id     UUID NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
    filename      VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    description   TEXT NOT NULL,
    data          BYTEA NOT NULL,
    size_bytes    BIGINT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

-- One attachment per memory for now (the Nova Memória sheet has a single
-- paperclip) — lifting this to many is just dropping this index later.
CREATE UNIQUE INDEX uq_attachments_memory_id ON attachments (memory_id);
