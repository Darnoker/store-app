package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.orderservice.outbox.model.OutboxEvent;

final class OutboxMessageMapper {

    public static OutboundMessage mapToOutboundMessage(OutboxEvent event) {
        return new OutboundMessage(
                event.id(),
                event.destination(),
                event.aggregateId(),
                event.eventType(),
                event.createdAt(),
                event.payload()
        );
    }
}
