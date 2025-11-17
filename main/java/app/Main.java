package app;

import app.util.BackgroundMusicPlayer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // COMMENT OUT music initialization for now
        // BackgroundMusicPlayer.initialize();

        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        primaryStage.setTitle("Barangay Health System");
        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up music player when app closes
        BackgroundMusicPlayer.dispose();
    }

    public static void main(String[] args) {
        launch(args);
    }
}