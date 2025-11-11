package app.controller;

import app.dao.ServiceTransactionDAO;
import app.dao.ResidentDAO;
import app.dao.HealthPersonnelDAO;
import app.dao.HealthServiceDAO;
import app.model.ServiceTransaction;
import app.model.Resident;
import app.model.HealthPersonnel;
import app.model.HealthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * TransactionController
 * Handles service transactions that link residents, personnel, and services.
 */
public class TransactionController {

    @FXML private TableView<ServiceTransaction> tableTransactions;
    @FXML private TableColumn<ServiceTransaction, Integer> colId;
    @FXML private TableColumn<ServiceTransaction, String> colResident;
    @FXML private TableColumn<ServiceTransaction, String> colService;
    @FXML private TableColumn<ServiceTransaction, String> colPersonnel;
    @FXML private TableColumn<ServiceTransaction, Date> colDate;
    @FXML private TableColumn<ServiceTransaction, String> colRemarks;

    @FXML private ComboBox<Resident> comboResident;
    @FXML private ComboBox<HealthService> comboService;
    @FXML private ComboBox<HealthPersonnel> comboPersonnel;
    @FXML private TextField txtRemarks;
    @FXML private DatePicker dateProvided;

    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final HealthServiceDAO serviceDAO = new HealthServiceDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        // Table setup
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTransactionId()));
        colResident.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getResidentName()));
        colService.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colPersonnel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersonnelName()));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateProvided()));
        colRemarks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRemarks()));

        // Load data
        refreshTransactions();
        loadDropdowns();

        // Default date to today
        dateProvided.setValue(LocalDate.now());
    }

    /** ADD NEW TRANSACTION **/
    @FXML
    private void addTransaction() {
        Resident resident = comboResident.getValue();
        HealthService service = comboService.getValue();
        HealthPersonnel personnel = comboPersonnel.getValue();

        if (resident == null || service == null || personnel == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select resident, service, and personnel.");
            return;
        }

        ServiceTransaction t = new ServiceTransaction();
        t.setResidentId(resident.getResidentId());
        t.setServiceId(service.getServiceId());
        t.setPersonnelId(personnel.getPersonnelId());
        t.setDateProvided(Date.valueOf(dateProvided.getValue()));
        t.setRemarks(txtRemarks.getText());

        if (transactionDAO.addTransaction(t)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Transaction added successfully!");
            refreshTransactions();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add transaction.");
        }
    }

    /** DELETE TRANSACTION **/
    @FXML
    private void deleteTransaction() {
        ServiceTransaction selected = tableTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a transaction to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this transaction?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && transactionDAO.deleteTransaction(selected.getTransactionId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Transaction deleted successfully!");
                refreshTransactions();
            }
        });
    }

    /** REFRESH TABLE **/
    @FXML
    private void refreshTransactions() {
        List<ServiceTransaction> list = transactionDAO.getAllTransactions();
        ObservableList<ServiceTransaction> obsList = FXCollections.observableArrayList(list);
        tableTransactions.setItems(obsList);
    }

    /** LOAD DROPDOWN DATA **/
    private void loadDropdowns() {
        comboResident.setItems(FXCollections.observableArrayList(residentDAO.getAllResidents()));
        comboService.setItems(FXCollections.observableArrayList(serviceDAO.getAllServices()));
        comboPersonnel.setItems(FXCollections.observableArrayList(personnelDAO.getAllPersonnel()));

        comboResident.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Resident r) { return r == null ? "" : r.getFirstName() + " " + r.getLastName(); }
            public Resident fromString(String s) { return null; }
        });

        comboService.setConverter(new javafx.util.StringConverter<>() {
            public String toString(HealthService s) { return s == null ? "" : s.getServiceType(); }
            public HealthService fromString(String s) { return null; }
        });

        comboPersonnel.setConverter(new javafx.util.StringConverter<>() {
            public String toString(HealthPersonnel p) { return p == null ? "" : p.getFirstName() + " " + p.getLastName(); }
            public HealthPersonnel fromString(String s) { return null; }
        });
    }

    /** CLEAR FORM **/
    private void clearFields() {
        comboResident.setValue(null);
        comboService.setValue(null);
        comboPersonnel.setValue(null);
        txtRemarks.clear();
        dateProvided.setValue(LocalDate.now());
    }

    /** ALERT HELPER **/
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
