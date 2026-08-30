package com.github.darnoker.productservice.outbox.persistence;

import com.github.darnoker.productservice.outbox.model.OutboxEvent;
import com.github.darnoker.productservice.outbox.model.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private static final String INVENTORY_TOPIC = "inventory-topic";
    private final OutboxEventRepository repository;

    public void publish(UUID aggregateId, String eventType, String payload, Instant createdAt) {
        repository.save(new OutboxEvent(UUID.randomUUID(), aggregateId, eventType, INVENTORY_TOPIC, payload, createdAt,
                OutboxEventStatus.PENDING, 0, createdAt, null, null, null));
    }
}
