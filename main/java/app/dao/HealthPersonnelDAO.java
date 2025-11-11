package app.dao;

import app.model.HealthPersonnel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for HealthPersonnel table
 */
public class HealthPersonnelDAO {

    /** CREATE **/
    public boolean addPersonnel(HealthPersonnel p) {
        String sql = "INSERT INTO HealthPersonnel (first_name, last_name, role, specialization, contact_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getRole());
            ps.setString(4, p.getSpecialization());
            ps.setString(5, p.getContactNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ ALL **/
    public List<HealthPersonnel> getAllPersonnel() {
        List<HealthPersonnel> list = new ArrayList<>();
        String sql = "SELECT * FROM HealthPersonnel";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                HealthPersonnel p = new HealthPersonnel();
                p.setPersonnelId(rs.getInt("personnel_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                p.setRole(rs.getString("role"));
                p.setSpecialization(rs.getString("specialization"));
                p.setContactNumber(rs.getString("contact_number"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** GET BY ID **/
    public HealthPersonnel getPersonnelById(int personnelId) {
        String sql = "SELECT * FROM HealthPersonnel WHERE personnel_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personnelId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                HealthPersonnel p = new HealthPersonnel();
                p.setPersonnelId(rs.getInt("personnel_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                p.setRole(rs.getString("role"));
                p.setSpecialization(rs.getString("specialization"));
                p.setContactNumber(rs.getString("contact_number"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** UPDATE **/
    public boolean updatePersonnel(HealthPersonnel p) {
        String sql = "UPDATE HealthPersonnel SET first_name=?, last_name=?, role=?, specialization=?, contact_number=? WHERE personnel_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getRole());
            ps.setString(4, p.getSpecialization());
            ps.setString(5, p.getContactNumber());
            ps.setInt(6, p.getPersonnelId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** DELETE **/
    public boolean deletePersonnel(int id) {
        String sql = "DELETE FROM HealthPersonnel WHERE personnel_id=?";
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