package com.github.darnoker.common.async;

import java.time.Instant;
import java.util.UUID;

public record InboundMessage(UUID id,
                             UUID aggregateId,
                             String eventType,
                             Instant occurredAt,
                             int schemaVersion,
                             String payload) {
}
