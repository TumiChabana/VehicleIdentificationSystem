package com.vis.model;

import java.time.LocalDate;

public class CustomerQuery {

    private int queryId;
    private int customerId;
    private int vehicleId;
    private LocalDate date;
    private String queryText;
    private String responseText;

    // Joined fields for display
    private String customerName;
    private String vehicleReg;

    public CustomerQuery() {}

    public int getQueryId() { return queryId; }
    public void setQueryId(int q) { this.queryId = q; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int c) { this.customerId = c; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int v) { this.vehicleId = v; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate d) { this.date = d; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String q) { this.queryText = q; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String r) {
        this.responseText = r;
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String c) {
        this.customerName = c;
    }

    public String getVehicleReg() { return vehicleReg; }
    public void setVehicleReg(String v) { this.vehicleReg = v; }
}