package org.example.service;

import org.example.db.DatabaseConnection;
import org.example.model.Item;

import java.sql.*;
import java.util.List;
import java.util.Locale;

/**
 * Persists shopping cart records and items.
 */
public class CartService {

    /**
     * Saves a cart record and its items. Returns generated cart id, or -1 if failed.
     */
    public long saveCart(int totalItems, double totalCost, Locale locale, List<Item> items) {
        String language = toDbLang(locale);
        String insertRecord = "INSERT INTO cart_records(total_items, total_cost, language) VALUES(?,?,?)";
        String insertItem = "INSERT INTO cart_items(cart_record_id, item_number, price, quantity, subtotal) VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            long cartId;
            try (PreparedStatement ps = conn.prepareStatement(insertRecord, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, totalItems);
                ps.setDouble(2, totalCost);
                ps.setString(3, language);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    cartId = rs.next() ? rs.getLong(1) : -1L;
                }
            }

            if (cartId <= 0) {
                conn.rollback();
                return -1L;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                int index = 1;
                for (Item item : items) {
                    ps.setLong(1, cartId);
                    ps.setInt(2, index++);
                    ps.setDouble(3, item.price());
                    ps.setInt(4, item.quantity());
                    ps.setDouble(5, item.price() * item.quantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return cartId;
        } catch (Exception e) {
            // Ignore errors: feature is optional when DB isn't available
            return -1L;
        }
    }

    private String toDbLang(Locale locale) {
        String country = locale.getCountry();
        if (country == null || country.isBlank()) {
            return locale.getLanguage();
        }
        return locale.getLanguage() + "_" + country;
    }
}
