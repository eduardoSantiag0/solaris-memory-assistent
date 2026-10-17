CREATE TABLE events (
    id          UUID PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

-- Pure association table: an event never modifies or duplicates a memory,
-- it only points at existing rows. A memory may belong to many events, and
-- an event may hold many memories.
CREATE TABLE event_memories (
    id         UUID PRIMARY KEY,
    event_id   UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    memory_id  UUID NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
    added_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_event_memories UNIQUE (event_id, memory_id)
);

CREATE INDEX idx_event_memories_memory_id ON event_memories (memory_id);
CREATE INDEX idx_event_memories_event_id ON event_memories (event_id);
