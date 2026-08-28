package com.github.darnoker.orderservice.outbox.persistence;

enum OutboxEventEntityStatus {
    PENDING, PROCESSING, PUBLISHED, FAILED
}
