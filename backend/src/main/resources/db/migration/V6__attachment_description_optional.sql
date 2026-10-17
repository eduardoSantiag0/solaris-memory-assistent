-- The "descreva o anexo" field is no longer required (RF revisto 17/10) —
-- the memory's own content is still the one mandatory field.
ALTER TABLE attachments ALTER COLUMN description DROP NOT NULL;
