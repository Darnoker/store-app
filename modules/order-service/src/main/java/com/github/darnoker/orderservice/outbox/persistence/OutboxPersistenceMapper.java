package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;

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
                eventEntity.isPublished()
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
                event.published()
        );
    }
}
