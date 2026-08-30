package com.github.darnoker.productservice.message.inbox.persistence;

import com.github.darnoker.productservice.message.inbox.model.InboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class InboxRepositoryAdapter implements InboxRepository {

    private final JpaInboxMessageRepository repository;

    @Override
    public boolean recordIfAbsent(InboxMessage message) {
        return repository.insertIfAbsent(message.consumerName(), message.eventId(), message.receivedAt()) == 1;
    }
}
