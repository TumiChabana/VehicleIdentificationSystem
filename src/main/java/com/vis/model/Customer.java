package com.vis.model;

public class Customer extends Person {

    private String address;


    public Customer(int id, String name, String address,
                    String phone, String email) {
        super(id, name, phone, email);
        this.address = address;
    }

    public Customer() {

    }

    // Polymorphism — Customer's own version of getSummary()
    @Override
    public String getSummary() {
        return "Customer: " + name + " | Phone: " + phone
                + " | Email: " + email;
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}