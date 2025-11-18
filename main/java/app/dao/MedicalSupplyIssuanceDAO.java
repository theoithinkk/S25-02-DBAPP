package app.dao;

import app.model.MedicalSupplyIssuance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalSupplyIssuanceDAO {

    /** CREATE - Record a new medical supply issuance */
    public boolean addIssuance(MedicalSupplyIssuance issuance) {
        String sql = "INSERT INTO MedicalSupplyIssuance (resident_id, item_id, personnel_id, quantity_issued, issuance_date, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issuance.getResidentId());
            ps.setInt(2, issuance.getItemId());
            ps.setInt(3, issuance.getPersonnelId());
            ps.setInt(4, issuance.getQuantityIssued());
            ps.setDate(5, issuance.getIssuanceDate());
            ps.setString(6, issuance.getRemarks());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ - Get all medical supply issuances */
    public List<MedicalSupplyIssuance> getAllIssuances() {
        List<MedicalSupplyIssuance> list = new ArrayList<>();
        String sql = "SELECT msi.issuance_id, msi.quantity_issued, msi.issuance_date, msi.remarks, " +
                "r.resident_id, r.first_name AS resident_first, r.last_name AS resident_last, " +
                "ci.item_id, ci.item_name, " +
                "hp.personnel_id, hp.first_name AS personnel_first, hp.last_name AS personnel_last " +
                "FROM MedicalSupplyIssuance msi " +
                "JOIN Residents r ON msi.resident_id = r.resident_id " +
                "JOIN ClinicInventory ci ON msi.item_id = ci.item_id " +
                "JOIN HealthPersonnel hp ON msi.personnel_id = hp.personnel_id " +
                "ORDER BY msi.issuance_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MedicalSupplyIssuance issuance = new MedicalSupplyIssuance();
                issuance.setIssuanceId(rs.getInt("issuance_id"));
                issuance.setResidentId(rs.getInt("resident_id"));
                issuance.setItemId(rs.getInt("item_id"));
                issuance.setPersonnelId(rs.getInt("personnel_id"));
                issuance.setQuantityIssued(rs.getInt("quantity_issued"));
                issuance.setIssuanceDate(rs.getDate("issuance_date"));
                issuance.setRemarks(rs.getString("remarks"));

                // Set derived display fields
                issuance.setResidentName(rs.getString("resident_first") + " " + rs.getString("resident_last"));
                issuance.setItemName(rs.getString("item_name"));
                issuance.setPersonnelName(rs.getString("personnel_first") + " " + rs.getString("personnel_last"));

                list.add(issuance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** READ - Get issuances for a specific resident */
    public List<MedicalSupplyIssuance> getIssuancesByResident(int residentId) {
        List<MedicalSupplyIssuance> list = new ArrayList<>();
        String sql = "SELECT msi.*, ci.item_name, hp.first_name AS personnel_first, hp.last_name AS personnel_last " +
                "FROM MedicalSupplyIssuance msi " +
                "JOIN ClinicInventory ci ON msi.item_id = ci.item_id " +
                "JOIN HealthPersonnel hp ON msi.personnel_id = hp.personnel_id " +
                "WHERE msi.resident_id = ? " +
                "ORDER BY msi.issuance_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalSupplyIssuance issuance = extractIssuanceFromResultSet(rs);
                list.add(issuance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** READ - Get issuances for a specific date range */
    public List<MedicalSupplyIssuance> getIssuancesByDateRange(Date startDate, Date endDate) {
        List<MedicalSupplyIssuance> list = new ArrayList<>();
        String sql = "SELECT msi.*, r.first_name AS resident_first, r.last_name AS resident_last, " +
                "ci.item_name, hp.first_name AS personnel_first, hp.last_name AS personnel_last " +
                "FROM MedicalSupplyIssuance msi " +
                "JOIN Residents r ON msi.resident_id = r.resident_id " +
                "JOIN ClinicInventory ci ON msi.item_id = ci.item_id " +
                "JOIN HealthPersonnel hp ON msi.personnel_id = hp.personnel_id " +
                "WHERE msi.issuance_date BETWEEN ? AND ? " +
                "ORDER BY msi.issuance_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalSupplyIssuance issuance = extractIssuanceFromResultSet(rs);
                issuance.setResidentName(rs.getString("resident_first") + " " + rs.getString("resident_last"));
                issuance.setPersonnelName(rs.getString("personnel_first") + " " + rs.getString("personnel_last"));
                list.add(issuance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Helper method to extract issuance from ResultSet */
    private MedicalSupplyIssuance extractIssuanceFromResultSet(ResultSet rs) throws SQLException {
        MedicalSupplyIssuance issuance = new MedicalSupplyIssuance();
        issuance.setIssuanceId(rs.getInt("issuance_id"));
        issuance.setResidentId(rs.getInt("resident_id"));
        issuance.setItemId(rs.getInt("item_id"));
        issuance.setPersonnelId(rs.getInt("personnel_id"));
        issuance.setQuantityIssued(rs.getInt("quantity_issued"));
        issuance.setIssuanceDate(rs.getDate("issuance_date"));
        issuance.setRemarks(rs.getString("remarks"));
        issuance.setItemName(rs.getString("item_name"));
        return issuance;
    }
}