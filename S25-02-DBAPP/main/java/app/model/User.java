package app.model;

/**
 * Simplified User model for authentication
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // Admin, Personnel, Staff
    private Integer personnelId; // Only for Personnel role

    // Constructors
    public User() {}

    public User(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getPersonnelId() {
        return personnelId;
    }

    public void setPersonnelId(Integer personnelId) {
        this.personnelId = personnelId;
    }

    // Role checking methods
    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    public boolean isPersonnel() {
        return "Personnel".equals(role);
    }

    public boolean isStaff() {
        return "Staff".equals(role);
    }

    // Permission checking methods
    public boolean canDelete() {
        return isAdmin(); // Only Admin can delete
    }

    public boolean canManageTransactions() {
        return isAdmin() || isPersonnel(); // Admin and Personnel can manage transactions
    }

    public boolean canManageInventory() {
        return isAdmin() || isPersonnel(); // Admin and Personnel can manage inventory
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}