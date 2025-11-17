package app.controller;

import app.dao.*;
import app.model.*;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AvailServiceDialogController {

    @FXML private TextField txtSearchResident;
    @FXML private TableView<Resident> tableResidents;
    @FXML private TableColumn<Resident, Integer> colResidentId;
    @FXML private TableColumn<Resident, String> colResidentName;
    @FXML private TableColumn<Resident, Integer> colAge;
    @FXML private TableColumn<Resident, String> colSex;
    @FXML private TableColumn<Resident, String> colContact;
    @FXML private Label lblSelectedResident;

    @FXML private ComboBox<HealthService> comboService;
    @FXML private Label lblServiceFee;

    @FXML private ComboBox<HealthPersonnel> comboPersonnel;

    @FXML private DatePicker dateProvided;
    @FXML private TextArea txtRemarks;

    @FXML private Label lblStatus;

    private final ResidentDAO residentDAO = new ResidentDAO();
    private final HealthServiceDAO serviceDAO = new HealthServiceDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();
    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();

    private Resident selectedResident = null;

    @FXML
    private void initialize() {
        // Setup table columns
        colResidentId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getResidentId()));
        colResidentName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colAge.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAge()));
        colSex.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getSex()));
        colContact.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getContactNumber()));

        // Load all residents initially
        loadAllResidents();

        // Handle resident selection
        tableResidents.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedResident = newVal;
                lblSelectedResident.setText("✓ Selected: " + selectedResident.getFirstName() +
                        " " + selectedResident.getLastName() + " (ID: " + selectedResident.getResidentId() + ")");
                lblSelectedResident.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        // Load services
        loadServices();

        // Handle service selection to show fee
        comboService.setOnAction(e -> {
            HealthService selected = comboService.getValue();
            if (selected != null) {
                lblServiceFee.setText("₱" + String.format("%.2f", selected.getFee()));
            }
        });

        // Load personnel
        loadPersonnel();

        // Set default date to today
        dateProvided.setValue(LocalDate.now());
    }

    private void loadAllResidents() {
        List<Resident> residents = residentDAO.getAllResidents();
        tableResidents.setItems(FXCollections.observableArrayList(residents));
    }

    @FXML
    private void handleSearchResident() {
        String searchText = txtSearchResident.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            loadAllResidents();
            return;
        }

        List<Resident> allResidents = residentDAO.getAllResidents();
        List<Resident> filtered = allResidents.stream()
                .filter(r ->
                        r.getFirstName().toLowerCase().contains(searchText) ||
                                r.getLastName().toLowerCase().contains(searchText) ||
                                String.valueOf(r.getResidentId()).contains(searchText))
                .collect(Collectors.toList());

        tableResidents.setItems(FXCollections.observableArrayList(filtered));
    }

    private void loadServices() {
        List<HealthService> services = serviceDAO.getAllServices();
        comboService.setItems(FXCollections.observableArrayList(services));

        // Custom display for combo box
        comboService.setConverter(new javafx.util.StringConverter<HealthService>() {
            @Override
            public String toString(HealthService service) {
                return service == null ? "" : service.getServiceType() + " - ₱" +
                        String.format("%.2f", service.getFee());
            }

            @Override
            public HealthService fromString(String string) {
                return null;
            }
        });
    }

    private void loadPersonnel() {
        List<HealthPersonnel> personnel = personnelDAO.getAllPersonnel();
        comboPersonnel.setItems(FXCollections.observableArrayList(personnel));

        // Custom display for combo box
        comboPersonnel.setConverter(new javafx.util.StringConverter<HealthPersonnel>() {
            @Override
            public String toString(HealthPersonnel p) {
                return p == null ? "" : p.getFirstName() + " " + p.getLastName() +
                        " (" + p.getRole() + ")";
            }

            @Override
            public HealthPersonnel fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handleCompleteTransaction() {
        // Validation
        if (selectedResident == null) {
            showStatus("Please select a resident", true);
            return;
        }

        if (comboService.getValue() == null) {
            showStatus("Please select a health service", true);
            return;
        }

        if (comboPersonnel.getValue() == null) {
            showStatus("Please assign a health personnel", true);
            return;
        }

        if (dateProvided.getValue() == null) {
            showStatus("Please select a date", true);
            return;
        }

        // Create service transaction
        ServiceTransaction transaction = new ServiceTransaction();
        transaction.setResidentId(selectedResident.getResidentId());
        transaction.setServiceId(comboService.getValue().getServiceId());
        transaction.setPersonnelId(comboPersonnel.getValue().getPersonnelId());
        transaction.setDateProvided(Date.valueOf(dateProvided.getValue()));
        transaction.setRemarks(txtRemarks.getText().trim());

        // Save to database
        if (transactionDAO.addTransaction(transaction)) {
            // Log the action
            if (SessionManager.isLoggedIn()) {
                String action = String.format("AVAIL_SERVICE: Resident %s availed %s by %s",
                        selectedResident.getFirstName() + " " + selectedResident.getLastName(),
                        comboService.getValue().getServiceType(),
                        comboPersonnel.getValue().getFirstName() + " " + comboPersonnel.getValue().getLastName());

                residentDAO.logAction(SessionManager.getCurrentUser().getUserId(), action);
            }

            showStatus("✓ Transaction completed successfully!", false);

            // Show success dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Transaction Complete");
            alert.setHeaderText("Health Service Transaction Recorded");
            alert.setContentText(
                    "Resident: " + selectedResident.getFirstName() + " " + selectedResident.getLastName() + "\n" +
                            "Service: " + comboService.getValue().getServiceType() + "\n" +
                            "Fee: ₱" + String.format("%.2f", comboService.getValue().getFee()) + "\n" +
                            "Personnel: " + comboPersonnel.getValue().getFirstName() + " " +
                            comboPersonnel.getValue().getLastName() + "\n" +
                            "Date: " + dateProvided.getValue()
            );
            alert.showAndWait();

            // Close dialog
            handleCancel();

        } else {
            showStatus("Failed to save transaction. Please try again.", true);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtSearchResident.getScene().getWindow();
        stage.close();
    }

    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblStatus.setVisible(true);
    }
}