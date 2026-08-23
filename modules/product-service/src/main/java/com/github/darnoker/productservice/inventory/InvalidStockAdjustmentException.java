package com.github.darnoker.productservice.inventory;

public class InvalidStockAdjustmentException extends IllegalArgumentException {

    public InvalidStockAdjustmentException(String message) {
        super(message);
    }
}
