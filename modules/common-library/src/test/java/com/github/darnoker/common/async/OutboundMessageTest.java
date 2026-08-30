package com.github.darnoker.common.async;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboundMessageTest {

    @Test
    void retainsTheTransportNeutralMessageData() {
        UUID id = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-30T12:00:00Z");

        OutboundMessage message = new OutboundMessage(
                id, "inventory-topic", aggregateId, "STOCK_RESERVED", occurredAt, "{\"quantity\":2}"
        );

        assertEquals(id, message.id());
        assertEquals("inventory-topic", message.destination());
        assertEquals(aggregateId, message.aggregateId());
        assertEquals("STOCK_RESERVED", message.eventType());
        assertEquals(occurredAt, message.occurredAt());
        assertEquals("{\"quantity\":2}", message.payload());
    }
}
