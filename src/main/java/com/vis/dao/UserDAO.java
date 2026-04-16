package com.vis.dao;

import com.vis.model.User;
import com.vis.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

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
                // BCrypt verifies password against stored hash
                if (BCrypt.checkpw(password, hash)) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            hash,
                            rs.getString("role")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null; // null means login failed
    }

    // REGISTER new user
    public boolean register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password_hash, role) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Hash the password before storing — never store plain text
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Register error: " + e.getMessage());
            return false;
        }
    }
}