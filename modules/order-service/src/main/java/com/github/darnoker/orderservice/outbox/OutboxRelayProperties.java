package com.github.darnoker.orderservice.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("outbox.relay")
public record OutboxRelayProperties(
        int batchSize,
        Duration publishTimeout,
        Duration leaseDuration,
        Duration leaseRenewalInterval
) {

    public OutboxRelayProperties {
        if (batchSize < 1) {
            throw new IllegalArgumentException("outbox.relay.batch-size must be positive");
        }
        if (publishTimeout.isZero() || publishTimeout.isNegative()) {
            throw new IllegalArgumentException("outbox.relay.publish-timeout must be positive");
        }
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("outbox.relay.lease-duration must be positive");
        }
        if (leaseRenewalInterval.isZero() || leaseRenewalInterval.isNegative()) {
            throw new IllegalArgumentException("outbox.relay.lease-renewal-interval must be positive");
        }
        if (publishTimeout.compareTo(leaseDuration) > 0) {
            throw new IllegalArgumentException("outbox.relay.publish-timeout must not exceed lease-duration");
        }
        if (leaseRenewalInterval.compareTo(leaseDuration) >= 0) {
            throw new IllegalArgumentException("outbox.relay.lease-renewal-interval must be shorter than lease-duration");
        }
    }
}
