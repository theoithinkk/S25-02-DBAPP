package app.controller;

import app.dao.HealthPersonnelDAO;
import app.model.HealthPersonnel;
import app.model.User;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.stream.Collectors;

import java.util.List;

public class PersonnelController {

    @FXML private TableView<HealthPersonnel> tablePersonnel;
    @FXML private TableColumn<HealthPersonnel, Integer> colId;
    @FXML private TableColumn<HealthPersonnel, String> colFirstName;
    @FXML private TableColumn<HealthPersonnel, String> colLastName;
    @FXML private TableColumn<HealthPersonnel, String> colRole;
    @FXML private TableColumn<HealthPersonnel, String> colSpecialization;
    @FXML private TableColumn<HealthPersonnel, String> colContact;

    @FXML private TextField txtFirstName, txtLastName, txtRole, txtSpecialization, txtContact;
    @FXML private Label lblRecordCount;
    @FXML private Button btnDelete;
    @FXML private TextField txtSearch;

    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        System.out.println("=== PersonnelController Initialize ===");

        // Configure permissions and visibility
        configurePermissions();

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPersonnelId()));
        colFirstName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        colLastName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        colSpecialization.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialization()));
        colContact.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContactNumber()));

        // Add input validation
        addInputValidation();

        refreshPersonnel();

        tablePersonnel.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtFirstName.setText(newSel.getFirstName());
                txtLastName.setText(newSel.getLastName());
                txtRole.setText(newSel.getRole());
                txtSpecialization.setText(newSel.getSpecialization());
                txtContact.setText(newSel.getContactNumber());
            }
        });
        // Setup live search
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterPersonnel(newValue);
            });
        }
    }

    private void addInputValidation() {
        // Contact: Numbers only, max 11 digits
        txtContact.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtContact.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 11) {
                txtContact.setText(oldValue);
            }
        });

        // Name/role/specialization fields: Letters, spaces, hyphens only
        addTextValidation(txtFirstName);
        addTextValidation(txtLastName);
        addTextValidation(txtRole);
        addTextValidation(txtSpecialization);
    }

    private void addTextValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s\\-]*")) {
                field.setText(newValue.replaceAll("[^a-zA-Z\\s\\-]", ""));
            }
        });
    }

    private void configurePermissions() {
        // Hide delete button for Personnel and Staff (only Admin can delete personnel)
        if (btnDelete != null) {
            boolean canDelete = SessionManager.getCurrentUser() != null &&
                    SessionManager.getCurrentUser().canDeleteRecords();
            btnDelete.setVisible(canDelete);
            btnDelete.setManaged(canDelete);
        }
    }

    @FXML
    private void addPersonnel() {
        if (!validateInput()) {
            return;
        }

        HealthPersonnel p = new HealthPersonnel();
        p.setFirstName(txtFirstName.getText().trim());
        p.setLastName(txtLastName.getText().trim());
        p.setRole(txtRole.getText().trim());
        p.setSpecialization(txtSpecialization.getText().trim());
        p.setContactNumber(txtContact.getText().trim());

        if (personnelDAO.addPersonnel(p)) {
            if (SessionManager.isLoggedIn()) {
                personnelDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                        "ADD_PERSONNEL: " + p.getFirstName() + " " + p.getLastName());
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Personnel added successfully!");
            refreshPersonnel();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add personnel to database.");
        }
    }

    @FXML
    private void updatePersonnel() {
        HealthPersonnel selected = tablePersonnel.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a personnel from the table to update.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        selected.setFirstName(txtFirstName.getText().trim());
        selected.setLastName(txtLastName.getText().trim());
        selected.setRole(txtRole.getText().trim());
        selected.setSpecialization(txtSpecialization.getText().trim());
        selected.setContactNumber(txtContact.getText().trim());

        if (personnelDAO.updatePersonnel(selected)) {
            if (SessionManager.isLoggedIn()) {
                personnelDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                        "UPDATE_PERSONNEL: " + selected.getPersonnelId());
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Personnel updated successfully!");
            refreshPersonnel();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update personnel.");
        }
    }

    @FXML
    private void deletePersonnel() {
        // Double-check permission
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.canDeleteRecords()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Admins can delete personnel.");
            return;
        }

        HealthPersonnel selected = tablePersonnel.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a personnel to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this personnel?", ButtonType.OK, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (personnelDAO.deletePersonnel(selected.getPersonnelId())) {
                    if (SessionManager.isLoggedIn()) {
                        personnelDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                                "DELETE_PERSONNEL: " + selected.getPersonnelId());
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Personnel deleted successfully!");
                    refreshPersonnel();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete Personnel.");
                }
            }
        });
    }

    @FXML
    private void filterPersonnel(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshPersonnel();
            return;
        }

        String search = searchText.trim().toLowerCase();
        List<HealthPersonnel> allPersonnel = personnelDAO.getAllPersonnel();
        List<HealthPersonnel> filtered = allPersonnel.stream()
                .filter(p ->
                        p.getFirstName().toLowerCase().contains(search) ||
                                p.getLastName().toLowerCase().contains(search) ||
                                (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(search) ||
                                p.getRole().toLowerCase().contains(search) ||
                                (p.getSpecialization() != null && p.getSpecialization().toLowerCase().contains(search)))
                .collect(java.util.stream.Collectors.toList());

        tablePersonnel.setItems(FXCollections.observableArrayList(filtered));

        if (lblRecordCount != null) {
            lblRecordCount.setText(filtered.size() + " personnel" + (filtered.size() != 1 ? "s" : ""));
        }
    }

    @FXML
    private void refreshPersonnel() {
        try {
            List<HealthPersonnel> list = personnelDAO.getAllPersonnel();
            ObservableList<HealthPersonnel> obsList = FXCollections.observableArrayList(list);
            tablePersonnel.setItems(obsList);

            // Update count
            if (lblRecordCount != null) {
                lblRecordCount.setText(list.size() + " personnel" + (list.size() != 1 ? "s" : ""));
            }

            if (list.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Data",
                        "No health personnel found in the database. Click 'Add Personnel' to create your first record.");
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR refreshing health personnel: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not load health personnel from database: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtRole.clear();
        txtSpecialization.clear();
        txtContact.clear();
        tablePersonnel.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("• First name is required\n");
        }
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("• Last name is required\n");
        }
        if (txtRole.getText().trim().isEmpty()) {
            errors.append("• Role is required\n");
        }
        if (txtSpecialization.getText() == null || txtSpecialization.getText().trim().isEmpty()) {
            errors.append("• Specialization is required\n");
        }
        if (!txtContact.getText().trim().isEmpty() && txtContact.getText().trim().length() != 11) {
            errors.append("• Contact number must be exactly 11 digits\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please fix the following:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}