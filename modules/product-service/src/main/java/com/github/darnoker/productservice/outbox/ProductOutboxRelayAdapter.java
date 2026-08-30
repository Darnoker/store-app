package com.github.darnoker.productservice.outbox;

import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.common.outbox.OutboxRelayRepository;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProductOutboxRelayAdapter implements OutboxRelayRepository {
    private final OutboxEventRepository repository;

    public List<OutboundMessage> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil) {
        return repository.claimBatch(batchSize, instanceId, leaseUntil).stream()
                .map(event -> new OutboundMessage(event.id(), event.destination(), event.aggregateId(), event.eventType(), event.createdAt(), event.payload())).toList();
    }

    public int markAsPublished(Collection<UUID> ids, UUID instanceId) {
        return repository.markAsPublished(ids, instanceId);
    }

    public int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil) {
        return repository.renewLease(ids, instanceId, leaseUntil);
    }

    public void updateError(UUID id, UUID instanceId, int maxRetries, Instant nextAttemptAt, String error) {
        repository.updateError(id, instanceId, maxRetries, nextAttemptAt, error);
    }
}
