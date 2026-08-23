package com.github.darnoker.productservice.inventory;

public enum InventoryEventType {
    STOCK_RESERVED("stock.reserved"),
    STOCK_CONFIRMED("stock.confirmed"),
    STOCK_RELEASED("stock.released"),
    STOCK_EXPIRED("stock.expired"),
    STOCK_ADJUSTED("stock.adjusted");

    private final String value;

    InventoryEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
