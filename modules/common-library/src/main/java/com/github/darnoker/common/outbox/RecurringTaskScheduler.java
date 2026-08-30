package com.github.darnoker.common.outbox;

import java.time.Duration;

public interface RecurringTaskScheduler {
    ScheduledTask scheduleAtFixedRate(Runnable task, Duration interval);
    interface ScheduledTask { void cancel(); }
}
