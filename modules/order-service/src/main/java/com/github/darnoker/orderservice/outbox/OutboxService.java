package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.orderservice.order.event.OrderCreated;
import com.github.darnoker.orderservice.order.event.OrderEvent;
import com.github.darnoker.orderservice.outbox.model.OutboxEvent;
import com.github.darnoker.orderservice.outbox.model.OutboxEventStatus;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository repository;

    private final ObjectMapper objectMapper;

    private final Clock clock;

    public void save(UUID aggregateId, String orderTopic, EventType eventType, OrderEvent orderEvent) {
        Instant createdAt = Instant.now(clock);
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                aggregateId,
                eventType.name(),
                orderTopic,
                objectMapper.writeValueAsString(orderEvent),
                createdAt,
                OutboxEventStatus.PENDING,
                0,
                createdAt,
                null,
                null,
                null
        );
        repository.save(event);
    }
}
