package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findPending();

    int markAsPublished(Collection<UUID> uuid);
}
