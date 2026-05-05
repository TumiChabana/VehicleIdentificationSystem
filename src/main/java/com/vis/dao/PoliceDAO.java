package com.vis.dao;

import com.vis.model.PoliceReport;
import com.vis.model.Violation;
import com.vis.util.DBConnection;
import com.vis.util.DataCache;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceDAO {

    // ── POLICE REPORTS ──────────────────────────────

    public List<PoliceReport> getAllReports() {
        List<PoliceReport> list = new ArrayList<>();
        String sql = """
        SELECT pr.*, v.registration_number as reg
        FROM police_report pr
        JOIN vehicle v ON pr.vehicle_id = v.vehicle_id
        ORDER BY pr.report_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PoliceReport r = mapReport(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    public boolean addReport(PoliceReport r) {
        String sql = """
        INSERT INTO police_report
        (vehicle_id, report_date, report_type,
         description, officer_name, created_by_user_id)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getVehicleId());
            ps.setDate(2, Date.valueOf(r.getDate()));
            ps.setString(3, r.getReportType());
            ps.setString(4, r.getDescription());
            ps.setString(5, r.getOfficerName());
            // Tag this report to the officer who filed it
            if (r.getCreatedByUserId() > 0) {
                ps.setInt(6, r.getCreatedByUserId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            boolean success = ps.executeUpdate() > 0;
            if (success) DataCache.getInstance().invalidate(); // ← only if it worked
            return success;



        } catch (SQLException e) {
            System.err.println("Error adding report: " + e.getMessage());
            return false;
        }
    }

    // ── VIOLATIONS ───────────────────────────────────

    public List<Violation> getAllViolations() {
        List<Violation> list = new ArrayList<>();
        String sql = """
        SELECT vl.*, v.registration_number as reg
        FROM violation vl
        JOIN vehicle v ON vl.vehicle_id = v.vehicle_id
        ORDER BY vl.violation_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Violation v = mapViolation(rs);
                v.setVehicleReg(rs.getString("reg"));
                list.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }



    public boolean deleteReport(int id) {
        String sql = "DELETE FROM police_report WHERE report_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) DataCache.getInstance().invalidate(); // ← only if it worked
            return success;

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteViolation(int id) {
        String sql = "DELETE FROM violation WHERE violation_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public boolean addViolation(Violation v) {
        String sql = """
        INSERT INTO violation
        (vehicle_id, violation_date, violation_type,
         fine_amount, status, created_by_user_id)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getVehicleId());
            ps.setDate(2, Date.valueOf(v.getViolationDate()));
            ps.setString(3, v.getViolationType());
            ps.setDouble(4, v.getFineAmount());
            ps.setString(5, v.getStatus());
            if (v.getCreatedByUserId() > 0) {
                ps.setInt(6, v.getCreatedByUserId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
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

    // Get reports filed by a specific user
    public List<PoliceReport> getReportsByUserId(int userId) {
        List<PoliceReport> list = new ArrayList<>();
        String sql = """
        SELECT pr.*, v.registration_number as reg
        FROM police_report pr
        JOIN vehicle v ON pr.vehicle_id = v.vehicle_id
        WHERE pr.created_by_user_id = ?
        ORDER BY pr.report_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PoliceReport r = mapReport(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    // Get violations recorded by a specific user
    public List<Violation> getViolationsByUserId(int userId) {
        List<Violation> list = new ArrayList<>();
        String sql = """
        SELECT vl.*, v.registration_number as reg
        FROM violation vl
        JOIN vehicle v ON vl.vehicle_id = v.vehicle_id
        WHERE vl.created_by_user_id = ?
        ORDER BY vl.violation_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Violation v = mapViolation(rs);
                v.setVehicleReg(rs.getString("reg"));
                list.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    // Get reports for vehicles owned by a customer
    public List<PoliceReport> getReportsByCustomerId(
            int customerId) {
        List<PoliceReport> list = new ArrayList<>();
        String sql = """
        SELECT pr.*, v.registration_number as reg
        FROM police_report pr
        JOIN vehicle v ON pr.vehicle_id = v.vehicle_id
        WHERE v.owner_id = ?
        ORDER BY pr.report_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PoliceReport r = mapReport(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    public List<Violation> getViolationsByCustomerId(int customerId) {
        List<Violation> list = new ArrayList<>();
        String sql = """
        SELECT vl.*, v.registration_number as reg
        FROM violation vl
        JOIN vehicle v ON vl.vehicle_id = v.vehicle_id
        WHERE v.owner_id = ?
        ORDER BY vl.violation_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Violation v = mapViolation(rs);
                v.setVehicleReg(rs.getString("reg"));
                list.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}