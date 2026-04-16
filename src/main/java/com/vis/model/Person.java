package com.vis.model;

// Abstract base class — cannot be instantiated directly
// Customer and Officer will inherit from this
public abstract class Person {

    protected int id;
    protected String name;
    protected String phone;
    protected String email;

    public Person() {}

    public Person(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Abstract method — every Person subclass MUST implement this
    // This is our polymorphism in action
    public abstract String getSummary();

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return getSummary();
    }
}