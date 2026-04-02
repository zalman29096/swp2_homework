package org.example;

import org.example.model.Item;

import java.util.List;

public final class ShoppingCartCalculator {

    private ShoppingCartCalculator() {
    }

    public static double calculateItemTotal(Item item) {
        return item.price() * item.quantity();
    }

    public static double calculateCartTotal(List<Item> items) {
        double total = 0.0;
        for (Item item : items) {
            total += calculateItemTotal(item);
        }
        return total;
    }
}
