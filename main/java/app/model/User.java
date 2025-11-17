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

    /**
     * Can delete residents, services, and personnel records
     * Only Admin can delete these
     */
    public boolean canDeleteRecords() {
        return isAdmin();
    }

    /**
     * Can delete inventory items
     * Admin and Personnel can delete inventory
     */
    public boolean canDeleteInventory() {
        return isAdmin() || isPersonnel();
    }

    /**
     * Can update service transactions
     * Admin and Personnel can update transactions
     */
    public boolean canUpdateTransactions() {
        return isAdmin() || isPersonnel();
    }

    /**
     * Can delete service transactions
     * Only Admin can delete transactions
     */
    public boolean canDeleteTransactions() {
        return isAdmin();
    }

    /**
     * Can manage inventory (view, add, update)
     * Admin and Personnel can manage inventory (not Staff)
     */
    public boolean canManageInventory() {
        return isAdmin() || isPersonnel();
    }

    /**
     * Can view transactions
     * Admin and Personnel can view transactions (not Staff)
     */
    public boolean canViewTransactions() {
        return isAdmin() || isPersonnel();
    }

    /**
     * Can access audit log
     * Only Admin can access audit log
     */
    public boolean canAccessAuditLog() {
        return isAdmin();
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}