package com.vis.dao;

import com.vis.model.InsuranceRecord;
import com.vis.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsuranceDAO {

    public List<InsuranceRecord> getAllRecords() {
        List<InsuranceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM insurance_record ORDER BY insurance_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching insurance: " + e.getMessage());
        }
        return list;
    }

    public boolean addRecord(InsuranceRecord r) {
        String sql = "INSERT INTO insurance_record " +
                "(vehicle_id, provider_name, policy_number, " +
                "start_date, end_date, premium_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getVehicleId());
            ps.setString(2, r.getProviderName());
            ps.setString(3, r.getPolicyNumber());
            ps.setDate(4, Date.valueOf(r.getDate()));
            ps.setDate(5, Date.valueOf(r.getEndDate()));
            ps.setDouble(6, r.getPremiumAmount());
            ps.setString(7, r.getStatus());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding insurance: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE insurance_record SET status=? " +
                "WHERE insurance_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating insurance: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRecord(int id) {
        String sql = "DELETE FROM insurance_record WHERE insurance_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting insurance: " + e.getMessage());
            return false;
        }
    }

    private InsuranceRecord mapRow(ResultSet rs) throws SQLException {
        return new InsuranceRecord(
                rs.getInt("insurance_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("start_date").toLocalDate(),
                null,
                rs.getString("provider_name"),
                rs.getString("policy_number"),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("premium_amount"),
                rs.getString("status")
        );
    }

    public List<InsuranceRecord> getAllRecordsWithVehicle() {
        List<InsuranceRecord> list = new ArrayList<>();
        String sql = """
        SELECT ir.*, v.registration_number as reg
        FROM insurance_record ir
        JOIN vehicle v ON ir.vehicle_id = v.vehicle_id
        ORDER BY ir.end_date ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsuranceRecord r = mapRow(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    // Get policies by provider name (for insurance role)
    public List<InsuranceRecord> getRecordsByProvider(
            String providerName) {
        List<InsuranceRecord> list = new ArrayList<>();
        String sql = """
        SELECT ir.*, v.registration_number as reg
        FROM insurance_record ir
        JOIN vehicle v ON ir.vehicle_id = v.vehicle_id
        WHERE ir.provider_name ILIKE ?
        ORDER BY ir.end_date ASC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + providerName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InsuranceRecord r = mapRow(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    // Get policies for vehicles owned by a customer
    public List<InsuranceRecord> getRecordsByCustomerId(
            int customerId) {
        List<InsuranceRecord> list = new ArrayList<>();
        String sql = """
        SELECT ir.*, v.registration_number as reg
        FROM insurance_record ir
        JOIN vehicle v ON ir.vehicle_id = v.vehicle_id
        WHERE v.owner_id = ?
        ORDER BY ir.end_date ASC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InsuranceRecord r = mapRow(rs);
                r.setVehicleReg(rs.getString("reg"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}