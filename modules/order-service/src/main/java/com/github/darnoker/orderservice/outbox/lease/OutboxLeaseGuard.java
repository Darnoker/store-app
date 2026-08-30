package com.github.darnoker.orderservice.outbox.lease;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public record OutboxLeaseGuard(AtomicBoolean held, ScheduledFuture<?> heartbeat) implements AutoCloseable {

    public boolean isHeld() {
        return held.get();
    }

    @Override
    public void close() {
        if (heartbeat != null) {
            heartbeat.cancel(false);
        }
    }
}
