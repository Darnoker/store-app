package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.outbox.OutboxRelayRepository;
import com.github.darnoker.common.outbox.OutboxRelaySettings;
import com.github.darnoker.common.outbox.RecurringTaskScheduler;
import com.github.darnoker.common.outbox.ReliableOutboxRelay;
import com.github.darnoker.common.outbox.TransactionRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {
    private final ReliableOutboxRelay relay;

    OutboxRelay(AsyncMessagePublisher publisher,
                OutboxRelayRepository repository,
                Clock clock,
                TransactionTemplate transactionTemplate,
                OutboxRelayProperties properties,
                @Qualifier("outboxLeaseTaskScheduler")
                TaskScheduler scheduler) {
        TransactionRunner transactionRunner = new TransactionRunner() {
            public <T> T execute(java.util.function.Supplier<T> action) {
                return transactionTemplate.execute(_ -> action.get());
            }

            public void executeWithoutResult(Runnable action) {
                transactionTemplate.executeWithoutResult(_ -> action.run());
            }
        };
        RecurringTaskScheduler recurringScheduler = (task, interval) -> () -> scheduler.scheduleAtFixedRate(task, interval).cancel(false);
        relay = new ReliableOutboxRelay(
                publisher,
                repository,
                clock,
                transactionRunner,
                new OutboxRelaySettings(
                        properties.batchSize(),
                        properties.publishTimeout(),
                        properties.leaseDuration(),
                        properties.leaseRenewalInterval(),
                        10),
                recurringScheduler
        );
    }

    @Scheduled(fixedDelayString = "${outbox.relay.polling-interval-ms:10000}")
    public void relay() {
        relay.relay();
    }
}
