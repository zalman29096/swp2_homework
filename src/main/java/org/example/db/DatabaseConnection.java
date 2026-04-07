package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String url ="jdbc:mysql://127.0.0.1:3306/shopping_cart_localization?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true";
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        return DriverManager.getConnection(url, user, pass);
    }

}
