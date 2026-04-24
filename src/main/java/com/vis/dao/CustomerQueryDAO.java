package com.vis.dao;

import com.vis.model.CustomerQuery;
import com.vis.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerQueryDAO {

    public List<CustomerQuery> getAllQueries() {
        List<CustomerQuery> list = new ArrayList<>();
        String sql = """
            SELECT cq.*,
                   c.name as customer_name,
                   v.registration_number as vehicle_reg
            FROM customer_query cq
            JOIN customer c ON cq.customer_id = c.customer_id
            JOIN vehicle v  ON cq.vehicle_id  = v.vehicle_id
            ORDER BY cq.query_date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching queries: "
                    + e.getMessage());
        }
        return list;
    }

    public List<CustomerQuery> getQueriesByCustomer(
            int customerId) {
        List<CustomerQuery> list = new ArrayList<>();
        String sql = """
            SELECT cq.*,
                   c.name as customer_name,
                   v.registration_number as vehicle_reg
            FROM customer_query cq
            JOIN customer c ON cq.customer_id = c.customer_id
            JOIN vehicle v  ON cq.vehicle_id  = v.vehicle_id
            WHERE cq.customer_id = ?
            ORDER BY cq.query_date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching queries: "
                    + e.getMessage());
        }
        return list;
    }

    public boolean addQuery(CustomerQuery q) {
        String sql = """
            INSERT INTO customer_query
            (customer_id, vehicle_id, query_date, query_text)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, q.getCustomerId());
            ps.setInt(2, q.getVehicleId());
            ps.setDate(3, Date.valueOf(q.getDate()));
            ps.setString(4, q.getQueryText());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding query: "
                    + e.getMessage());
            return false;
        }
    }

    // Uses stored procedure from Phase 4
    public boolean respondToQuery(int queryId,
                                  String response) {
        String sql = "CALL respond_to_query(?, ?)";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, queryId);
            cs.setString(2, response);
            cs.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("Error responding: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuery(int queryId) {
        String sql = "DELETE FROM customer_query "
                + "WHERE query_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, queryId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting query: "
                    + e.getMessage());
            return false;
        }
    }

    private CustomerQuery mapRow(ResultSet rs)
            throws SQLException {
        CustomerQuery q = new CustomerQuery();
        q.setQueryId(rs.getInt("query_id"));
        q.setCustomerId(rs.getInt("customer_id"));
        q.setVehicleId(rs.getInt("vehicle_id"));
        q.setDate(rs.getDate("query_date").toLocalDate());
        q.setQueryText(rs.getString("query_text"));
        q.setResponseText(rs.getString("response_text"));
        q.setCustomerName(rs.getString("customer_name"));
        q.setVehicleReg(rs.getString("vehicle_reg"));
        return q;
    }
}