package com.vis.model;

public class Vehicle {

    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private String color;
    private int ownerId;
    private String ownerName;
    public Vehicle() {}

    public Vehicle(int vehicleId, String registrationNumber, String make,
                   String model, int year, String color, int ownerId) {
        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.ownerId = ownerId;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { registrationNumber = r; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    // Add getter and setter
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return year + " " + make + " " + model
                + " [" + registrationNumber + "]";
    }
}