package app.dao;

import app.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for User authentication and management
 */
public class UserDAO {

    /**
     * Authenticate user login
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));

                int personnelId = rs.getInt("personnel_id");
                if (!rs.wasNull()) {
                    user.setPersonnelId(personnelId);
                }

                // Log the login
                logAction(user.getUserId(), "LOGIN");

                System.out.println("✓ User logged in: " + username + " (" + user.getRole() + ")");
                return user;
            }

            System.out.println("✗ Login failed: Invalid credentials");
            return null;

        } catch (SQLException e) {
            System.err.println("✗ Login error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create new user account
     */
    public boolean createUser(User user, int createdBy) {
        // Check if admin limit reached (only 4 admins allowed)
        if (user.isAdmin() && countAdmins() >= 4) {
            System.err.println("✗ Cannot create admin: Maximum 4 admin accounts allowed");
            return false;
        }

        // If Personnel role, verify personnel_id exists
        if (user.isPersonnel() && user.getPersonnelId() != null) {
            if (!personnelExists(user.getPersonnelId())) {
                System.err.println("✗ Cannot create Personnel account: Personnel ID does not exist");
                return false;
            }
        }

        String sql = "INSERT INTO Users (username, password, role, personnel_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());

            if (user.getPersonnelId() != null) {
                ps.setInt(4, user.getPersonnelId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction(createdBy, "CREATE_USER: " + user.getUsername() + " (" + user.getRole() + ")");
                System.out.println("✓ User created: " + user.getUsername());
            }

            return success;

        } catch (SQLException e) {
            System.err.println("✗ Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all users (Admin only feature)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));

                int personnelId = rs.getInt("personnel_id");
                if (!rs.wasNull()) {
                    user.setPersonnelId(personnelId);
                }

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Delete user (Admin only)
     */
    public boolean deleteUser(int userId, int deletedBy) {
        // Prevent deleting the last admin
        User user = getUserById(userId);
        if (user != null && user.isAdmin() && countAdmins() <= 1) {
            System.err.println("✗ Cannot delete: Must have at least 1 admin account");
            return false;
        }

        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction(deletedBy, "DELETE_USER: " + userId);
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change password
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                logAction(userId, "CHANGE_PASSWORD");
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

    /**
     * Get user by ID
     */
    private User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Count admin accounts
     */
    private int countAdmins() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'Admin'";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if personnel already has an account
     */
    public boolean personnelHasAccount(int personnelId) {
        String sql = "SELECT COUNT(*) FROM Users WHERE personnel_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personnelId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Create user account through self-registration (no admin approval needed)
     */
    public boolean createUserSelfRegistration(User user) {
        // Personnel accounts must link to existing personnel
        if (user.isPersonnel() && user.getPersonnelId() != null) {
            if (!personnelExists(user.getPersonnelId())) {
                System.err.println("✗ Personnel ID does not exist");
                return false;
            }
            if (personnelHasAccount(user.getPersonnelId())) {
                System.err.println("✗ Personnel already has an account");
                return false;
            }
        }

        String sql = "INSERT INTO Users (username, password, role, personnel_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());

            if (user.getPersonnelId() != null) {
                ps.setInt(4, user.getPersonnelId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                System.out.println("✓ Self-registration successful: " + user.getUsername() + " (" + user.getRole() + ")");
            }

            return success;

        } catch (SQLException e) {
            System.err.println("✗ Error during self-registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if personnel exists in HealthPersonnel table
     */
    private boolean personnelExists(int personnelId) {
        String sql = "SELECT COUNT(*) FROM HealthPersonnel WHERE personnel_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personnelId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}