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
            ScrollPane root = loader.load(); // Changed from VBox to ScrollPane

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Avail Health Service");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root, 650, 750)); // Increased size for scrolling
            dialogStage.setResizable(false);

            // Refresh statistics when dialog closes
            dialogStage.setOnHidden(e -> loadStatistics());

            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Error opening avail service dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open transaction dialog");
        }
    }

    @FXML
    private void handleIssueMedicalSupplies() {
        showAlert("Coming Soon", "Issuing Medical Supplies feature will be implemented soon.");
    }

    @FXML
    private void handleRestockInventory() {
        showAlert("Coming Soon", "Restocking Inventory feature will be implemented soon.");
    }

    @FXML
    private void handleLogClinicVisit() {
        try {
            Parent clinicVisitsRoot = FXMLLoader.load(getClass().getResource("/view/clinicvisits.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Log Clinic Visit");
            stage.setScene(new Scene(clinicVisitsRoot, 700, 750)); // Increased height to 750
            stage.setResizable(false); // Changed to true so user can resize if needed
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load clinic visits form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}