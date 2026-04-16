package com.vis.model;

import java.time.LocalDate;

// Abstract base for all vehicle-related records
// ServiceRecord, PoliceReport, InsuranceRecord inherit from this
public abstract class VehicleRecord {

    protected int recordId;
    protected int vehicleId;
    protected LocalDate date;
    protected String description;

    public VehicleRecord() {}

    public VehicleRecord(int recordId, int vehicleId,
                         LocalDate date, String description) {
        this.recordId = recordId;
        this.vehicleId = vehicleId;
        this.date = date;
        this.description = description;
    }

    // Abstract — each record type displays itself differently
    public abstract String getRecordType();
    public abstract String getSummary();

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }
}