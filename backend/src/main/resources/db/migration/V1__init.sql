CREATE TABLE memories (
    id                 UUID PRIMARY KEY,
    content            TEXT NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    event_at           TIMESTAMPTZ NULL,
    expires_at         TIMESTAMPTZ NULL,
    status             VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    sensitivity        VARCHAR(10) NULL,
    client_request_id  VARCHAR(100) NULL,
    CONSTRAINT uq_memories_client_request_id UNIQUE (client_request_id)
);

CREATE INDEX idx_memories_status ON memories (status);
CREATE INDEX idx_memories_expires_at ON memories (expires_at);
CREATE INDEX idx_memories_event_at ON memories (event_at);

-- Single row holding the issued-token revocation watermark for the single
-- application user (PRE-05). Any JWT issued before min_issued_at is rejected.
CREATE TABLE auth_state (
    id            SMALLINT PRIMARY KEY DEFAULT 1,
    min_issued_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_auth_state_singleton CHECK (id = 1)
);

INSERT INTO auth_state (id, min_issued_at) VALUES (1, now());
