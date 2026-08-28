package com.github.darnoker.orderservice.outbox.model;

public enum OutboxEventStatus {
    PENDING, PROCESSING, PUBLISHED, FAILED
}
