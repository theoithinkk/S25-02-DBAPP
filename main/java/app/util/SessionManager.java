package app.util;

import app.model.User;

/**
 * Manages the current logged-in user session
 */
public class SessionManager {

    private static User currentUser;

    /**
     * Set the current logged-in user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("✓ Session started for: " + user.getUsername());
    }

    /**
     * Get the current logged-in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the current session (logout)
     */
    public static void clearSession() {
        if (currentUser != null) {
            System.out.println("✓ Session cleared for: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Checks if current user has admin role
     */
    public static boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    /**
     * Check if current user has personnel role
     */
    public static boolean isPersonnel() {
        return isLoggedIn() && currentUser.isPersonnel();
    }

    /**
     * Check if current user has staff role
     */
    public static boolean isStaff() {
        return isLoggedIn() && currentUser.isStaff();
    }

    /**
     * Check if current user can delete records (residents, services, personnel)
     * Only Admin can delete these
     */
    public static boolean canDelete() {
        return isLoggedIn() && currentUser.canDeleteRecords();
    }

    /**
     * Check if current user can delete inventory items
     * Admin and Personnel can delete inventory
     */
    public static boolean canDeleteInventory() {
        return isLoggedIn() && currentUser.canDeleteInventory();
    }

    /**
     * Check if current user can update transactions
     * Admin and Personnel can update transactions
     */
    public static boolean canUpdateTransactions() {
        return isLoggedIn() && currentUser.canUpdateTransactions();
    }

    /**
     * Check if current user can delete transactions
     * Only Admin can delete transactions
     */
    public static boolean canDeleteTransactions() {
        return isLoggedIn() && currentUser.canDeleteTransactions();
    }

    /**
     * Check if current user can manage inventory
     * Admin and Personnel can manage inventory
     */
    public static boolean canManageInventory() {
        return isLoggedIn() && currentUser.canManageInventory();
    }

    /**
     * Check if current user can view/manage transactions
     * Admin and Personnel can view transactions
     */
    public static boolean canManageTransactions() {
        return isLoggedIn() && currentUser.canViewTransactions();
    }

    /**
     * Check if current user can access audit log
     * Only Admin can access audit log
     */
    public static boolean canAccessAuditLog() {
        return isLoggedIn() && currentUser.canAccessAuditLog();
    }
}