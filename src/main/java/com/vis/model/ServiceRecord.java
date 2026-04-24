package com.vis.model;

import java.time.LocalDate;

public class ServiceRecord extends VehicleRecord {

    private String serviceType;
    private double cost;
    private String vehicleReg;

    public ServiceRecord() {

    }


    public String getVehicleReg() { return vehicleReg; }
    public void setVehicleReg(String r) { this.vehicleReg = r; }

    public ServiceRecord(int recordId, int vehicleId, LocalDate date,
                         String serviceType, String description, double cost) {
        super(recordId, vehicleId, date, description);
        this.serviceType = serviceType;
        this.cost = cost;
    }

    @Override
    public String getRecordType() { return "Service Record"; }

    @Override
    public String getSummary() {
        return serviceType + " on " + date + " — M" + cost;
    }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String t) { this.serviceType = t; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}