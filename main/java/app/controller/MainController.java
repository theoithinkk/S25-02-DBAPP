package app.controller;

import app.model.User;
import app.util.BackgroundMusicPlayer;
import app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
    @FXML private Button btnReports;

    // Music controls
    @FXML private Button btnMuteMusic;
    @FXML private Slider volumeSlider;
    @FXML private Label lblVolume;

    /**
     * Called after login to initialize UI based on user role
     */
    public void initializeWithUser(User user) {
        System.out.println("=== Initializing Main Window for: " + user.getUsername() + " ===");

        // Update welcome text
        if (txtWelcome != null) {
            txtWelcome.setText(user.getUsername());
        }

        // Configure UI based on role
        configureUIForRole();

        BackgroundMusicPlayer.play();

        // Initialize music controls
        initializeMusicControls();

        // **LOAD DASHBOARD AS DEFAULT PAGE**
        showDashboard();
    }

    /**
     * Initialize music controls (volume slider and mute button)
     */
    private void initializeMusicControls() {
        // Check if music is available
        if (!BackgroundMusicPlayer.isAvailable()) {
            // Hide music controls if media module is not available
            if (btnMuteMusic != null) {
                btnMuteMusic.setVisible(false);
                btnMuteMusic.setManaged(false);
            }
            if (volumeSlider != null) {
                volumeSlider.setVisible(false);
                volumeSlider.setManaged(false);
            }
            if (lblVolume != null) {
                lblVolume.setVisible(false);
                lblVolume.setManaged(false);
            }
            System.out.println("⚠ Music controls hidden (JavaFX Media module not available)");
            return;
        }

        // Setup volume slider
        if (volumeSlider != null) {
            volumeSlider.setValue(BackgroundMusicPlayer.getVolume() * 100);
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                BackgroundMusicPlayer.setVolume(newVal.doubleValue() / 100.0);
                if (lblVolume != null) {
                    lblVolume.setText(String.format("%.0f%%", newVal.doubleValue()));
                }
            });
        }

        // Update mute button
        updateMusicButton();
    }

    /**
     * Toggle music mute/unmute
     */
    @FXML
    private void toggleMusic() {
        BackgroundMusicPlayer.toggleMute();
        updateMusicButton();
    }

    /**
     * Update music button text and style based on mute state
     */
    private void updateMusicButton() {
        if (btnMuteMusic != null) {
            if (BackgroundMusicPlayer.isMuted()) {
                btnMuteMusic.setText("🔇 Unmute");
                btnMuteMusic.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            } else {
                btnMuteMusic.setText("🔊 Mute");
                btnMuteMusic.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        }
    }

    /**
     * Configure which buttons are visible/enabled based on user role
     * ADMIN: Full access to everything
     * PERSONNEL: CR on everything, CRD on inventory, CRU on transactions, no access to personnel management
     * STAFF: CR on everything, can generate reports, no access to personnel/inventory/transactions management
     */
    private void configureUIForRole() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            System.err.println("✗ No user in session!");
            return;
        }

        System.out.println("Configuring UI for role: " + user.getRole());

        // All roles can access these
        setButtonVisibility(btnDashboard, true);
        setButtonVisibility(btnResidents, true);
        setButtonVisibility(btnServices, true);
        setButtonVisibility(btnReports, true);

        // Audit Log - Admin only (completely hide for others)
        setButtonVisibility(btnAuditLog, user.canAccessAuditLog());

        // Personnel Management - Admin only
        setButtonVisibility(btnPersonnel, user.isAdmin());

        // Inventory - Admin and Personnel only (Staff cannot access)
        setButtonVisibility(btnInventory, user.canManageInventory());

        // Transactions - Admin and Personnel only (Staff cannot access)
        setButtonVisibility(btnTransactions, user.canViewTransactions());
    }

    /**
     * Set button visibility and enable/disable state
     * Makes button completely invisible if not allowed
     */
    private void setButtonVisibility(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible); // Also affects layout
        }
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Loading view: " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            javafx.scene.Node pane = loader.load();

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
        User user = SessionManager.getCurrentUser();
        if (user == null || !user.canAccessAuditLog()) {
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
        User user = SessionManager.getCurrentUser();
        if (user == null || !user.canManageInventory()) {
            showAccessDenied("You don't have permission to manage inventory");
            return;
        }
        System.out.println("=== Show Inventory ===");
        loadView("inventory.fxml");
    }

    @FXML
    private void showTransactions() {
        User user = SessionManager.getCurrentUser();
        if (user == null || !user.canViewTransactions()) {
            showAccessDenied("You don't have permission to manage transactions");
            return;
        }
        System.out.println("=== Show Transactions ===");
        loadView("transactions.fxml");
    }

    @FXML
    private void showReports() {
        System.out.println("=== Show Reports ===");
        loadView("reports.fxml");
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

            // Stop music
            BackgroundMusicPlayer.dispose();

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

            // Restart music for next login
            BackgroundMusicPlayer.initialize();

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