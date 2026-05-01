package com.vis.dao;

import com.vis.model.ServiceRecord;
import com.vis.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {

    public List<ServiceRecord> getAllServiceRecords() {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = """
            SELECT sr.*, v.registration_number as reg
            FROM service_record sr
            JOIN vehicle v ON sr.vehicle_id = v.vehicle_id
            ORDER BY sr.service_date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching services: "
                    + e.getMessage());
        }
        return list;
    }

    public List<ServiceRecord> getServicesByVehicle(int vehicleId) {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = """
            SELECT sr.*, v.registration_number as reg
            FROM service_record sr
            JOIN vehicle v ON sr.vehicle_id = v.vehicle_id
            WHERE sr.vehicle_id = ?
            ORDER BY sr.service_date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching services: "
                    + e.getMessage());
        }
        return list;
    }

    public boolean addServiceRecord(ServiceRecord sr) {
        String sql = """
            INSERT INTO service_record
            (vehicle_id, service_date, service_type,
             description, cost)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sr.getVehicleId());
            ps.setDate(2, Date.valueOf(sr.getDate()));
            ps.setString(3, sr.getServiceType());
            ps.setString(4, sr.getDescription());
            ps.setDouble(5, sr.getCost());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding service: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean deleteServiceRecord(int id) {
        String sql = "DELETE FROM service_record "
                + "WHERE service_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting service: "
                    + e.getMessage());
            return false;
        }
    }

    private ServiceRecord mapRow(ResultSet rs)
            throws SQLException {
        ServiceRecord sr = new ServiceRecord(
                rs.getInt("service_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("service_date").toLocalDate(),
                rs.getString("service_type"),
                rs.getString("description"),
                rs.getDouble("cost")
        );
        sr.setVehicleReg(rs.getString("reg"));
        return sr;
    }

    // Get service records for vehicles owned by a customer
    public List<ServiceRecord> getServicesByCustomerId(
            int customerId) {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = """
        SELECT sr.*, v.registration_number as reg
        FROM service_record sr
        JOIN vehicle v ON sr.vehicle_id = v.vehicle_id
        WHERE v.owner_id = ?
        ORDER BY sr.service_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ServiceRecord sr = new ServiceRecord(
                        rs.getInt("service_id"),
                        rs.getInt("vehicle_id"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost")
                );
                sr.setVehicleReg(rs.getString("reg"));
                list.add(sr);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}