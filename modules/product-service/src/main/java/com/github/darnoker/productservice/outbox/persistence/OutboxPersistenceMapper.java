package com.github.darnoker.productservice.outbox.persistence;

import com.github.darnoker.productservice.outbox.model.OutboxEvent;
import com.github.darnoker.productservice.outbox.model.OutboxEventStatus;

final class OutboxPersistenceMapper {
    private OutboxPersistenceMapper() {
    }

    static OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(entity.getId(), entity.getAggregateId(), entity.getEventType(), entity.getDestination(),
                entity.getPayload(), entity.getCreatedAt(), OutboxEventStatus.valueOf(entity.getStatus().name()),
                entity.getRetryCount(), entity.getNextAttemptAt(), entity.getLockedBy(), entity.getLockedUntil(), entity.getLastError());
    }

    static OutboxEventEntity toEntity(OutboxEvent event) {
        return new OutboxEventEntity(event.id(), event.aggregateId(), event.eventType(), event.destination(), event.payload(),
                event.createdAt(), OutboxEventEntityStatus.valueOf(event.status().name()), event.retryCount(),
                event.nextAttemptAt(), event.lockedBy(), event.lockedUntil(), event.lastError());
    }
}
