package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final AsyncMessagePublisher publisher;

    private final OutboxRepository repository;

    private final Clock clock;

    private final TransactionTemplate transactionTemplate;

    private final int batchSize = 100;

    private static final UUID INSTANCE_ID = UUID.randomUUID();

    private static final int MAX_RETRIES = 10;

    @Scheduled(fixedDelayString = "${outbox.relay.polling-interval-ms:10000}")
    public void relay() {
        final List<OutboundMessage> claimedMessages = transactionTemplate.execute((_) ->
                repository.claimBatch(batchSize, INSTANCE_ID, Instant.now(clock).plusSeconds(60)).stream()
                        .map(OutboxMessageMapper::mapToOutboundMessage)
                        .toList());

        final Set<UUID> publishedIds = claimedMessages.stream()
                .map(this::publish)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        if (!publishedIds.isEmpty()) {
            transactionTemplate.executeWithoutResult(_ -> repository.markAsPublished(publishedIds, INSTANCE_ID));
        }
    }

    private Optional<UUID> publish(OutboundMessage message) {
        try {
            publisher.publish(message)
                    .toCompletableFuture()
                    .join();

            return Optional.of(message.id());
        } catch (Exception e) {
            log.error("Failed to publish outbox message {}", message.id(), e);
            transactionTemplate.executeWithoutResult(_ -> repository.updateError(
                    message.id(),
                    INSTANCE_ID,
                    MAX_RETRIES,
                    Instant.now(clock).plusSeconds(60),
                    errorMessage(e)
            ));
        }
        return Optional.empty();
    }

    private String errorMessage(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getName());
        return message.substring(0, Math.min(message.length(), 2000));
    }
}
