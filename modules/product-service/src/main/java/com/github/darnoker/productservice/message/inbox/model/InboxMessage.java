package com.github.darnoker.productservice.message.inbox.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InboxMessage(String consumerName, UUID eventId, Instant receivedAt) {

    public InboxMessage {
        if (consumerName == null || consumerName.isBlank()) {
            throw new IllegalArgumentException("Consumer name is required");
        }
        Objects.requireNonNull(eventId, "Event id is required");
        Objects.requireNonNull(receivedAt, "Received time is required");
    }
}
