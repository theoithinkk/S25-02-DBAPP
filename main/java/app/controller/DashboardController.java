package app.controller;

import app.dao.DBConnection;
import app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.text.Text;
import javafx.application.Platform;

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
        openTransactionDialog("/view/avail_service_dialog.fxml",
                "Avail Health Service", 650, 750, true);
    }

    @FXML
    private void handleIssueMedicalSupplies() {
        openTransactionDialog("/view/issue_supplies.fxml",
                "Medical Supply Issuance", 650, 750, true);
    }

    @FXML
    private void handleRestockInventory() {
        openTransactionDialog("/view/restock_inventory.fxml",
                "Restock Clinic Inventory", 650, 750, false);
    }

    @FXML
    private void handleLogClinicVisit() {
        openTransactionDialog("/view/clinicvisits.fxml",
                "Log Clinic Visit", 700, 750, false);
    }

    private void openTransactionDialog(String resourcePath,
                                       String title,
                                       double width,
                                       double height,
                                       boolean refreshStats) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();

            Stage dialogStage = configureDialogStage(root, title, width, height, refreshStats);

            System.out.println("📋 About to show dialog: " + title);
            dialogStage.showAndWait();
        } catch (Exception e) {
            System.err.println("Error opening dialog (" + title + "): " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open " + title + " dialog");
        }
    }

    private Stage configureDialogStage(Parent root,
                                       String title,
                                       double width,
                                       double height,
                                       boolean refreshStats) {
        Stage dialogStage = new Stage();
        Scene scene = new Scene(root, width, height);
        dialogStage.setScene(scene);
        dialogStage.setTitle(title);
        dialogStage.setResizable(false);

        Window owner = getOwnerWindow();
        if (owner != null) {
            dialogStage.initOwner(owner);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            System.out.println("✅ Dialog owner set for " + title);
        } else {
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            System.out.println("⚠️ Dialog owner not found, using application modal for " + title);
        }

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE),
                dialogStage::close
        );

        dialogStage.setOnShown(e -> Platform.runLater(() -> {
            Node firstInput = findFirstInputNode(scene);
            if (firstInput != null) {
                firstInput.requestFocus();
            }
        }));

        if (refreshStats) {
            dialogStage.setOnHidden(e -> loadStatistics());
        }

        dialogStage.centerOnScreen();
        return dialogStage;
    }

    private Node findFirstInputNode(Scene scene) {
        Node node = scene.lookup(".text-field");
        if (node == null) {
            node = scene.lookup(".combo-box");
        }
        if (node == null) {
            node = scene.lookup(".table-view");
        }
        return node;
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