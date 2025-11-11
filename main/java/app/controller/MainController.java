package app.controller;

import app.model.User;
import app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML private AnchorPane contentArea;
    @FXML private Label txtWelcome;

    // Sidebar buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnAuditLog;
    @FXML private Button btnResidents;
    @FXML private Button btnServices;
    @FXML private Button btnPersonnel;
    @FXML private Button btnInventory;
    @FXML private Button btnTransactions;

    /**
     * Called after login to initialize UI based on user role
     */
    public void initializeWithUser(User user) {
        System.out.println("=== Initializing Main Window for: " + user.getUsername() + " ===");

        // Update welcome text
        if (txtWelcome != null) {
            txtWelcome.setText("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        }

        // Configure UI based on role
        configureUIForRole();
    }

    /**
     * Configure which buttons are visible/enabled based on user role
     */
    private void configureUIForRole() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            System.err.println("✗ No user in session!");
            return;
        }

        System.out.println("Configuring UI for role: " + user.getRole());

        // All roles can access these
        enableButton(btnDashboard, true);
        enableButton(btnResidents, true);
        enableButton(btnServices, true);

        // Audit Log - Admin only
        enableButton(btnAuditLog, user.isAdmin());

        // Admin: Full access to everything
        if (user.isAdmin()) {
            enableButton(btnPersonnel, true);
            enableButton(btnInventory, true);
            enableButton(btnTransactions, true);
        }
        // Personnel: Can access transactions and inventory, not personnel management
        else if (user.isPersonnel()) {
            enableButton(btnPersonnel, false); // Cannot manage other personnel
            enableButton(btnInventory, true);
            enableButton(btnTransactions, true);
        }
        // Staff: Limited access - can only encode residents and services
        else if (user.isStaff()) {
            enableButton(btnPersonnel, false);
            enableButton(btnInventory, false);
            enableButton(btnTransactions, false);
        }
    }

    private void enableButton(Button button, boolean enabled) {
        if (button != null) {
            button.setDisable(!enabled);
            button.setOpacity(enabled ? 1.0 : 0.5);
        }
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Loading view: " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            javafx.scene.Node pane = loader.load(); // Changed to Node instead of AnchorPane

            contentArea.getChildren().clear();
            contentArea.getChildren().add(pane);

            // Set anchors for any Node type
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);

            System.out.println("✓ View loaded successfully");

        } catch (IOException e) {
            System.err.println("✗ Error loading view: " + fxmlFile);
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Loading Error");
            alert.setHeaderText("Could not load " + fxmlFile);
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void showDashboard() {
        System.out.println("=== Show Dashboard ===");
        loadView("dashboard.fxml");
    }

    @FXML
    private void showAuditLog() {
        if (!SessionManager.isAdmin()) {
            showAccessDenied("Only Admins can view audit logs");
            return;
        }
        System.out.println("=== Show Audit Log ===");
        loadView("audit_log.fxml");
    }

    @FXML
    private void showResidents() {
        System.out.println("=== Show Residents ===");
        loadView("residents.fxml");
    }

    @FXML
    private void showServices() {
        System.out.println("=== Show Services ===");
        loadView("services.fxml");
    }

    @FXML
    private void showPersonnel() {
        if (!SessionManager.isAdmin()) {
            showAccessDenied("Only Admins can manage personnel");
            return;
        }
        System.out.println("=== Show Personnel ===");
        loadView("personnel.fxml");
    }

    @FXML
    private void showInventory() {
        if (!SessionManager.canManageInventory()) {
            showAccessDenied("You don't have permission to manage inventory");
            return;
        }
        System.out.println("=== Show Inventory ===");
        loadView("inventory.fxml");
    }

    @FXML
    private void showTransactions() {
        if (!SessionManager.canManageTransactions()) {
            showAccessDenied("You don't have permission to manage transactions");
            return;
        }
        System.out.println("=== Show Transactions ===");
        loadView("transactions.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to continue.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logout();
            }
        });
    }

    private void logout() {
        try {
            // Clear session
            SessionManager.clearSession();

            // Load login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Login - Barangay Health System");
            stage.setScene(new javafx.scene.Scene(root, 400, 500));

            // Close current window
            Stage currentStage = (Stage) contentArea.getScene().getWindow();
            currentStage.close();

            // Show login window
            stage.show();

            System.out.println("✓ Logged out successfully");

        } catch (Exception e) {
            System.err.println("✗ Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAccessDenied(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Insufficient Permissions");
        alert.setContentText(message);
        alert.showAndWait();
    }
}