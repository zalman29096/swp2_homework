package org.example.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationService {

    public Map<String, String> loadStrings(Locale locale) {
        Map<String, String> map = new HashMap<>();
        String lang = toDbLang(locale);
        String sql = "SELECT `key`, `value` FROM localization_strings WHERE language = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn != null ? conn.prepareStatement(sql) : null) {
            if (ps == null) {
                return map;
            }
            ps.setString(1, lang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getString(2));
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return map;
    }

    // Avoid compile-time dependency to DatabaseConnection to enable testing without MySQL/driver on classpath
    private Connection getConnection() throws Exception {
        try {
            Class<?> dbClass = Class.forName("org.example.db.DatabaseConnection");
            Method m = dbClass.getMethod("getConnection");
            return (Connection) m.invoke(null);
        } catch (Throwable t) {
            throw new SQLException(t);
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
