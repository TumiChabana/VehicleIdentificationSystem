package com.vis.model;

import java.time.LocalDate;

public class InsuranceRecord extends VehicleRecord {

    private String providerName;
    private String policyNumber;
    private LocalDate endDate;
    private double premiumAmount;
    private String status;

    public InsuranceRecord() {}

    public InsuranceRecord(int recordId, int vehicleId, LocalDate startDate,
                           String description, String providerName,
                           String policyNumber, LocalDate endDate,
                           double premiumAmount, String status) {
        super(recordId, vehicleId, startDate, description);
        this.providerName = providerName;
        this.policyNumber = policyNumber;
        this.endDate = endDate;
        this.premiumAmount = premiumAmount;
        this.status = status;
    }

    @Override
    public String getRecordType() { return "Insurance Record"; }

    @Override
    public String getSummary() {
        return providerName + " | Policy: " + policyNumber
                + " | Status: " + status;
    }

    public String getProviderName() { return providerName; }
    public void setProviderName(String p) { this.providerName = p; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String p) { this.policyNumber = p; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate e) { this.endDate = e; }

    public double getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(double p) { this.premiumAmount = p; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}