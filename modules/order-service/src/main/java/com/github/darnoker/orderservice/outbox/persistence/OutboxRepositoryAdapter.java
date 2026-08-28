package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class OutboxRepositoryAdapter implements OutboxRepository {

    private final JpaOutboxEventRepository repository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return OutboxPersistenceMapper.toDomain(repository.save(OutboxPersistenceMapper.toEntity(outboxEvent)));
    }

    @Override
    public List<OutboxEvent> findPending() {
        return repository.findAllByStatus(OutboxEventEntityStatus.PENDING).stream()
                .map(OutboxPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int markAsPublished(Collection<UUID> uuid) {
        return repository.markAsPublished(uuid);
    }
}
