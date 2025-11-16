package app.controller;

import app.dao.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreateAccountController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private VBox personnelBox;
    @FXML private TextField txtPersonnelId;

    @FXML
    private void handleRoleChange() {
        String selectedRole = comboRole.getValue();
        boolean isPersonnel = "Personnel".equals(selectedRole);
        personnelBox.setVisible(isPersonnel);
        personnelBox.setManaged(isPersonnel);
    }

    @FXML
    private void handleCreateAccount() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();
        String role = comboRole.getValue();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert("Error", "All required fields must be filled out.", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.", Alert.AlertType.ERROR);
            return;
        }

        // If Personnel, check existence
        if ("Personnel".equals(role)) {
            String personnelIdText = txtPersonnelId.getText().trim();
            if (personnelIdText.isEmpty()) {
                showAlert("Error", "Please enter your Personnel ID.", Alert.AlertType.ERROR);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String query = "SELECT first_name, last_name FROM healthpersonnel WHERE personnel_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, personnelIdText);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    showAlert("Error", "No personnel found with ID " + personnelIdText, Alert.AlertType.ERROR);
                    return;
                }

                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Identity");
                confirm.setHeaderText("Confirm Personnel Identity");
                confirm.setContentText("Are you sure you are " + fullName + " (ID: " + personnelIdText + ")?");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Database Error", "Unable to verify Personnel ID.", Alert.AlertType.ERROR);
                return;
            }
        }

        // If passed all checks, create the account
        try (Connection conn = DBConnection.getConnection()) {
            String insert = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insert);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();

            showAlert("Success", "Account created successfully!", Alert.AlertType.INFORMATION);
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to create account.", Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        txtUsername.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        comboRole.setValue(null);
        txtPersonnelId.clear();
        personnelBox.setVisible(false);
        personnelBox.setManaged(false);
    }

    @FXML
    private void handleBack() {
        // Close the current "Create Account" window
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
