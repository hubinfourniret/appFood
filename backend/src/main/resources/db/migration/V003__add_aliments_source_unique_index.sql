-- Unique index on (source, source_id) for idempotent Ciqual import
-- Allows INSERT ON CONFLICT to detect existing aliments by source + source_id
CREATE UNIQUE INDEX idx_aliments_source_source_id ON aliments(source, source_id);
