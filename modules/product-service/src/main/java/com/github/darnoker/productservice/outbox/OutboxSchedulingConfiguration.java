package com.github.darnoker.productservice.outbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
class OutboxSchedulingConfiguration {
    @Bean("outboxLeaseTaskScheduler")
    ThreadPoolTaskScheduler outboxLeaseTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("outbox-lease-");
        return scheduler;
    }
}
