package com.github.darnoker.productservice.outbox;

import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.productservice.outbox.model.OutboxEvent;
import com.github.darnoker.productservice.outbox.model.OutboxEventStatus;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductOutboxRelayAdapterTest {

    @Test
    void mapsClaimedOutboxEventsToOutboundMessages() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        UUID instanceId = UUID.randomUUID();
        Instant leaseUntil = Instant.parse("2026-08-30T12:01:00Z");
        OutboxEvent event = event();
        when(repository.claimBatch(eq(10), eq(instanceId), eq(leaseUntil))).thenReturn(List.of(event));

        List<OutboundMessage> messages = new ProductOutboxRelayAdapter(repository)
                .claimBatch(10, instanceId, leaseUntil);

        assertEquals(List.of(new OutboundMessage(
                event.id(), event.destination(), event.aggregateId(), event.eventType(), event.createdAt(), event.payload()
        )), messages);
    }

    private OutboxEvent event() {
        Instant occurredAt = Instant.parse("2026-08-30T12:00:00Z");
        return new OutboxEvent(UUID.randomUUID(), UUID.randomUUID(), "STOCK_RESERVED", "inventory-topic", "{}", occurredAt,
                OutboxEventStatus.PENDING, 0, occurredAt, null, null, null);
    }
}
