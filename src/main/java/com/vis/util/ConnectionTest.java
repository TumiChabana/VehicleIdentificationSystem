package com.vis.util;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ SUCCESS — Neon DB is live!");
                System.out.println("   Host: " +
                        conn.getMetaData().getURL());
            }
        } catch (SQLException e) {
            System.err.println("❌ FAILED: " + e.getMessage());
        } finally {
            DBConnection.closeConnection();
        }
    }
}