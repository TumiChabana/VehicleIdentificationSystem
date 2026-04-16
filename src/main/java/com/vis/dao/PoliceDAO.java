package com.vis.dao;

import com.vis.model.PoliceReport;
import com.vis.model.Violation;
import com.vis.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceDAO {

    // ── POLICE REPORTS ──────────────────────────────

    public List<PoliceReport> getAllReports() {
        List<PoliceReport> list = new ArrayList<>();
        String sql = "SELECT * FROM police_report ORDER BY report_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapReport(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching reports: " + e.getMessage());
        }
        return list;
    }

    public boolean addReport(PoliceReport r) {
        String sql = "INSERT INTO police_report " +
                "(vehicle_id, report_date, report_type, " +
                "description, officer_name) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getVehicleId());
            ps.setDate(2, Date.valueOf(r.getDate()));
            ps.setString(3, r.getReportType());
            ps.setString(4, r.getDescription());
            ps.setString(5, r.getOfficerName());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding report: " + e.getMessage());
            return false;
        }
    }

    // ── VIOLATIONS ───────────────────────────────────

    public List<Violation> getAllViolations() {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT * FROM violation ORDER BY violation_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapViolation(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching violations: " + e.getMessage());
        }
        return list;
    }

    public boolean addViolation(Violation v) {
        String sql = "INSERT INTO violation " +
                "(vehicle_id, violation_date, violation_type, " +
                "fine_amount, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getVehicleId());
            ps.setDate(2, Date.valueOf(v.getViolationDate()));
            ps.setString(3, v.getViolationType());
            ps.setDouble(4, v.getFineAmount());
            ps.setString(5, v.getStatus());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding violation: " + e.getMessage());
            return false;
        }
    }

    // UPDATE VIOLATION STATUS — uses stored procedure
    public boolean updateViolationStatus(int id, String status) {
        String sql = "CALL update_violation_status(?, ?)";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.setString(2, status);
            cs.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating violation: " + e.getMessage());
            return false;
        }
    }

    private PoliceReport mapReport(ResultSet rs) throws SQLException {
        return new PoliceReport(
                rs.getInt("report_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("report_date").toLocalDate(),
                rs.getString("report_type"),
                rs.getString("description"),
                rs.getString("officer_name")
        );
    }

    private Violation mapViolation(ResultSet rs) throws SQLException {
        return new Violation(
                rs.getInt("violation_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("violation_date").toLocalDate(),
                rs.getString("violation_type"),
                rs.getDouble("fine_amount"),
                rs.getString("status")
        );
    }
}