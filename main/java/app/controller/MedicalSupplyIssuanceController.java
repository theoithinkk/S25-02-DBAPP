package app.controller;

import app.dao.ClinicInventoryDAO;
import app.dao.InventoryMovementDAO;
import app.dao.ResidentDAO;
import app.model.ClinicInventory;
import app.model.Resident;
import app.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class MedicalSupplyIssuanceController {

    @FXML private TextField txtSearchResident;
    @FXML private TableView<Resident> tableResidents;
    @FXML private TableColumn<Resident, Integer> colResidentId;
    @FXML private TableColumn<Resident, String> colResidentName;
    @FXML private TableColumn<Resident, Integer> colAge;
    @FXML private TableColumn<Resident, String> colSex;
    @FXML private TableColumn<Resident, String> colContact;
    @FXML private TableColumn<Resident, String> colVulnerability;
    @FXML private Label lblSelectedResident;

    @FXML private ComboBox<ClinicInventory> cmbMedicalSupply;
    @FXML private TextField txtQuantity;
    @FXML private TextArea txtRemarks;
    @FXML private Label lblCurrentStock;
    @FXML private Label lblExpirationDate;
    @FXML private Label lblStockStatus;
    @FXML private VBox containerStockInfo;

    @FXML private ComboBox<String> cmbPersonnel;
    @FXML private DatePicker dateIssuance;
    @FXML private Label lblStatus;

    // Additional fields
    @FXML private Label lblDaysRemaining;
    @FXML private Label lblRemainingAfter;
    @FXML private Label lblPersonnelRole;
    @FXML private ComboBox<String> cmbTime;
    @FXML private VBox containerSummary;
    @FXML private Label lblSummaryReceiver;
    @FXML private Label lblSummaryItem;
    @FXML private Label lblSummaryQuantity;
    @FXML private Label lblSummaryPersonnel;
    @FXML private Label lblSummaryDate;

    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final InventoryMovementDAO movementDAO = new InventoryMovementDAO();

    private Resident selectedResident = null;

    @FXML
    private void initialize() {
        setupResidentTableView();
        setupMedicalSupplyComboBox();
        setupPersonnelComboBox();
        setupTimeComboBox();
        setupEventHandlers();

        containerStockInfo.setVisible(false);
        containerSummary.setVisible(false);
        dateIssuance.setValue(LocalDate.now());
    }

    private void setupResidentTableView() {
        List<Resident> residents = residentDAO.getAllResidents();
        ObservableList<Resident> residentList = FXCollections.observableArrayList(residents);
        tableResidents.setItems(residentList);

        // FIXED: Use property bindings like the working controller
        colResidentId.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getResidentId()));
        colResidentName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colAge.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getAge()));
        colSex.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSex()));
        colContact.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getContactNumber()));
        colVulnerability.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getVulnerabilityStatus()));

        // FIXED: Use proper selection handling like working controller
        tableResidents.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedResident = newVal;
                lblSelectedResident.setText("✓ Selected: " + selectedResident.getFirstName() +
                        " " + selectedResident.getLastName() + " (ID: " + selectedResident.getResidentId() + ")");
                lblSelectedResident.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                updateTransactionSummary();
            } else {
                selectedResident = null;
                lblSelectedResident.setText("No resident selected");
                lblSelectedResident.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
            }
        });
    }

    private void setupMedicalSupplyComboBox() {
        List<ClinicInventory> availableItems = inventoryDAO.getAllItems().stream()
                .filter(item -> item.getQuantity() > 0)
                .collect(Collectors.toList());

        ObservableList<ClinicInventory> supplyList = FXCollections.observableArrayList(availableItems);
        cmbMedicalSupply.setItems(supplyList);

        cmbMedicalSupply.setConverter(new StringConverter<ClinicInventory>() {
            @Override
            public String toString(ClinicInventory item) {
                return item == null ? "" : item.getItemName() + " (" + item.getQuantity() + " available)";
            }

            @Override
            public ClinicInventory fromString(String string) {
                return null;
            }
        });
    }

    private void setupPersonnelComboBox() {
        ObservableList<String> personnelList = FXCollections.observableArrayList(
                "Dr. Maria Santos - Physician",
                "Nurse Juan Dela Cruz - Registered Nurse",
                "MedTech Ana Reyes - Medical Technologist",
                "Midwife Lorna Garcia - Midwife"
        );
        cmbPersonnel.setItems(personnelList);
    }

    private void setupTimeComboBox() {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 8; hour <= 17; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                String time = String.format("%02d:%02d", hour, minute);
                timeSlots.add(time);
            }
        }
        cmbTime.setItems(timeSlots);
        cmbTime.setValue("08:00");
    }

    private void setupEventHandlers() {
        cmbMedicalSupply.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateStockInfo(newVal);
                containerStockInfo.setVisible(true);
                updateTransactionSummary();
            } else {
                containerStockInfo.setVisible(false);
            }
        });

        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRemainingAfterCalculation();
            updateTransactionSummary();
        });

        cmbPersonnel.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePersonnelRole(newVal);
            updateTransactionSummary();
        });

        dateIssuance.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTransactionSummary();
        });

        cmbTime.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTransactionSummary();
        });
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
                                String.valueOf(r.getResidentId()).contains(searchText) ||
                                (r.getVulnerabilityStatus() != null &&
                                        r.getVulnerabilityStatus().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());

        tableResidents.setItems(FXCollections.observableArrayList(filtered));
    }

    private void loadAllResidents() {
        List<Resident> residents = residentDAO.getAllResidents();
        tableResidents.setItems(FXCollections.observableArrayList(residents));
    }

    // FIXED: Better validation like working controller
    private boolean validateInputs() {
        if (selectedResident == null) {
            showStatus("Please select a resident", true);
            return false;
        }

        ClinicInventory selectedItem = cmbMedicalSupply.getValue();
        if (selectedItem == null) {
            showStatus("Please select a medical supply item", true);
            return false;
        }

        if (cmbPersonnel.getValue() == null) {
            showStatus("Please assign health personnel", true);
            return false;
        }

        if (dateIssuance.getValue() == null) {
            showStatus("Please select issuance date", true);
            return false;
        }

        if (cmbTime.getValue() == null) {
            showStatus("Please select issuance time", true);
            return false;
        }

        // Check expiration date
        if (selectedItem.getExpirationDate() != null) {
            LocalDate expDate = selectedItem.getExpirationDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (expDate.isBefore(LocalDate.now())) {
                showStatus("This item expired on " + expDate + ". Cannot issue expired medical supplies.", true);
                return false;
            }
        }

        try {
            int quantity = Integer.parseInt(txtQuantity.getText());
            if (quantity <= 0) {
                showStatus("Quantity must be greater than 0", true);
                return false;
            }

            if (selectedItem.getQuantity() < quantity) {
                showStatus("Only " + selectedItem.getQuantity() + " " + selectedItem.getItemName() +
                        " available. Cannot issue " + quantity + ".", true);
                return false;
            }
        } catch (NumberFormatException e) {
            showStatus("Please enter a valid quantity", true);
            return false;
        }

        return true;
    }

    // FIXED: Better transaction handling like working controller
    @FXML
    private void handleIssueSupplies() {
        if (!validateInputs()) {
            return;
        }

        ClinicInventory selectedItem = cmbMedicalSupply.getValue();
        int quantity = Integer.parseInt(txtQuantity.getText());

        try {
            // Update inventory quantity
            if (!inventoryDAO.deductQuantity(selectedItem.getItemId(), quantity)) {
                showStatus("Failed to update inventory. Please try again.", true);
                return;
            }

            // Log ISSUE movement in InventoryMovement table
            String remarks = txtRemarks.getText().trim();
            if (remarks.isEmpty()) {
                remarks = "Issued to " + selectedResident.getFirstName() + " " + selectedResident.getLastName();
            }

            boolean movementLogged = movementDAO.insertIssueMovement(
                    selectedItem.getItemId(),
                    quantity,
                    getPersonnelIdFromSelection(),
                    selectedResident.getResidentId(),
                    remarks
            );

            if (movementLogged) {
                // Log the action
                if (SessionManager.isLoggedIn()) {
                    String action = String.format("ISSUE_SUPPLY: Resident %s received %d %s issued by %s",
                            selectedResident.getFirstName() + " " + selectedResident.getLastName(),
                            quantity,
                            selectedItem.getItemName(),
                            cmbPersonnel.getValue());

                    residentDAO.logAction(SessionManager.getCurrentUser().getUserId(), action);
                }

                showStatus("✓ Medical supplies issued successfully!", false);

                // Show success dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Issuance Complete");
                alert.setHeaderText("Medical Supplies Issued Successfully");
                alert.setContentText(
                        "Resident: " + selectedResident.getFirstName() + " " + selectedResident.getLastName() + "\n" +
                                "Item: " + selectedItem.getItemName() + "\n" +
                                "Quantity: " + quantity + "\n" +
                                "Issued by: " + cmbPersonnel.getValue() + "\n" +
                                "Date: " + dateIssuance.getValue() + " at " + cmbTime.getValue()
                );
                alert.showAndWait();

                clearForm();
                refreshSuppliesList();

            } else {
                showStatus("Failed to record issuance. Please try again.", true);
            }

        } catch (Exception e) {
            showStatus("An error occurred: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtSearchResident.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        selectedResident = null;
        tableResidents.getSelectionModel().clearSelection();
        cmbMedicalSupply.setValue(null);
        cmbPersonnel.setValue(null);
        cmbTime.setValue("08:00");
        txtQuantity.clear();
        txtRemarks.clear();
        txtSearchResident.clear();
        dateIssuance.setValue(LocalDate.now());
        containerStockInfo.setVisible(false);
        containerSummary.setVisible(false);
        lblSelectedResident.setText("No resident selected");
        lblSelectedResident.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
        lblDaysRemaining.setText("-");
        lblRemainingAfter.setText("-");
        lblPersonnelRole.setText("-");
        lblStatus.setVisible(false);
    }

    private void refreshSuppliesList() {
        setupMedicalSupplyComboBox();
    }

    private void updateStockInfo(ClinicInventory item) {
        lblCurrentStock.setText(String.valueOf(item.getQuantity()));

        if (item.getExpirationDate() != null) {
            LocalDate expDate = item.getExpirationDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate today = LocalDate.now();

            lblExpirationDate.setText("Expires: " + expDate.toString());

            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expDate);
            lblDaysRemaining.setText(daysRemaining + " days");

            if (daysRemaining < 0) {
                lblDaysRemaining.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                lblExpirationDate.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else if (daysRemaining <= 30) {
                lblDaysRemaining.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                lblExpirationDate.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                lblDaysRemaining.setStyle("-fx-text-fill: green;");
                lblExpirationDate.setStyle("-fx-text-fill: green;");
            }
        } else {
            lblExpirationDate.setText("No expiration date");
            lblExpirationDate.setStyle("-fx-text-fill: gray;");
            lblDaysRemaining.setText("-");
            lblDaysRemaining.setStyle("-fx-text-fill: gray;");
        }

        // Stock status
        if (item.getQuantity() == 0) {
            lblStockStatus.setText("❌ OUT OF STOCK");
            lblStockStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else if (item.getQuantity() <= 5) {
            lblStockStatus.setText("⚠️ LOW STOCK");
            lblStockStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            lblStockStatus.setText("✅ Available");
            lblStockStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }

        updateRemainingAfterCalculation();
    }

    private void updateRemainingAfterCalculation() {
        try {
            ClinicInventory selectedItem = cmbMedicalSupply.getValue();
            if (selectedItem != null && !txtQuantity.getText().isEmpty()) {
                int currentQty = selectedItem.getQuantity();
                int issueQty = Integer.parseInt(txtQuantity.getText());
                int remaining = currentQty - issueQty;

                lblRemainingAfter.setText(String.valueOf(remaining));
                if (remaining < 0) {
                    lblRemainingAfter.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (remaining <= 5) {
                    lblRemainingAfter.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                } else {
                    lblRemainingAfter.setStyle("-fx-text-fill: #3b82f6;");
                }
            } else {
                lblRemainingAfter.setText("-");
                lblRemainingAfter.setStyle("-fx-text-fill: #6b7280;");
            }
        } catch (NumberFormatException e) {
            lblRemainingAfter.setText("-");
            lblRemainingAfter.setStyle("-fx-text-fill: #6b7280;");
        }
    }

    private void updatePersonnelRole(String personnel) {
        if (personnel != null) {
            if (personnel.contains("Dr.")) {
                lblPersonnelRole.setText("Physician");
            } else if (personnel.contains("Nurse")) {
                lblPersonnelRole.setText("Registered Nurse");
            } else if (personnel.contains("MedTech")) {
                lblPersonnelRole.setText("Medical Technologist");
            } else if (personnel.contains("Midwife")) {
                lblPersonnelRole.setText("Midwife");
            } else {
                lblPersonnelRole.setText("Healthcare Provider");
            }
        } else {
            lblPersonnelRole.setText("-");
        }
    }

    private void updateTransactionSummary() {
        ClinicInventory item = cmbMedicalSupply.getValue();
        String personnel = cmbPersonnel.getValue();

        if (selectedResident != null || item != null || personnel != null) {
            containerSummary.setVisible(true);

            lblSummaryReceiver.setText("Receiver: " + (selectedResident != null ?
                    selectedResident.getFirstName() + " " + selectedResident.getLastName() : "Not selected"));
            lblSummaryItem.setText("Item: " + (item != null ? item.getItemName() : "Not selected"));
            lblSummaryQuantity.setText("Quantity: " + (!txtQuantity.getText().isEmpty() ?
                    txtQuantity.getText() : "-"));
            lblSummaryPersonnel.setText("Personnel: " + (personnel != null ? personnel : "Not selected"));

            String dateTime = (dateIssuance.getValue() != null ? dateIssuance.getValue().toString() : "-") +
                    " " + (cmbTime.getValue() != null ? cmbTime.getValue() : "");
            lblSummaryDate.setText("Date & Time: " + dateTime);
        } else {
            containerSummary.setVisible(false);
        }
    }

    private int getPersonnelIdFromSelection() {
        String personnel = cmbPersonnel.getValue();
        if (personnel != null) {
            if (personnel.contains("Dr.")) return 1;
            if (personnel.contains("Nurse")) return 2;
            if (personnel.contains("MedTech")) return 3;
            if (personnel.contains("Midwife")) return 4;
        }
        return 1;
    }

    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblStatus.setVisible(true);
    }
}