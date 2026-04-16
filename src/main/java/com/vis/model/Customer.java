package com.vis.model;

public class Customer extends Person {

    private String address;

    public Customer() {}

    public Customer(int id, String name, String address,
                    String phone, String email) {
        super(id, name, phone, email);
        this.address = address;
    }

    // Polymorphism — Customer's own version of getSummary()
    @Override
    public String getSummary() {
        return "Customer: " + name + " | Phone: " + phone
                + " | Email: " + email;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}