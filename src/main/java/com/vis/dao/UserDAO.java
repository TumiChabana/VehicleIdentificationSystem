package com.vis.dao;

import com.vis.model.User;
import com.vis.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // LOGIN — checks username and password
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hash = rs.getString("password_hash");

                // Try BCrypt first, fall back to plain text
                boolean passwordMatches;
                try {
                    passwordMatches = BCrypt.checkpw(password, hash);
                } catch (Exception e) {
                    // Plain text fallback for development
                    passwordMatches = password.equals(hash);
                }

                if (passwordMatches) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            hash,
                            rs.getString("role")
                    );

                    // Load new fields safely
                    // (handles if columns don't exist yet)
                    try {
                        user.setCustomerId(
                                rs.getInt("customer_id"));
                        user.setIdentifier(
                                rs.getString("identifier"));
                    } catch (SQLException ex) {
                        System.err.println(
                                "New columns not yet in DB: "
                                        + ex.getMessage());
                    }

                    return user; // ← only ONE return here
                }
            }

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // REGISTER new user
    public static boolean register(String username,
                                   String password,
                                   String role) {
        String sql = "INSERT INTO users " +
                "(username, password_hash, role) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Hash password properly
            String hash = BCrypt.hashpw(
                    password, BCrypt.gensalt(10));

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Register error: "
                    + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users " +
                "ORDER BY role, username";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                // Load extra fields if they exist
                try {
                    user.setCustomerId(
                            rs.getInt("customer_id"));
                    user.setIdentifier(
                            rs.getString("identifier"));
                    // Format the timestamp nicely
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        user.setCreatedAt(ts.toLocalDateTime()
                                .format(java.time.format.DateTimeFormatter
                                        .ofPattern("dd MMM yyyy HH:mm")));
                    } else {
                        user.setCreatedAt("—");
                    }
                } catch (SQLException ex) {
                    user.setCreatedAt("—");
                }
                list.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: "
                    + e.getMessage());
        }
        return list;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: "
                    + e.getMessage());
            return false;
        }
    }

    public static boolean linkCustomer(String username,
                                       int customerId) {
        String sql = "UPDATE users SET customer_id = ? " +
                "WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Link error: " + e.getMessage());
            return false;
        }
    }

    public boolean setIdentifier(String username,
                                 String identifier) {
        String sql = "UPDATE users SET identifier = ? " +
                "WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error setting identifier: "
                    + e.getMessage());
            return false;
        }
    }
}