package app.dao;

import app.model.ServiceTransaction;
import app.model.TransactionStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransactionDAO {

    /** CREATE **/
    public boolean addTransaction(ServiceTransaction t) {
        String sql = "INSERT INTO ServiceTransactions (service_id, resident_id, personnel_id, date_provided, status, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getServiceId());
            ps.setInt(2, t.getResidentId());
            ps.setInt(3, t.getPersonnelId());
            ps.setDate(4, t.getDateProvided());
            ps.setString(5, t.getStatus() != null ? t.getStatus().name() : TransactionStatus.PENDING.name());
            ps.setString(6, t.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ **/
    public List<ServiceTransaction> getAllTransactions() {
        List<ServiceTransaction> list = new ArrayList<>();
        String sql = "SELECT st.transaction_id, st.resident_id, st.personnel_id, st.date_provided, st.status, st.remarks, " +
                "r.first_name AS resident_first, r.last_name AS resident_last, " +
                "hs.service_id, hs.service_type, " +
                "hp.first_name AS personnel_first, hp.last_name AS personnel_last " +
                "FROM ServiceTransactions st " +
                "LEFT JOIN Residents r ON st.resident_id = r.resident_id " +
                "LEFT JOIN HealthServices hs ON st.service_id = hs.service_id " +
                "LEFT JOIN HealthPersonnel hp ON st.personnel_id = hp.personnel_id " +
                "ORDER BY st.date_provided DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ServiceTransaction t = new ServiceTransaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setServiceId(rs.getInt("service_id"));
                t.setResidentId(rs.getInt("resident_id"));
                t.setPersonnelId(rs.getInt("personnel_id"));
                t.setDateProvided(rs.getDate("date_provided"));
                t.setRemarks(rs.getString("remarks"));

                // Parse status
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        t.setStatus(TransactionStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        t.setStatus(TransactionStatus.PENDING);
                    }
                } else {
                    t.setStatus(TransactionStatus.PENDING);
                }

                String residentFirst = rs.getString("resident_first");
                String residentLast = rs.getString("resident_last");
                t.setResidentName((residentFirst != null ? residentFirst : "") + " " +
                        (residentLast != null ? residentLast : ""));

                t.setServiceType(rs.getString("service_type"));

                String personnelFirst = rs.getString("personnel_first");
                String personnelLast = rs.getString("personnel_last");
                t.setPersonnelName((personnelFirst != null ? personnelFirst : "") + " " +
                        (personnelLast != null ? personnelLast : ""));

                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** UPDATE STATUS **/
    public boolean updateTransactionStatus(int transactionId, TransactionStatus status) {
        String sql = "UPDATE ServiceTransactions SET status = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** DELETE **/
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM ServiceTransactions WHERE transaction_id=?";
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
            System.err.println("Warning: Could not log action: " + e.getMessage());
        }
    }
}