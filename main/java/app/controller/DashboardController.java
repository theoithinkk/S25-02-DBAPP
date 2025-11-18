package app.controller;

import app.dao.DBConnection;
import app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.text.Text;

import java.sql.*;

public class DashboardController {

    @FXML private Text txtWelcome;
    @FXML private Text txtResidentCount;
    @FXML private Text txtServiceCount;
    @FXML private Text txtTransactionCount;

    @FXML
    private void initialize() {
        // Set welcome message
        if (SessionManager.isLoggedIn()) {
            txtWelcome.setText("Welcome, " + SessionManager.getCurrentUser().getUsername() +
                    " (" + SessionManager.getCurrentUser().getRole() + ")");
        }

        // Load statistics
        loadStatistics();
    }

    // Helper to get the owner window
    private Window getOwnerWindow() {
        if (txtWelcome != null && txtWelcome.getScene() != null && txtWelcome.getScene().getWindow() != null) {
            return txtWelcome.getScene().getWindow();
        }
        return null;
    }

    private void loadStatistics() {
        try (Connection conn = DBConnection.getConnection()) {

            // Count residents
            String residentSql = "SELECT COUNT(*) FROM Residents";
            Statement st1 = conn.createStatement();
            ResultSet rs1 = st1.executeQuery(residentSql);
            if (rs1.next()) {
                txtResidentCount.setText(String.valueOf(rs1.getInt(1)));
            }

            // Count services
            String serviceSql = "SELECT COUNT(*) FROM HealthServices";
            Statement st2 = conn.createStatement();
            ResultSet rs2 = st2.executeQuery(serviceSql);
            if (rs2.next()) {
                txtServiceCount.setText(String.valueOf(rs2.getInt(1)));
            }

            // Count today's transactions
            String transactionSql = "SELECT COUNT(*) FROM ServiceTransactions WHERE DATE(date_provided) = CURDATE()";
            Statement st3 = conn.createStatement();
            ResultSet rs3 = st3.executeQuery(transactionSql);
            if (rs3.next()) {
                txtTransactionCount.setText(String.valueOf(rs3.getInt(1)));
            }

        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAvailHealthService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/avail_service_dialog.fxml"));
            ScrollPane root = loader.load();

            Stage dialogStage = new Stage();

            // Set owner window BEFORE setting modality
            Window owner = getOwnerWindow();
            if (owner != null) {
                dialogStage.initOwner(owner);
                System.out.println("✅ Dialog owner set for Avail Health Service");
            } else {
                System.out.println("⚠️ Warning: Could not find owner window for Avail Health Service dialog");
            }

            dialogStage.setTitle("Avail Health Service");
            // FIXED: Use NONE to avoid blocking issues - dialog is still on top
            // User can click between windows, but dialog stays visible
            dialogStage.setScene(new Scene(root, 650, 750));
            dialogStage.setResizable(false);

            // Refresh statistics when dialog closes
            dialogStage.setOnHidden(e -> loadStatistics());

            System.out.println("📋 About to show Avail Health Service dialog...");
            dialogStage.show(); // Changed from showAndWait() to show()

        } catch (Exception e) {
            System.err.println("Error opening avail service dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open transaction dialog");
        }
    }

    @FXML
    private void handleIssueMedicalSupplies() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/issue_supplies.fxml"));
            ScrollPane root = loader.load();

            Stage dialogStage = new Stage();

            // Set owner window BEFORE setting modality
            Window owner = getOwnerWindow();
            if (owner != null) {
                dialogStage.initOwner(owner);
                System.out.println("✅ Dialog owner set for Medical Supplies");
            } else {
                System.out.println("⚠️ Warning: Could not find owner window for Medical Supplies dialog");
            }

            dialogStage.setTitle("Medical Supply Issuance");
            dialogStage.setScene(new Scene(root, 650, 750));
            dialogStage.setResizable(false);

            // Refresh statistics when dialog closes
            dialogStage.setOnHidden(e -> loadStatistics());

            System.out.println("📋 About to show Medical Supplies dialog...");
            dialogStage.show();

        } catch (Exception e) {
            System.err.println("Error opening medical supplies dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open transaction dialog");
        }
    }

    @FXML
    private void handleRestockInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/restock_inventory.fxml"));
            ScrollPane root = loader.load();

            Stage dialogStage = new Stage();

            // Set owner window BEFORE setting modality
            Window owner = getOwnerWindow();
            if (owner != null) {
                dialogStage.initOwner(owner);
                System.out.println("✅ Dialog owner set for Restock Inventory");
            } else {
                System.out.println("⚠️ Warning: Could not find owner window for Restock Inventory dialog");
            }

            dialogStage.setTitle("Restock Clinic Inventory");
            dialogStage.setScene(new Scene(root, 650, 750));
            dialogStage.setResizable(false);

            System.out.println("📋 About to show Restock Inventory dialog...");
            dialogStage.show();

        } catch (Exception e) {
            System.err.println("Error opening restock inventory dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open restock inventory dialog");
        }
    }

    @FXML
    private void handleLogClinicVisit() {
        try {
            Parent clinicVisitsRoot = FXMLLoader.load(getClass().getResource("/view/clinicvisits.fxml"));

            Stage stage = new Stage();

            // Set owner window BEFORE setting modality
            Window owner = getOwnerWindow();
            if (owner != null) {
                stage.initOwner(owner);
                System.out.println("✅ Stage owner set for Log Clinic Visit");
            } else {
                System.out.println("⚠️ Warning: Could not find owner window for Log Clinic Visit");
            }

            stage.setTitle("Log Clinic Visit");
            stage.setScene(new Scene(clinicVisitsRoot, 700, 750));
            stage.setResizable(false);

            System.out.println("📋 About to show Log Clinic Visit window...");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load clinic visits form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Set owner for alerts
        Window owner = getOwnerWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}