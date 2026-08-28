package com.github.darnoker.common.async;

import java.time.Instant;
import java.util.UUID;

public record OutboundMessage(UUID id,
                              String destination,
                              UUID aggregateId,
                              String eventType,
                              Instant occuredAt,
                              String payload) {
}
