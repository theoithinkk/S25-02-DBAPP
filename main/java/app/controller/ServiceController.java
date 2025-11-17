package app.controller;

import app.dao.HealthServiceDAO;
import app.model.HealthService;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ServiceController {

    @FXML private TableView<HealthService> tableServices;
    @FXML private TableColumn<HealthService, Integer> colId;
    @FXML private TableColumn<HealthService, String> colType;
    @FXML private TableColumn<HealthService, String> colDescription;
    @FXML private TableColumn<HealthService, Double> colFee;
    @FXML private TableColumn<HealthService, String> colRemarks;

    @FXML private TextField txtServiceType;
    @FXML private TextField txtFee;
    @FXML private TextField txtRemarks;
    @FXML private TextArea txtDescription;

    @FXML private Label lblServiceCount;
    @FXML private Label lblNextId;
    @FXML private Button btnDelete;

    private final HealthServiceDAO serviceDAO = new HealthServiceDAO();

    @FXML
    private void initialize() {
        // Setup table columns
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getServiceId()));
        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colDescription.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        colFee.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFee()));
        colRemarks.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRemarks()));

        // Configure permissions
        if (btnDelete != null && !SessionManager.canDelete()) {
            btnDelete.setDisable(true);
            btnDelete.setOpacity(0.5);
            btnDelete.setTooltip(new Tooltip("Only Admins can delete services"));
        }

        refreshServices();

        // Handle row selection
        tableServices.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtServiceType.setText(newSel.getServiceType());
                txtDescription.setText(newSel.getDescription());
                txtFee.setText(String.valueOf(newSel.getFee()));
                txtRemarks.setText(newSel.getRemarks());
            }
        });

        // Update next ID display
        updateNextIdDisplay();
    }

    @FXML
    private void addService() {
        // Validate required fields
        if (txtServiceType.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Service type is required.");
            return;
        }

        try {
            // Find the next available ID (fills gaps)
            int nextId = findNextAvailableId();

            HealthService service = new HealthService();
            service.setServiceId(nextId); // Set the ID to fill gap
            service.setServiceType(txtServiceType.getText().trim());
            service.setDescription(txtDescription.getText().trim());
            service.setFee(txtFee.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtFee.getText().trim()));
            service.setRemarks(txtRemarks.getText().trim());

            if (serviceDAO.addServiceWithId(service)) {
                // Log action
                if (SessionManager.isLoggedIn()) {
                    serviceDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                            "ADD_SERVICE: " + service.getServiceType() + " (ID: " + nextId + ")");
                }

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Service added successfully with ID: " + nextId);
                refreshServices();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add service.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid fee amount.");
        }
    }

    @FXML
    private void updateService() {
        HealthService selected = tableServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a service to update.");
            return;
        }

        try {
            selected.setServiceType(txtServiceType.getText().trim());
            selected.setDescription(txtDescription.getText().trim());
            selected.setFee(Double.parseDouble(txtFee.getText().trim()));
            selected.setRemarks(txtRemarks.getText().trim());

            if (serviceDAO.updateService(selected)) {
                // Log action
                if (SessionManager.isLoggedIn()) {
                    serviceDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                            "UPDATE_SERVICE: " + selected.getServiceId());
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Service updated successfully!");
                refreshServices();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update service.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid fee amount.");
        }
    }

    @FXML
    private void deleteService() {
        // Double-check permission
        if (!SessionManager.canDelete()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Admins can delete services.");
            return;
        }

        HealthService selected = tableServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a service to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete: " + selected.getServiceType() + "?\n\n" +
                        "Note: The ID " + selected.getServiceId() + " will be available for reuse.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Health Service");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (serviceDAO.deleteService(selected.getServiceId())) {
                    // Log action
                    if (SessionManager.isLoggedIn()) {
                        serviceDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                                "DELETE_SERVICE: " + selected.getServiceId() + " (" + selected.getServiceType() + ")");
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Deleted",
                            "Service deleted successfully!\nID " + selected.getServiceId() + " is now available for reuse.");
                    refreshServices();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete service.");
                }
            }
        });
    }

    @FXML
    private void refreshServices() {
        List<HealthService> list = serviceDAO.getAllServices();
        ObservableList<HealthService> obsList = FXCollections.observableArrayList(list);
        tableServices.setItems(obsList);

        // Update count label
        if (lblServiceCount != null) {
            lblServiceCount.setText(list.size() + " service" + (list.size() != 1 ? "s" : ""));
        }

        // Update next ID display
        updateNextIdDisplay();

        if (list.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data",
                    "No services found. Click 'Add Service' to create your first service.");
        }
    }

    @FXML
    private void clearFields() {
        txtServiceType.clear();
        txtDescription.clear();
        txtFee.clear();
        txtRemarks.clear();
        tableServices.getSelectionModel().clearSelection();
    }

    /**
     * Finds the next available ID by checking for gaps in the sequence
     */
    private int findNextAvailableId() {
        List<HealthService> services = serviceDAO.getAllServices();

        if (services.isEmpty()) {
            return 1; // Start from 1 if no services exist
        }

        // Create a sorted list of existing IDs
        List<Integer> existingIds = services.stream()
                .map(HealthService::getServiceId)
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        // Find the first gap in the sequence
        int expectedId = 1;
        for (Integer id : existingIds) {
            if (id > expectedId) {
                return expectedId; // Found a gap!
            }
            expectedId = id + 1;
        }

        // No gaps found, return next sequential ID
        return expectedId;
    }

    /**
     * Updates the "Next ID" label to show which ID will be used next
     */
    private void updateNextIdDisplay() {
        if (lblNextId != null) {
            int nextId = findNextAvailableId();
            lblNextId.setText("Next ID: " + nextId);
            lblNextId.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11; -fx-font-weight: bold;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}