package app.controller;

import app.dao.HealthPersonnelDAO;
import app.model.HealthPersonnel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PersonnelController {

    @FXML private TableView<HealthPersonnel> tablePersonnel;
    @FXML private TableColumn<HealthPersonnel, Integer> colId;
    @FXML private TableColumn<HealthPersonnel, String> colName;
    @FXML private TableColumn<HealthPersonnel, String> colRole;
    @FXML private TableColumn<HealthPersonnel, String> colSpec;

    @FXML private TextField txtFirstName, txtLastName, txtRole, txtSpecialization, txtContact;

    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPersonnelId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        colSpec.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialization()));

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
    }

    @FXML
    private void addPersonnel() {
        HealthPersonnel p = new HealthPersonnel();
        p.setFirstName(txtFirstName.getText());
        p.setLastName(txtLastName.getText());
        p.setRole(txtRole.getText());
        p.setSpecialization(txtSpecialization.getText());
        p.setContactNumber(txtContact.getText());

        if (personnelDAO.addPersonnel(p)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Personnel added successfully!");
            refreshPersonnel();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add personnel.");
        }
    }

    @FXML
    private void updatePersonnel() {
        HealthPersonnel selected = tablePersonnel.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a personnel to update.");
            return;
        }

        selected.setFirstName(txtFirstName.getText());
        selected.setLastName(txtLastName.getText());
        selected.setRole(txtRole.getText());
        selected.setSpecialization(txtSpecialization.getText());
        selected.setContactNumber(txtContact.getText());

        if (personnelDAO.updatePersonnel(selected)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Personnel updated successfully!");
            refreshPersonnel();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update personnel.");
        }
    }

    @FXML
    private void deletePersonnel() {
        HealthPersonnel selected = tablePersonnel.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a personnel to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this personnel?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && personnelDAO.deletePersonnel(selected.getPersonnelId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Personnel deleted successfully!");
                refreshPersonnel();
                clearFields();
            }
        });
    }

    @FXML
    private void refreshPersonnel() {
        List<HealthPersonnel> list = personnelDAO.getAllPersonnel();
        ObservableList<HealthPersonnel> obsList = FXCollections.observableArrayList(list);
        tablePersonnel.setItems(obsList);
    }

    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtRole.clear();
        txtSpecialization.clear();
        txtContact.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
