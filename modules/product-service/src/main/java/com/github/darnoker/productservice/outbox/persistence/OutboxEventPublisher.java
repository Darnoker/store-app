package com.github.darnoker.productservice.outbox.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public void publish(UUID aggregateId, String eventType, String payload, Instant createdAt) {
        outboxEventRepository.save(new OutboxEventEntity(UUID.randomUUID(), aggregateId, eventType, payload, createdAt, false));
    }
}
