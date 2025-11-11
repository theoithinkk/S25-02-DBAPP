package app.controller;

import app.dao.*;
import app.model.*;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

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
    @FXML private DatePicker dateProvided;
    @FXML private TextField txtRemarks;

    @FXML private Label lblTransactionCount;
    @FXML private Button btnDelete;

    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final HealthServiceDAO serviceDAO = new HealthServiceDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        // Setup table columns
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTransactionId()));
        colResident.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getResidentName()));
        colService.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colPersonnel.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPersonnelName()));
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateProvided()));
        colRemarks.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRemarks()));

        // Configure permissions
        if (btnDelete != null && !SessionManager.canDelete()) {
            btnDelete.setDisable(true);
            btnDelete.setOpacity(0.5);
            btnDelete.setTooltip(new Tooltip("Only Admins can delete transactions"));
        }

        // Load data
        refreshTransactions();
        loadDropdowns();

        // Set default date to today
        dateProvided.setValue(LocalDate.now());
    }

    @FXML
    private void addTransaction() {
        // Validate required fields
        Resident resident = comboResident.getValue();
        HealthService service = comboService.getValue();
        HealthPersonnel personnel = comboPersonnel.getValue();

        if (resident == null || service == null || personnel == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select resident, service, and personnel.");
            return;
        }

        if (dateProvided.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a date.");
            return;
        }

        // Create transaction
        ServiceTransaction transaction = new ServiceTransaction();
        transaction.setResidentId(resident.getResidentId());
        transaction.setServiceId(service.getServiceId());
        transaction.setPersonnelId(personnel.getPersonnelId());
        transaction.setDateProvided(Date.valueOf(dateProvided.getValue()));
        transaction.setRemarks(txtRemarks.getText().trim());

        if (transactionDAO.addTransaction(transaction)) {
            // Log the action
            if (SessionManager.isLoggedIn()) {
                String action = String.format("ADD_TRANSACTION: %s availed %s by %s",
                        resident.getFirstName() + " " + resident.getLastName(),
                        service.getServiceType(),
                        personnel.getFirstName() + " " + personnel.getLastName());
                transactionDAO.logAction(SessionManager.getCurrentUser().getUserId(), action);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Transaction recorded successfully!\n\n" +
                            "Resident: " + resident.getFirstName() + " " + resident.getLastName() + "\n" +
                            "Service: " + service.getServiceType() + "\n" +
                            "Personnel: " + personnel.getFirstName() + " " + personnel.getLastName());
            refreshTransactions();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add transaction.");
        }
    }

    @FXML
    private void deleteTransaction() {
        // Double-check permission
        if (!SessionManager.canDelete()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied",
                    "Only Admins can delete transactions.");
            return;
        }

        ServiceTransaction selected = tableTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Select a transaction to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this transaction?\n\n" +
                        "Transaction ID: " + selected.getTransactionId() + "\n" +
                        "Resident: " + selected.getResidentName() + "\n" +
                        "Service: " + selected.getServiceType() + "\n" +
                        "Date: " + selected.getDateProvided(),
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Transaction");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (transactionDAO.deleteTransaction(selected.getTransactionId())) {
                    // Log the action
                    if (SessionManager.isLoggedIn()) {
                        transactionDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                                "DELETE_TRANSACTION: " + selected.getTransactionId());
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Deleted",
                            "Transaction deleted successfully!");
                    refreshTransactions();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete transaction.");
                }
            }
        });
    }

    @FXML
    private void refreshTransactions() {
        List<ServiceTransaction> list = transactionDAO.getAllTransactions();
        ObservableList<ServiceTransaction> obsList = FXCollections.observableArrayList(list);
        tableTransactions.setItems(obsList);

        // Update count label
        if (lblTransactionCount != null) {
            lblTransactionCount.setText(list.size() + " transaction" +
                    (list.size() != 1 ? "s" : ""));
        }

        if (list.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data",
                    "No transactions found. Add a new transaction to get started.");
        }
    }

    @FXML
    private void clearFields() {
        comboResident.setValue(null);
        comboService.setValue(null);
        comboPersonnel.setValue(null);
        txtRemarks.clear();
        dateProvided.setValue(LocalDate.now());
        tableTransactions.getSelectionModel().clearSelection();
    }

    /**
     * Load dropdown data
     */
    private void loadDropdowns() {
        // Load residents
        List<Resident> residents = residentDAO.getAllResidents();
        comboResident.setItems(FXCollections.observableArrayList(residents));
        comboResident.setConverter(new javafx.util.StringConverter<Resident>() {
            @Override
            public String toString(Resident r) {
                return r == null ? "" :
                        r.getFirstName() + " " + r.getLastName() + " (ID: " + r.getResidentId() + ")";
            }

            @Override
            public Resident fromString(String string) {
                return null;
            }
        });

        // Load services
        List<HealthService> services = serviceDAO.getAllServices();
        comboService.setItems(FXCollections.observableArrayList(services));
        comboService.setConverter(new javafx.util.StringConverter<HealthService>() {
            @Override
            public String toString(HealthService s) {
                return s == null ? "" :
                        s.getServiceType() + " - ₱" + String.format("%.2f", s.getFee());
            }

            @Override
            public HealthService fromString(String string) {
                return null;
            }
        });

        // Load personnel
        List<HealthPersonnel> personnel = personnelDAO.getAllPersonnel();
        comboPersonnel.setItems(FXCollections.observableArrayList(personnel));
        comboPersonnel.setConverter(new javafx.util.StringConverter<HealthPersonnel>() {
            @Override
            public String toString(HealthPersonnel p) {
                return p == null ? "" :
                        p.getFirstName() + " " + p.getLastName() + " (" + p.getRole() + ")";
            }

            @Override
            public HealthPersonnel fromString(String string) {
                return null;
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}