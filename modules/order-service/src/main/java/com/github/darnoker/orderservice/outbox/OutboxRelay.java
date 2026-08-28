package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final AsyncMessagePublisher publisher;

    private final OutboxRepository repository;

    @Scheduled(fixedDelayString = "${outbox.relay.polling-interval-ms:10000}")
    @Transactional
    public void relay() {
        Set<UUID> publishedIds = repository.findPending().stream()
                .map(OutboxMessageMapper::mapToOutboundMessage)
                .map(this::publish)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        repository.markAsPublished(publishedIds);
    }

    private Optional<UUID> publish(OutboundMessage message) {
        try {
            publisher.publish(message)
                    .toCompletableFuture()
                    .join();

            return Optional.of(message.id());
        } catch (Exception e) {
            log.error("Failed to publish outbox message {}", message.id(), e);
        }
        return Optional.empty();
    }
}
