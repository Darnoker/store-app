package com.github.darnoker.productservice.inventory;

import com.github.darnoker.common.async.InboundMessage;
import com.github.darnoker.common.async.InboundMessageHandler;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.message.inbox.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedMessageHandler implements InboundMessageHandler {

    private static final String CONSUMER_NAME = "product-service.order-created";

    private final InboxService inboxService;

    private final InventoryService inventoryService;

    private final OrderCreatedMessageMapper mapper;


    @Override
    public String eventType() {
        return OrderEventType.ORDER_CREATED.name();
    }

    @Override
    public void consume(InboundMessage message) {
        log.info("Consuming message {} of type {}", message.id(), message.eventType());
        if(!inboxService.recordIfAbsent(CONSUMER_NAME, message.id())) {
            return;
        }
        ReserveStockCommand command = mapper.toReserveStockCommand(message);
        inventoryService.reserveStock(command);
        log.info("Reserved stocks completed");



    }
}
