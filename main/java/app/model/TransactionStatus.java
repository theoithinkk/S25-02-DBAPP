package app.model;

/**
 * Enum for Service Transaction Status
 */
public enum TransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Get enum from string value
     */
    public static TransactionStatus fromString(String text) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return PENDING; // Default
    }
}