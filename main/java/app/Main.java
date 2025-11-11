package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("=== Starting Barangay Health Management System ===");

        try {
            // Load LOGIN page instead of main page
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 450, 550);  // Bigger login window
            stage.setTitle("Login - Barangay Health System");
            stage.setScene(scene);
            stage.setResizable(false); // Lock login window size
            stage.show();

            System.out.println("✓ Login page loaded successfully!");

        } catch (Exception e) {
            System.err.println("✗ Failed to start application!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}