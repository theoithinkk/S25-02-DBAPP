package app.controller;

import app.dao.HealthServiceDAO;
import app.model.HealthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Handles CRUD for Health Services.
 * Connected to services.fxml
 */
public class ServiceController {

    @FXML private TableView<HealthService> tableServices;
    @FXML private TableColumn<HealthService, Integer> colId;
    @FXML private TableColumn<HealthService, String> colType;
    @FXML private TableColumn<HealthService, Double> colFee;
    @FXML private TableColumn<HealthService, String> colRemarks;

    @FXML private TextField txtServiceType, txtFee, txtRemarks;

    private final HealthServiceDAO serviceDAO = new HealthServiceDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getServiceId()));
        colType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colFee.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFee()));
        colRemarks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRemarks()));

        refreshServices();

        tableServices.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtServiceType.setText(newSel.getServiceType());
                txtFee.setText(String.valueOf(newSel.getFee()));
                txtRemarks.setText(newSel.getRemarks());
            }
        });
    }

    @FXML
    private void addService() {
        if (txtServiceType.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Service type is required.");
            return;
        }

        HealthService s = new HealthService();
        s.setServiceType(txtServiceType.getText());
        s.setFee(Double.parseDouble(txtFee.getText().isEmpty() ? "0" : txtFee.getText()));
        s.setRemarks(txtRemarks.getText());

        if (serviceDAO.addService(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Service added successfully!");
            refreshServices();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add service.");
        }
    }

    @FXML
    private void updateService() {
        HealthService selected = tableServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a service to update.");
            return;
        }

        selected.setServiceType(txtServiceType.getText());
        selected.setFee(Double.parseDouble(txtFee.getText()));
        selected.setRemarks(txtRemarks.getText());

        if (serviceDAO.updateService(selected)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Service updated successfully!");
            refreshServices();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update service.");
        }
    }

    @FXML
    private void deleteService() {
        HealthService selected = tableServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a service to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this service?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && serviceDAO.deleteService(selected.getServiceId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Service deleted successfully!");
                refreshServices();
                clearFields();
            }
        });
    }

    @FXML
    private void refreshServices() {
        List<HealthService> list = serviceDAO.getAllServices();
        ObservableList<HealthService> obsList = FXCollections.observableArrayList(list);
        tableServices.setItems(obsList);
    }

    private void clearFields() {
        txtServiceType.clear();
        txtFee.clear();
        txtRemarks.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
