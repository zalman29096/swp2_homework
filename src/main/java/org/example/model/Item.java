package org.example.model;

public record Item(double price, int quantity) {
    public Item {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }
}
