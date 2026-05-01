package com.vis.model;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String role;
    private int customerId;      // links to customer table
    private String identifier;

    public User() {}

    public User(int userId, String username,
                String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int u) { this.userId = u; }

    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String p) { this.passwordHash = p; }

    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int c) { this.customerId = c; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String i) {
        this.identifier = i;
    }
}