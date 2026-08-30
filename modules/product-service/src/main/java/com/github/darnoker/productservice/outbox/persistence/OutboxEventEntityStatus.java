package com.github.darnoker.productservice.outbox.persistence;

enum OutboxEventEntityStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}
