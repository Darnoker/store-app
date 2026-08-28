package com.github.darnoker.orderservice.outbox.model;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        String eventType,
        String destination,
        String payload,
        Instant createdAt,
        boolean published
) {
}
