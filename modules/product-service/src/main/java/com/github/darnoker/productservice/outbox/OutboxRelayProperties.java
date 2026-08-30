package com.github.darnoker.productservice.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("outbox.relay")
public record OutboxRelayProperties(int batchSize, Duration publishTimeout, Duration leaseDuration, Duration leaseRenewalInterval) {
    public OutboxRelayProperties {
        if (batchSize < 1 || publishTimeout.isZero() || publishTimeout.isNegative() || leaseDuration.isZero() || leaseDuration.isNegative() || leaseRenewalInterval.isZero() || leaseRenewalInterval.isNegative() || publishTimeout.compareTo(leaseDuration) > 0 || leaseRenewalInterval.compareTo(leaseDuration) >= 0) {
            throw new IllegalArgumentException("Invalid outbox relay configuration");
        }
    }
}
