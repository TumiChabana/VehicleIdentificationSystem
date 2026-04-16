package com.vis.model;

import java.time.LocalDate;

public class Violation {

    private int violationId;
    private int vehicleId;
    private LocalDate violationDate;
    private String violationType;
    private double fineAmount;
    private String status;

    public Violation() {}

    public Violation(int violationId, int vehicleId, LocalDate violationDate,
                     String violationType, double fineAmount, String status) {
        this.violationId = violationId;
        this.vehicleId = vehicleId;
        this.violationDate = violationDate;
        this.violationType = violationType;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public int getViolationId() { return violationId; }
    public void setViolationId(int v) { this.violationId = v; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int v) { this.vehicleId = v; }

    public LocalDate getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDate d) { this.violationDate = d; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String t) { this.violationType = t; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double f) { this.fineAmount = f; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}