package com.github.darnoker.productservice.inventory.model;

public record Quantity(int value) {

    public Quantity {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity must not be negative");
        }
    }

    public Quantity add(Quantity other) {
        return new Quantity(Math.addExact(value, other.value));
    }

    public Quantity add(int amount) {
        return new Quantity(Math.addExact(value, amount));
    }

    public Quantity subtract(Quantity other) {
        return new Quantity(Math.subtractExact(value, other.value));
    }

    public boolean isLessThan(Quantity other) {
        return value < other.value;
    }

    public boolean isZero() {
        return value == 0;
    }
}
