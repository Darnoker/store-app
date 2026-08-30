package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;
import com.github.darnoker.orderservice.outbox.model.OutboxEventStatus;

final class OutboxPersistenceMapper {

    private OutboxPersistenceMapper() {

    }

    public static OutboxEvent toDomain(OutboxEventEntity eventEntity) {
        return new OutboxEvent(
                eventEntity.getId(),
                eventEntity.getAggregateId(),
                eventEntity.getEventType(),
                eventEntity.getDestination(),
                eventEntity.getPayload(),
                eventEntity.getCreatedAt(),
                fromEntity(eventEntity.getStatus()),
                eventEntity.getRetryCount(),
                eventEntity.getNextAttemptAt(),
                eventEntity.getLockedBy(),
                eventEntity.getLockedUntil(),
                eventEntity.getLastError()
        );
    }

    public static OutboxEventEntity toEntity(OutboxEvent event) {
        return new OutboxEventEntity(
                event.id(),
                event.aggregateId(),
                event.eventType(),
                event.destination(),
                event.payload(),
                event.createdAt(),
                fromDomain(event.status()),
                event.retryCount(),
                event.nextAttemptAt(),
                event.lockedBy(),
                event.lockedUntil(),
                event.lastError()
        );
    }

    private static OutboxEventEntityStatus fromDomain(OutboxEventStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> OutboxEventEntityStatus.PENDING;
            case PROCESSING -> OutboxEventEntityStatus.PROCESSING;
            case PUBLISHED -> OutboxEventEntityStatus.PUBLISHED;
            case FAILED -> OutboxEventEntityStatus.FAILED;
        };
    }

    private static OutboxEventStatus fromEntity(OutboxEventEntityStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> OutboxEventStatus.PENDING;
            case PROCESSING -> OutboxEventStatus.PROCESSING;
            case PUBLISHED -> OutboxEventStatus.PUBLISHED;
            case FAILED -> OutboxEventStatus.FAILED;
        };
    }
}
