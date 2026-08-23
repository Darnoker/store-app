package com.github.darnoker.productservice.inventory;

public class StockAdjustmentBelowReservedQuantityException extends IllegalStateException {

    public StockAdjustmentBelowReservedQuantityException() {
        super("Stock cannot be reduced below reserved quantity");
    }
}
