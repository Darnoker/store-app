package com.github.darnoker.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.github.darnoker.productservice.outbox.OutboxRelayProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(OutboxRelayProperties.class)
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
