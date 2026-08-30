package com.github.darnoker.productservice.message.inbox;

import com.github.darnoker.productservice.message.inbox.model.InboxMessage;
import com.github.darnoker.productservice.message.inbox.persistence.InboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxRepository repository;

    private final Clock clock;

    /**
     * Must be called inside the transaction that performs the business reaction
     * and creates its outbox event.
     */
    @Transactional
    public boolean recordIfAbsent(String consumerName, UUID eventId) {
        return repository.recordIfAbsent(new InboxMessage(consumerName, eventId, Instant.now(clock)));
    }
}
