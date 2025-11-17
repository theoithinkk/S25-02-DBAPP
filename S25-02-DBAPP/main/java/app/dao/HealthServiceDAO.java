package app.dao;

import app.model.HealthService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for HealthServices table with ID gap filling functionality
 */
public class HealthServiceDAO {

    /** CREATE - Standard auto-increment **/
    public boolean addService(HealthService s) {
        String sql = "INSERT INTO HealthServices (service_type, description, fee, remarks) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getServiceType());
            ps.setString(2, s.getDescription());
            ps.setDouble(3, s.getFee());
            ps.setString(4, s.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** CREATE - With specific ID (for filling gaps) **/
    public boolean addServiceWithId(HealthService s) {
        String sql = "INSERT INTO HealthServices (service_id, service_type, description, fee, remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getServiceId());
            ps.setString(2, s.getServiceType());
            ps.setString(3, s.getDescription());
            ps.setDouble(4, s.getFee());
            ps.setString(5, s.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ **/
    public List<HealthService> getAllServices() {
        List<HealthService> list = new ArrayList<>();
        String sql = "SELECT * FROM HealthServices ORDER BY service_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HealthService s = new HealthService();
                s.setServiceId(rs.getInt("service_id"));
                s.setServiceType(rs.getString("service_type"));
                s.setDescription(rs.getString("description"));
                s.setFee(rs.getDouble("fee"));
                s.setRemarks(rs.getString("remarks"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** UPDATE **/
    public boolean updateService(HealthService s) {
        String sql = "UPDATE HealthServices SET service_type=?, description=?, fee=?, remarks=? WHERE service_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getServiceType());
            ps.setString(2, s.getDescription());
            ps.setDouble(3, s.getFee());
            ps.setString(4, s.getRemarks());
            ps.setInt(5, s.getServiceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** DELETE **/
    public boolean deleteService(int id) {
        String sql = "DELETE FROM HealthServices WHERE service_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Log user action to audit log
     */
    public void logAction(int userId, String action) {
        String sql = "INSERT INTO AuditLog (user_id, action) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Don't fail the main operation if logging fails
            System.err.println("Warning: Could not log action: " + e.getMessage());
        }
    }
}