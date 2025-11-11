package app.dao;

import app.model.ServiceTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransactionDAO {

    /** CREATE **/
    public boolean addTransaction(ServiceTransaction t) {
        String sql = "INSERT INTO ServiceTransactions (service_id, resident_id, personnel_id, date_provided, remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getServiceId());
            ps.setInt(2, t.getResidentId());
            ps.setInt(3, t.getPersonnelId());
            ps.setDate(4, t.getDateProvided());
            ps.setString(5, t.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** READ **/
    public List<ServiceTransaction> getAllTransactions() {
        List<ServiceTransaction> list = new ArrayList<>();
        String sql = """
            SELECT st.transaction_id, st.date_provided, st.remarks,
                   r.first_name AS resident_first, r.last_name AS resident_last,
                   hs.service_type,
                   hp.first_name AS personnel_first, hp.last_name AS personnel_last
            FROM ServiceTransactions st
            LEFT JOIN Residents r ON st.resident_id = r.resident_id
            LEFT JOIN HealthServices hs ON st.service_id = hs.service_id
            LEFT JOIN HealthPersonnel hp ON st.personnel_id = hp.personnel_id
            ORDER BY st.date_provided DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ServiceTransaction t = new ServiceTransaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setDateProvided(rs.getDate("date_provided"));
                t.setRemarks(rs.getString("remarks"));
                t.setResidentName(rs.getString("resident_first") + " " + rs.getString("resident_last"));
                t.setServiceType(rs.getString("service_type"));
                t.setPersonnelName(rs.getString("personnel_first") + " " + rs.getString("personnel_last"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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
}
