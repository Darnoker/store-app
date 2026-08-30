package com.github.darnoker.common.async;

import java.time.Instant;
import java.util.UUID;

/**
 * Versioned wire contract for events published between Storeapp services.
 */
public record EventEnvelope<T>(
        UUID id,
        UUID aggregateId,
        String eventType,
        Instant occurredAt,
        int schemaVersion,
        T payload
) {
}
