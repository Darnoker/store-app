package com.github.darnoker.common.outbox;

import java.util.function.Supplier;

public interface TransactionRunner {
    <T> T execute(Supplier<T> action);
    void executeWithoutResult(Runnable action);
}
