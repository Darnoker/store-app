ALTER TABLE outbox_events
    ADD COLUMN destination VARCHAR(128),
    ADD COLUMN status VARCHAR(16),
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN locked_by UUID,
    ADD COLUMN locked_until TIMESTAMP WITH TIME ZONE,
    ADD COLUMN last_error VARCHAR(2000);

UPDATE outbox_events
SET destination = 'inventory-topic',
    status = CASE WHEN published THEN 'PUBLISHED' ELSE 'PENDING' END;

ALTER TABLE outbox_events
    ALTER COLUMN destination SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')),
    DROP COLUMN published;

DROP INDEX idx_outbox_events_unpublished;

CREATE INDEX idx_outbox_events_ready
    ON outbox_events(next_attempt_at, created_at, id)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_expired_locks
    ON outbox_events(locked_until, created_at, id)
    WHERE status = 'PROCESSING';
