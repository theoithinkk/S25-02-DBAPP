package app.controller;

import app.dao.ResidentDAO;
import app.model.Resident;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.stream.Collectors;

import java.util.List;

public class ResidentController {

    @FXML private TableView<Resident> tableResidents;
    @FXML private TableColumn<Resident, Integer> colId;
    @FXML private TableColumn<Resident, String> colFirstName;
    @FXML private TableColumn<Resident, String> colLastName;
    @FXML private TableColumn<Resident, Integer> colAge;
    @FXML private TableColumn<Resident, String> colSex;
    @FXML private TableColumn<Resident, String> colAddress;
    @FXML private TableColumn<Resident, String> colContact;
    @FXML private TableColumn<Resident, Integer> colHousehold;
    @FXML private TableColumn<Resident, String> colVulnerability;

    @FXML private TextField txtFirstName, txtLastName, txtAge, txtContact, txtAddress, txtHouseholdId;
    @FXML private ComboBox<String> comboSex;
    @FXML private ComboBox<String> comboVulnerability;
    @FXML private Label lblRecordCount;
    @FXML private Label lblHousehold;
    @FXML private Label lblVulnerability;
    @FXML private Button btnDelete;
    @FXML private TextField txtSearch;

    private final ResidentDAO residentDAO = new ResidentDAO();

    @FXML
    private void initialize() {
        System.out.println("=== ResidentController Initialize ===");

        // Configure permissions and visibility
        configurePermissions();

        // Setup ComboBoxes
        setupComboBoxes();

        // Set up table columns
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getResidentId()));
        colFirstName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        colLastName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        colAge.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAge()));
        colSex.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getSex()));
        colAddress.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAddress()));
        colContact.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getContactNumber()));
        colHousehold.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getHouseholdId()));
        colVulnerability.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getVulnerabilityStatus()));

        // Add input validation
        addInputValidation();

        refreshResidents();

        tableResidents.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtFirstName.setText(newSel.getFirstName());
                txtLastName.setText(newSel.getLastName());
                txtAge.setText(String.valueOf(newSel.getAge()));
                comboSex.setValue(newSel.getSex());
                txtContact.setText(newSel.getContactNumber());
                txtAddress.setText(newSel.getAddress());

                if (newSel.getHouseholdId() != null) {
                    txtHouseholdId.setText(String.valueOf(newSel.getHouseholdId()));
                } else {
                    txtHouseholdId.setText("");
                }

                comboVulnerability.setValue(newSel.getVulnerabilityStatus() != null ?
                        newSel.getVulnerabilityStatus() : "None");
            }
        });
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterResidents(newValue);
            });
        }
    }

    private void setupComboBoxes() {
        // Sex dropdown
        comboSex.setItems(FXCollections.observableArrayList("M", "F"));

        // Vulnerability dropdown
        comboVulnerability.setItems(FXCollections.observableArrayList(
                "None", "Senior Citizen", "PWD", "Pregnant", "Child", "Diabetic",
                "Hypertensive", "Immunocompromised", "Other"
        ));
        comboVulnerability.setValue("None");
    }

    private void addInputValidation() {
        // Age: Numbers only, max 3 digits
        txtAge.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtAge.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 3) {
                txtAge.setText(oldValue);
            }
        });

        // Contact: Numbers only, max 11 digits
        txtContact.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtContact.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 11) {
                txtContact.setText(oldValue);
            }
        });

        // Household ID: Numbers only
        txtHouseholdId.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtHouseholdId.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Name fields: Letters, spaces, hyphens only
        addNameValidation(txtFirstName);
        addNameValidation(txtLastName);
    }

    private void addNameValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s\\-]*")) {
                field.setText(newValue.replaceAll("[^a-zA-Z\\s\\-]", ""));
            }
        });
    }

    private void configurePermissions() {
        // Hide sensitive columns for Staff
        boolean showSensitiveData = SessionManager.isAdmin() || SessionManager.isPersonnel();

        colHousehold.setVisible(showSensitiveData);
        colVulnerability.setVisible(showSensitiveData);

        if (txtHouseholdId != null) txtHouseholdId.setVisible(showSensitiveData);
        if (lblHousehold != null) lblHousehold.setVisible(showSensitiveData);
        if (comboVulnerability != null) comboVulnerability.setVisible(showSensitiveData);
        if (lblVulnerability != null) lblVulnerability.setVisible(showSensitiveData);

        // Hide delete button for Personnel and Staff (only Admin can delete residents)
        if (btnDelete != null) {
            boolean canDelete = SessionManager.getCurrentUser() != null &&
                    SessionManager.getCurrentUser().canDeleteRecords();
            btnDelete.setVisible(canDelete);
            btnDelete.setManaged(canDelete);
        }
    }

    @FXML
    private void addResident() {
        // Validate required fields
        if (!validateInput()) {
            return;
        }

        try {
            Resident r = new Resident();
            r.setFirstName(txtFirstName.getText().trim());
            r.setLastName(txtLastName.getText().trim());
            r.setAge(Integer.parseInt(txtAge.getText().trim()));
            r.setSex(comboSex.getValue());
            r.setContactNumber(txtContact.getText().trim());
            r.setAddress(txtAddress.getText().trim());

            // Handle optional fields
            if (!txtHouseholdId.getText().trim().isEmpty()) {
                r.setHouseholdId(Integer.parseInt(txtHouseholdId.getText().trim()));
            }

            String vulnerability = comboVulnerability.getValue();
            if (vulnerability != null && !vulnerability.equals("None")) {
                r.setVulnerabilityStatus(vulnerability);
            }

            if (residentDAO.addResident(r)) {
                if (SessionManager.isLoggedIn()) {
                    residentDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                            "ADD_RESIDENT: " + r.getFirstName() + " " + r.getLastName());
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Resident added successfully!");
                refreshResidents();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add resident to database.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Please enter valid numbers for Age and Household ID.");
        }
    }

    @FXML
    private void updateResident() {
        Resident selected = tableResidents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a resident from the table to update.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selected.setFirstName(txtFirstName.getText().trim());
            selected.setLastName(txtLastName.getText().trim());
            selected.setAge(Integer.parseInt(txtAge.getText().trim()));
            selected.setSex(comboSex.getValue());
            selected.setContactNumber(txtContact.getText().trim());
            selected.setAddress(txtAddress.getText().trim());

            if (!txtHouseholdId.getText().trim().isEmpty()) {
                selected.setHouseholdId(Integer.parseInt(txtHouseholdId.getText().trim()));
            } else {
                selected.setHouseholdId(null);
            }

            String vulnerability = comboVulnerability.getValue();
            selected.setVulnerabilityStatus(
                    (vulnerability != null && !vulnerability.equals("None")) ? vulnerability : null
            );

            if (residentDAO.updateResident(selected)) {
                if (SessionManager.isLoggedIn()) {
                    residentDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                            "UPDATE_RESIDENT: " + selected.getResidentId());
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Resident updated successfully!");
                refreshResidents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update resident.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Please enter valid numbers for Age and Household ID.");
        }
    }

    @FXML
    private void deleteResident() {
        if (!SessionManager.canDelete()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied",
                    "Only Admins can delete records.");
            return;
        }

        Resident selected = tableResidents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a resident from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selected.getFirstName() + " " +
                        selected.getLastName() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Resident");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (residentDAO.deleteResident(selected.getResidentId())) {
                    if (SessionManager.isLoggedIn()) {
                        residentDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                                "DELETE_RESIDENT: " + selected.getResidentId());
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Resident deleted successfully!");
                    refreshResidents();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete resident.");
                }
            }
        });
    }

    @FXML
    private void refreshResidents() {
        try {
            List<Resident> list = residentDAO.getAllResidents();
            ObservableList<Resident> obsList = FXCollections.observableArrayList(list);
            tableResidents.setItems(obsList);

            // Update count
            if (lblRecordCount != null) {
                lblRecordCount.setText(list.size() + " resident" + (list.size() != 1 ? "s" : ""));
            }

            if (list.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Data",
                        "No residents found in the database. Click 'Add Resident' to create your first record.");
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR refreshing residents: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not load residents from database: " + e.getMessage());
        }
    }

    @FXML
    private void filterResidents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshResidents();
            return;
        }

        String search = searchText.trim().toLowerCase();
        List<Resident> allResidents = residentDAO.getAllResidents();
        List<Resident> filtered = allResidents.stream()
                .filter(r ->
                        r.getFirstName().toLowerCase().contains(search) ||
                                r.getLastName().toLowerCase().contains(search) ||
                                (r.getFirstName() + " " + r.getLastName()).toLowerCase().contains(search) ||
                                (r.getAddress() != null && r.getAddress().toLowerCase().contains(search)))
                .collect(Collectors.toList());

        tableResidents.setItems(FXCollections.observableArrayList(filtered));

        if (lblRecordCount != null) {
            lblRecordCount.setText(filtered.size() + " resident" + (filtered.size() != 1 ? "s" : ""));
        }
    }

    @FXML
    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtAge.clear();
        comboSex.setValue(null);
        txtContact.clear();
        txtAddress.clear();
        txtHouseholdId.clear();
        comboVulnerability.setValue("None");
        tableResidents.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("• First name is required\n");
        }
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("• Last name is required\n");
        }
        if (txtAge.getText().trim().isEmpty()) {
            errors.append("• Age is required\n");
        } else {
            try {
                int age = Integer.parseInt(txtAge.getText().trim());
                if (age < 0 || age > 150) {
                    errors.append("• Age must be between 0 and 150\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Age must be a valid number\n");
            }
        }
        if (comboSex.getValue() == null || comboSex.getValue().isEmpty()) {
            errors.append("• Sex is required\n");
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