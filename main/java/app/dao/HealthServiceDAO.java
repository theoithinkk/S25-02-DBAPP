package app.dao;

import app.model.HealthService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for HealthServices table
 */
public class HealthServiceDAO {

    /** CREATE **/
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

    /** READ **/
    public List<HealthService> getAllServices() {
        List<HealthService> list = new ArrayList<>();
        String sql = "SELECT * FROM HealthServices";
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
}
