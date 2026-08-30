package com.github.darnoker.productservice.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InventoryEventType {
    STOCK_RESERVED("stock.reserved"),
    STOCK_RESERVATION_FAILED("stock.reservation.failed"),
    STOCK_CONFIRMED("stock.confirmed"),
    STOCK_RELEASED("stock.released"),
    STOCK_EXPIRED("stock.expired"),
    STOCK_ADJUSTED("stock.adjusted");

    private final String value;
}
