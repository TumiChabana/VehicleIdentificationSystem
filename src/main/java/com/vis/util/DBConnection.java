package com.vis.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()  // won't crash if .env missing
            .load();

    private static final String URL =
            dotenv.get("DB_URL");
    private static final String USER =
            dotenv.get("DB_USER");
    private static final String PASSWORD =
            dotenv.get("DB_PASSWORD");

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.createStatement()
                        .execute("SET search_path TO public");
                System.out.println("✅ Database connected successfully.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            throw e;
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔒 Connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing: " + e.getMessage());
            }
        }
    }
}