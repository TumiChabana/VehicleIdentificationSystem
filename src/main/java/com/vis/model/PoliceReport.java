package com.vis.model;

import java.time.LocalDate;

public class PoliceReport extends VehicleRecord {

    private String reportType;
    private String officerName;
    private String vehicleReg;

    public PoliceReport() {}

    public PoliceReport(int recordId, int vehicleId, LocalDate date,
                        String reportType, String description,
                        String officerName) {
        super(recordId, vehicleId, date, description);
        this.reportType = reportType;
        this.officerName = officerName;
    }

    @Override
    public String getRecordType() { return "Police Report"; }

    @Override
    public String getSummary() {
        return reportType + " reported by " + officerName + " on " + date;
    }

    public String getReportType() { return reportType; }
    public void setReportType(String t) { this.reportType = t; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String o) { this.officerName = o; }

    public String getVehicleReg() { return vehicleReg; }
    public void setVehicleReg(String r) { this.vehicleReg = r; }
}