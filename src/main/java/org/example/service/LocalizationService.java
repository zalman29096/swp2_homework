package org.example.service;

import org.example.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationService {

    public Map<String, String> loadStrings(Locale locale) {
        Map<String, String> map = new HashMap<>();
        String lang = toDbLang(locale);
        String sql = "SELECT `key`, `value` FROM localization_strings WHERE language = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getString(2));
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return map;
    }

    private String toDbLang(Locale locale) {
        String country = locale.getCountry();
        if (country == null || country.isBlank()) {
            return locale.getLanguage();
        }
        return locale.getLanguage() + "_" + country;
    }
}
