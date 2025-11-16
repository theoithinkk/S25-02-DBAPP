package app.dao;

import app.model.Resident;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {

    /**
     * Find the first available ID (either a gap or next sequential)
     */
    private Integer findFirstAvailableId(Connection conn) throws SQLException {
        // First, check if ID 1 exists
        String checkOne = "SELECT COUNT(*) as count FROM Residents WHERE resident_id = 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(checkOne)) {
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("✨ ID 1 is available, using it");
                return 1;
            }
        }

        // Query to find the first gap in the sequence
        String sql = "SELECT t1.resident_id + 1 AS gap " +
                "FROM Residents t1 " +
                "LEFT JOIN Residents t2 ON t1.resident_id + 1 = t2.resident_id " +
                "WHERE t2.resident_id IS NULL " +
                "ORDER BY gap " +
                "LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                int gapId = rs.getInt("gap");

                // Check if this gap is less than or equal to the max ID (it's a real gap)
                String maxSql = "SELECT MAX(resident_id) as max_id FROM Residents";
                try (ResultSet maxRs = st.executeQuery(maxSql)) {
                    if (maxRs.next()) {
                        int maxId = maxRs.getInt("max_id");
                        if (gapId <= maxId) {
                            System.out.println("♻️ Found gap at ID: " + gapId);
                            return gapId;
                        }
                    }
                }
            }
        }

        // No gaps found, return null to use auto-increment
        return null;
    }

    public boolean addResident(Resident r) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Find first available ID (gap)
            Integer availableId = findFirstAvailableId(conn);

            String sql;
            if (availableId != null) {
                // Insert with specific ID (filling gap)
                sql = "INSERT INTO Residents (resident_id, first_name, last_name, age, sex, contact_number, address, household_id, vulnerability_status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, availableId);
                ps.setString(2, r.getFirstName());
                ps.setString(3, r.getLastName());
                ps.setInt(4, r.getAge());
                ps.setString(5, r.getSex());
                ps.setString(6, r.getContactNumber());
                ps.setString(7, r.getAddress());

                if (r.getHouseholdId() != null) {
                    ps.setInt(8, r.getHouseholdId());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }
                ps.setString(9, r.getVulnerabilityStatus());

                System.out.println("✓ Inserting with ID: " + availableId);
            } else {
                // Insert normally (auto-increment)
                sql = "INSERT INTO Residents (first_name, last_name, age, sex, contact_number, address, household_id, vulnerability_status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, r.getFirstName());
                ps.setString(2, r.getLastName());
                ps.setInt(3, r.getAge());
                ps.setString(4, r.getSex());
                ps.setString(5, r.getContactNumber());
                ps.setString(6, r.getAddress());

                if (r.getHouseholdId() != null) {
                    ps.setInt(7, r.getHouseholdId());
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                ps.setString(8, r.getVulnerabilityStatus());

                System.out.println("✓ Using auto-increment for new ID");
            }

            boolean success = ps.executeUpdate() > 0;
            conn.commit(); // Commit transaction

            return success;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Resident> getAllResidents() {
        List<Resident> list = new ArrayList<>();
        String sql = "SELECT * FROM Residents ORDER BY resident_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Resident r = new Resident();
                r.setResidentId(rs.getInt("resident_id"));
                r.setFirstName(rs.getString("first_name"));
                r.setLastName(rs.getString("last_name"));
                r.setAge(rs.getInt("age"));
                r.setSex(rs.getString("sex"));
                r.setContactNumber(rs.getString("contact_number"));
                r.setAddress(rs.getString("address"));

                // Handle nullable fields
                int householdId = rs.getInt("household_id");
                if (!rs.wasNull()) {
                    r.setHouseholdId(householdId);
                }
                r.setVulnerabilityStatus(rs.getString("vulnerability_status"));

                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateResident(Resident r) {
        String sql = "UPDATE Residents SET first_name=?, last_name=?, age=?, sex=?, " +
                "contact_number=?, address=?, household_id=?, vulnerability_status=? " +
                "WHERE resident_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getFirstName());
            ps.setString(2, r.getLastName());
            ps.setInt(3, r.getAge());
            ps.setString(4, r.getSex());
            ps.setString(5, r.getContactNumber());
            ps.setString(6, r.getAddress());

            if (r.getHouseholdId() != null) {
                ps.setInt(7, r.getHouseholdId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, r.getVulnerabilityStatus());
            ps.setInt(9, r.getResidentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteResident(int id) {
        String sql = "DELETE FROM Residents WHERE resident_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;

            if (success) {
                System.out.println("🗑️ Deleted resident ID: " + id + " (gap created)");
            }

            return success;
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