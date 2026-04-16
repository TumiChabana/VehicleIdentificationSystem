package com.vis.model;

public class Officer extends Person {

    private String badgeNumber;
    private String station;

    public Officer() {}

    public Officer(int id, String name, String phone,
                   String email, String badgeNumber, String station) {
        super(id, name, phone, email);
        this.badgeNumber = badgeNumber;
        this.station = station;
    }

    // Polymorphism — Officer's own version of getSummary()
    @Override
    public String getSummary() {
        return "Officer: " + name + " | Badge: " + badgeNumber
                + " | Station: " + station;
    }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }
}