package com.vis.dao;

import com.vis.model.Vehicle;
import com.vis.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    // GET ALL VEHICLES
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicle ORDER BY vehicle_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching vehicles: " + e.getMessage());
        }
        return list;
    }

    // GET VEHICLE BY ID
    public Vehicle getVehicleById(int id) {
        String sql = "SELECT * FROM vehicle WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Error fetching vehicle: " + e.getMessage());
        }
        return null;
    }

    // SEARCH BY REGISTRATION NUMBER
    public Vehicle getVehicleByReg(String reg) {
        String sql = "SELECT * FROM vehicle WHERE registration_number ILIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reg.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Error searching vehicle: " + e.getMessage());
        }
        return null;
    }

    // ADD VEHICLE — uses stored procedure
    public boolean addVehicle(Vehicle v) {
        String sql = "CALL add_vehicle(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, v.getRegistrationNumber());
            cs.setString(2, v.getMake());
            cs.setString(3, v.getModel());
            cs.setInt(4, v.getYear());
            cs.setString(5, v.getColor());
            cs.setInt(6, v.getOwnerId());
            cs.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding vehicle: " + e.getMessage());
            return false;
        }
    }

    // UPDATE VEHICLE
    public boolean updateVehicle(Vehicle v) {
        String sql = "UPDATE vehicle SET registration_number=?, make=?, " +
                "model=?, year=?, color=?, owner_id=? " +
                "WHERE vehicle_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getRegistrationNumber());
            ps.setString(2, v.getMake());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getYear());
            ps.setString(5, v.getColor());
            ps.setInt(6, v.getOwnerId());
            ps.setInt(7, v.getVehicleId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            return false;
        }
    }

    // DELETE VEHICLE
    public boolean deleteVehicle(int id) {
        String sql = "DELETE FROM vehicle WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
            return false;
        }
    }

    public List<Vehicle> getAllVehiclesWithOwners() {
        List<Vehicle> list = new ArrayList<>();
        String sql = """
        SELECT v.*, c.name as owner_name
        FROM vehicle v
        LEFT JOIN customer c ON v.owner_id = c.customer_id
        ORDER BY v.vehicle_id
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicle v = mapRow(rs);
                v.setOwnerName(rs.getString("owner_name"));
                list.add(v);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching vehicles: "
                    + e.getMessage());
        }
        return list;
    }

    // MAP DATABASE ROW → Vehicle object
    private Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("registration_number"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color"),
                rs.getInt("owner_id")
        );
    }
}