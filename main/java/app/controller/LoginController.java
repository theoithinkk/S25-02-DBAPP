package app.controller;

import app.dao.UserDAO;
import app.model.User;
import app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        // Allow Enter key to submit login
        txtPassword.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Attempt login
        User user = userDAO.login(username, password);

        if (user != null) {
            // Store logged-in user in session
            SessionManager.setCurrentUser(user);

            System.out.println("✓ Login successful: " + user.getUsername() + " (" + user.getRole() + ")");

            // Load main application window
            loadMainWindow();

        } else {
            showError("Invalid username or password");
        }
    }

    @FXML
    private void handleCreateAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/create_account.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New Account");
            stage.setScene(new Scene(root, 450, 600));
            stage.setResizable(false);

            // Show create account window
            stage.show();

        } catch (Exception e) {
            System.err.println("✗ Error loading create account page: " + e.getMessage());
            e.printStackTrace();
            showError("Could not open create account page");
        }
    }

    private void loadMainWindow() {
        try {
            // Load main.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Parent root = loader.load();

            // Get the controller and pass user info
            MainController mainController = loader.getController();
            mainController.initializeWithUser(SessionManager.getCurrentUser());

            // Create new stage
            Stage stage = new Stage();
            stage.setTitle("Barangay Health Services Management System - " +
                    SessionManager.getCurrentUser().getUsername());
            stage.setScene(new Scene(root, 1000, 600));
            stage.setMaximized(false);

            // Close login window
            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

            // Show main window
            stage.show();

            // Handle logout when main window closes
            stage.setOnCloseRequest(event -> {
                SessionManager.clearSession();
                System.out.println("✓ User logged out");
            });

        } catch (Exception e) {
            System.err.println("✗ Error loading main window: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading application");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);

        // Hide error after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> lblError.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}