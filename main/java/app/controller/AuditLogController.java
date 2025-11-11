package app.controller;

import app.dao.UserDAO;
import app.model.User;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AuditLogController {

    @FXML private StackPane contentPane;

    @FXML
    private void initialize() {
        // Load audit log by default
        showAuditLog();
    }

    @FXML
    private void showAuditLog() {
        try {
            VBox auditView = createAuditLogView();
            contentPane.getChildren().setAll(auditView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserAccounts() {
        try {
            VBox userView = createUserAccountsView();
            contentPane.getChildren().setAll(userView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createAuditLogView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(15));

        Label title = new Label("Audit Log - Recent Activity");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        TableView<AuditLogEntry> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<AuditLogEntry, Integer> colId = new TableColumn<>("Log ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLogId()));
        colId.setPrefWidth(60);

        TableColumn<AuditLogEntry, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colUsername.setPrefWidth(120);

        TableColumn<AuditLogEntry, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAction()));
        colAction.setPrefWidth(400);

        TableColumn<AuditLogEntry, Timestamp> colTimestamp = new TableColumn<>("Timestamp");
        colTimestamp.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTimestamp()));
        colTimestamp.setPrefWidth(180);

        table.getColumns().addAll(colId, colUsername, colAction, colTimestamp);

        // Load audit log data
        loadAuditLog(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadAuditLog(table));

        vbox.getChildren().addAll(title, table, btnRefresh);
        return vbox;
    }

    private VBox createUserAccountsView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(15));

        Label title = new Label("User Accounts Management");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        TableView<User> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<User, Integer> colId = new TableColumn<>("User ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getUserId()));
        colId.setPrefWidth(80);

        TableColumn<User, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colUsername.setPrefWidth(150);

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        colRole.setPrefWidth(100);

        TableColumn<User, Integer> colPersonnelId = new TableColumn<>("Personnel ID");
        colPersonnelId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPersonnelId()));
        colPersonnelId.setPrefWidth(100);

        table.getColumns().addAll(colId, colUsername, colRole, colPersonnelId);

        // Load users
        UserDAO userDAO = new UserDAO();
        table.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));

        // Buttons
        VBox buttonBox = new VBox(10);

        Button btnChangePassword = new Button("🔑 Change Password");
        btnChangePassword.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                changePassword(selected, table);
            } else {
                showAlert("Please select a user");
            }
        });

        Button btnDeleteUser = new Button("🗑️ Delete User");
        btnDeleteUser.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnDeleteUser.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteUser(selected, table);
            } else {
                showAlert("Please select a user");
            }
        });

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(userDAO.getAllUsers())));

        buttonBox.getChildren().addAll(btnChangePassword, btnDeleteUser, btnRefresh);

        vbox.getChildren().addAll(title, table, buttonBox);
        return vbox;
    }

    private void loadAuditLog(TableView<AuditLogEntry> table) {
        ObservableList<AuditLogEntry> logs = FXCollections.observableArrayList();

        String sql = "SELECT al.log_id, u.username, al.action, al.timestamp " +
                "FROM AuditLog al " +
                "LEFT JOIN Users u ON al.user_id = u.user_id " +
                "ORDER BY al.timestamp DESC LIMIT 100";

        try (Connection conn = app.dao.DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                AuditLogEntry entry = new AuditLogEntry(
                        rs.getInt("log_id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp")
                );
                logs.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        table.setItems(logs);
    }

    private void changePassword(User user, TableView<User> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change password for: " + user.getUsername());
        dialog.setContentText("New password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.length() < 6) {
                showAlert("Password must be at least 6 characters");
                return;
            }

            UserDAO userDAO = new UserDAO();
            if (userDAO.changePassword(user.getUserId(), newPassword)) {
                showAlert("Password changed successfully!");
            } else {
                showAlert("Failed to change password");
            }
        });
    }

    private void deleteUser(User user, TableView<User> table) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete user: " + user.getUsername() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserDAO userDAO = new UserDAO();
            if (userDAO.deleteUser(user.getUserId(), SessionManager.getCurrentUser().getUserId())) {
                showAlert("User deleted successfully");
                table.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));
            } else {
                showAlert("Failed to delete user. Cannot delete last admin.");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for audit log entries
    public static class AuditLogEntry {
        private int logId;
        private String username;
        private String action;
        private Timestamp timestamp;

        public AuditLogEntry(int logId, String username, String action, Timestamp timestamp) {
            this.logId = logId;
            this.username = username;
            this.action = action;
            this.timestamp = timestamp;
        }

        public int getLogId() { return logId; }
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public Timestamp getTimestamp() { return timestamp; }
    }
}