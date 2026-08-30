package com.github.darnoker.common.outbox;

import java.time.Duration;

public record OutboxRelaySettings(int batchSize, Duration publishTimeout, Duration leaseDuration,
                                  Duration leaseRenewalInterval, int maxRetries) {
    public OutboxRelaySettings {
        if (batchSize < 1 || maxRetries < 1 || publishTimeout.isNegative() || publishTimeout.isZero()
                || leaseDuration.isNegative() || leaseDuration.isZero() || leaseRenewalInterval.isNegative()
                || leaseRenewalInterval.isZero() || publishTimeout.compareTo(leaseDuration) > 0
                || leaseRenewalInterval.compareTo(leaseDuration) >= 0) {
            throw new IllegalArgumentException("Invalid outbox relay settings");
        }
    }
}
