package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.common.outbox.OutboxRelayRepository;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrderOutboxRelayAdapter implements OutboxRelayRepository {
    private final OutboxRepository repository;

    public List<OutboundMessage> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil) {
        return repository.claimBatch(batchSize, instanceId, leaseUntil).stream().map(OutboxMessageMapper::mapToOutboundMessage).toList();
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
