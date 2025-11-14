package app.controller;

import app.dao.*;
import app.model.*;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionController {

    @FXML private StackPane contentPane;

    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();
    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();
    private final ServiceInventoryDAO serviceInventoryDAO = new ServiceInventoryDAO();
    private final ClinicVisitsDAO clinicVisitsDAO = new ClinicVisitsDAO();
    private final ResidentDAO residentsDAO = new ResidentDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        showTransactions();
    }

    @FXML
    private void showTransactions() {
        try {
            VBox transactionView = createTransactionsView();
            contentPane.getChildren().setAll(transactionView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showServiceInventory() {
        try {
            VBox inventoryView = createServiceInventoryView();
            contentPane.getChildren().setAll(inventoryView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showClinicVisits() {
        try {
            VBox clinicVisitsView = createClinicVisitsView();
            contentPane.getChildren().setAll(clinicVisitsView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createTransactionsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Service Transactions Management");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("📌 To create a new transaction, use 'Avail Health Service' from the Dashboard.");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-padding: 5 0 10 0;");

        TableView<ServiceTransaction> table = new TableView<>();
        table.setPrefHeight(420);

        TableColumn<ServiceTransaction, Integer> colId = new TableColumn<>("Transaction ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTransactionId()));
        colId.setPrefWidth(110);

        TableColumn<ServiceTransaction, String> colResident = new TableColumn<>("Resident");
        colResident.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getResidentName()));
        colResident.setPrefWidth(150);

        TableColumn<ServiceTransaction, String> colService = new TableColumn<>("Service");
        colService.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colService.setPrefWidth(150);

        TableColumn<ServiceTransaction, String> colPersonnel = new TableColumn<>("Personnel");
        colPersonnel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersonnelName()));
        colPersonnel.setPrefWidth(150);

        TableColumn<ServiceTransaction, Date> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateProvided()));
        colDate.setPrefWidth(100);

        TableColumn<ServiceTransaction, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> {
            TransactionStatus status = data.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(status != null ? status.getDisplayName() : "PENDING");
        });
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colId, colResident, colService, colPersonnel, colDate, colStatus);
        loadTransactions(table);

        VBox buttonBox = new VBox(10);

        Button btnAddInventory = new Button("📦 Add Inventory Item to Transaction");
        btnAddInventory.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-pref-width: 280;");
        btnAddInventory.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddInventoryDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.");
            }
        });

        Button btnUpdateStatus = new Button("🔄 Update Transaction Status");
        btnUpdateStatus.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-pref-width: 280;");
        btnUpdateStatus.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateTransactionStatus(selected, table);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.");
            }
        });

        Button btnDelete = new Button("🗑️ Delete Transaction");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-pref-width: 280;");
        btnDelete.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteTransaction(selected, table);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.");
            }
        });

        if (!SessionManager.canDelete()) {
            btnDelete.setDisable(true);
            btnDelete.setOpacity(0.5);
        }

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle("-fx-padding: 10 20; -fx-pref-width: 280;");
        btnRefresh.setOnAction(e -> loadTransactions(table));

        buttonBox.getChildren().addAll(btnAddInventory, btnUpdateStatus, btnDelete, btnRefresh);

        vbox.getChildren().addAll(title, subtitle, table, buttonBox);
        return vbox;
    }

    private VBox createClinicVisitsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Clinic Visits Management");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Log and manage clinic visits - walk-ins and scheduled appointments");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-padding: 5 0 10 0;");

        TableView<ClinicVisits> table = new TableView<>();
        table.setPrefHeight(400);

        // Define columns
        TableColumn<ClinicVisits, Integer> colId = new TableColumn<>("Visit ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getVisitId()));
        colId.setPrefWidth(80);

        TableColumn<ClinicVisits, String> colResident = new TableColumn<>("Resident");
        colResident.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getResidentName()));
        colResident.setPrefWidth(150);

        TableColumn<ClinicVisits, String> colPersonnel = new TableColumn<>("Health Personnel");
        colPersonnel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPersonnelName()));
        colPersonnel.setPrefWidth(150);

        TableColumn<ClinicVisits, String> colVisitType = new TableColumn<>("Type");
        colVisitType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getVisitType()));
        colVisitType.setPrefWidth(100);

        TableColumn<ClinicVisits, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDiagnosis()));
        colDiagnosis.setPrefWidth(150);

        TableColumn<ClinicVisits, String> colTreatment = new TableColumn<>("Treatment");
        colTreatment.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTreatment()));
        colTreatment.setPrefWidth(150);

        TableColumn<ClinicVisits, Date> colDate = new TableColumn<>("Visit Date");
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getVisitDate()));
        colDate.setPrefWidth(100);

        table.getColumns().addAll(colId, colResident, colPersonnel, colVisitType, colDiagnosis, colTreatment, colDate);
        loadClinicVisits(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadClinicVisits(table));

        HBox buttonBox = new HBox(10, btnRefresh);

        vbox.getChildren().addAll(title, subtitle, table, buttonBox);
        return vbox;
    }

    private VBox createServiceInventoryView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Service Inventory Usage");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Track which clinic inventory items were used in specific transactions.");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        TableView<ServiceInventory> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<ServiceInventory, Integer> colInventoryId = new TableColumn<>("Inventory ID");
        colInventoryId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getInventoryId()));
        colInventoryId.setPrefWidth(110);

        TableColumn<ServiceInventory, Integer> colTransactionId = new TableColumn<>("Transaction ID");
        colTransactionId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTransactionId()));
        colTransactionId.setPrefWidth(120);

        TableColumn<ServiceInventory, String> colItem = new TableColumn<>("Item Name");
        colItem.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        colItem.setPrefWidth(200);

        TableColumn<ServiceInventory, Integer> colQuantity = new TableColumn<>("Quantity Used");
        colQuantity.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        colQuantity.setPrefWidth(120);

        TableColumn<ServiceInventory, Date> colExpiration = new TableColumn<>("Expiration");
        colExpiration.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getExpiration()));
        colExpiration.setPrefWidth(120);

        TableColumn<ServiceInventory, Date> colDateProvided = new TableColumn<>("Date Used");
        colDateProvided.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateProvided()));
        colDateProvided.setPrefWidth(120);

        table.getColumns().addAll(colInventoryId, colTransactionId, colItem, colQuantity, colExpiration, colDateProvided);
        loadServiceInventory(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadServiceInventory(table));

        vbox.getChildren().addAll(title, subtitle, table, btnRefresh);
        return vbox;
    }

    // Clinic Visits Methods
    private void loadClinicVisits(TableView<ClinicVisits> table) {
        List<ClinicVisits> clinicVisits = clinicVisitsDAO.getAllClinicVisits();
        table.setItems(FXCollections.observableArrayList(clinicVisits));
    }

    private void showAddClinicVisitDialog(TableView<ClinicVisits> table) {
        Dialog<ClinicVisits> dialog = new Dialog<>();
        dialog.setTitle("Log New Clinic Visit");
        dialog.setHeaderText("Add a new clinic visit record");

        ButtonType saveButtonType = new ButtonType("Save Visit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields
        ComboBox<String> visitTypeCombo = new ComboBox<>(FXCollections.observableArrayList("SCHEDULED", "WALK_IN"));
        visitTypeCombo.setValue("WALK_IN");

        ComboBox<Resident> residentCombo = new ComboBox<>();
        residentCombo.setPromptText("Select Resident");

        ComboBox<HealthPersonnel> personnelCombo = new ComboBox<>();
        personnelCombo.setPromptText("Select Health Personnel");

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Diagnosis");
        diagnosisArea.setPrefRowCount(3);

        TextArea treatmentArea = new TextArea();
        treatmentArea.setPromptText("Treatment provided");
        treatmentArea.setPrefRowCount(3);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Additional notes");
        notesArea.setPrefRowCount(2);

        DatePicker visitDatePicker = new DatePicker(LocalDate.now());

        // Load residents and personnel
        residentCombo.setItems(FXCollections.observableArrayList(residentsDAO.getAllResidents()));
        personnelCombo.setItems(FXCollections.observableArrayList(personnelDAO.getAllPersonnel()));

        // Set converters
        residentCombo.setConverter(new javafx.util.StringConverter<Resident>() {
            @Override
            public String toString(Resident resident) {
                return resident == null ? "" : resident.getFirstName() + " " + resident.getLastName();
            }
            @Override
            public Resident fromString(String string) { return null; }
        });

        personnelCombo.setConverter(new javafx.util.StringConverter<HealthPersonnel>() {
            @Override
            public String toString(HealthPersonnel personnel) {
                return personnel == null ? "" : personnel.getFirstName() + " " + personnel.getLastName() + " - " + personnel.getRole();
            }
            @Override
            public HealthPersonnel fromString(String string) { return null; }
        });

        // Add fields to grid
        grid.add(new Label("Visit Type:"), 0, 0);
        grid.add(visitTypeCombo, 1, 0);
        grid.add(new Label("Resident:"), 0, 1);
        grid.add(residentCombo, 1, 1);
        grid.add(new Label("Health Personnel:"), 0, 2);
        grid.add(personnelCombo, 1, 2);
        grid.add(new Label("Diagnosis:"), 0, 3);
        grid.add(diagnosisArea, 1, 3);
        grid.add(new Label("Treatment:"), 0, 4);
        grid.add(treatmentArea, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);
        grid.add(new Label("Visit Date:"), 0, 6);
        grid.add(visitDatePicker, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Convert result to ClinicVisit
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate required fields
                    if (residentCombo.getValue() == null || personnelCombo.getValue() == null) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error",
                                "Please select both Resident and Health Personnel");
                        return null;
                    }

                    // Create clinic visit
                    ClinicVisits clinicVisit = new ClinicVisits();
                    clinicVisit.setResidentId(residentCombo.getValue().getResidentId());
                    clinicVisit.setPersonnelId(personnelCombo.getValue().getPersonnelId());
                    clinicVisit.setVisitType(visitTypeCombo.getValue());
                    clinicVisit.setDiagnosis(diagnosisArea.getText().trim());
                    clinicVisit.setTreatment(treatmentArea.getText().trim());
                    clinicVisit.setNotes(notesArea.getText().trim());
                    clinicVisit.setVisitDate(Date.valueOf(visitDatePicker.getValue()));

                    return clinicVisit;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create visit record: " + e.getMessage());
                }
            }
            return null;
        });

        Optional<ClinicVisits> result = dialog.showAndWait();
        result.ifPresent(clinicVisit -> {
            if (clinicVisitsDAO.addClinicVisit(clinicVisit)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Clinic visit logged successfully!");
                loadClinicVisits(table);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save clinic visit.");
            }
        });
    }

    // Service Transactions Methods
    private void showAddInventoryDialog(ServiceTransaction transaction) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Inventory to Transaction");
        dialog.setHeaderText("Transaction ID: " + transaction.getTransactionId() + "\n" +
                "Resident: " + transaction.getResidentName() + "\n" +
                "Service: " + transaction.getServiceType());

        ButtonType addButtonType = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<ClinicInventory> comboItem = new ComboBox<>();
        TextField txtQuantity = new TextField("1");
        DatePicker expirationPicker = new DatePicker(LocalDate.now().plusYears(1));

        // Load fresh inventory data
        List<ClinicInventory> items = inventoryDAO.getAllItems();
        comboItem.setItems(FXCollections.observableArrayList(items));
        comboItem.setConverter(new javafx.util.StringConverter<ClinicInventory>() {
            @Override
            public String toString(ClinicInventory item) {
                return item == null ? "" : item.getItemName() + " (Stock: " + item.getQuantity() + ")";
            }

            @Override
            public ClinicInventory fromString(String string) {
                return null;
            }
        });

        // Restrict text field to numbers only
        txtQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtQuantity.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        grid.add(new Label("Select Item:"), 0, 0);
        grid.add(comboItem, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(txtQuantity, 1, 1);
        grid.add(new Label("Expiration:"), 0, 2);
        grid.add(expirationPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == addButtonType) {
            ClinicInventory selectedItem = comboItem.getValue();

            if (selectedItem == null) {
                showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item.");
                showAddInventoryDialog(transaction);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(txtQuantity.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Please enter a valid number.");
                showAddInventoryDialog(transaction);
                return;
            }

            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be greater than 0.");
                showAddInventoryDialog(transaction);
                return;
            }

            if (quantity > selectedItem.getQuantity()) {
                showAlert(Alert.AlertType.ERROR, "Insufficient Stock",
                        "Cannot take " + quantity + " items.\n" +
                                "You requested: " + quantity + "\n" +
                                "Available stock: " + selectedItem.getQuantity() + "\n\n" +
                                "Please enter a quantity of " + selectedItem.getQuantity() + " or less.");
                showAddInventoryDialog(transaction);
                return;
            }

            // Create service inventory record
            ServiceInventory serviceInventory = new ServiceInventory();
            serviceInventory.setItemId(selectedItem.getItemId());
            serviceInventory.setServiceId(transaction.getServiceId());
            serviceInventory.setTransactionId(transaction.getTransactionId());
            serviceInventory.setQuantity(quantity);
            serviceInventory.setExpiration(Date.valueOf(expirationPicker.getValue()));

            // Add to service inventory database
            if (serviceInventoryDAO.addServiceInventory(serviceInventory)) {
                // Update clinic inventory - REDUCE the quantity
                int newQuantity = selectedItem.getQuantity() - quantity;
                selectedItem.setQuantity(newQuantity);

                if (inventoryDAO.updateItem(selectedItem)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "✓ Inventory item added successfully!\n\n" +
                                    "Transaction ID: " + transaction.getTransactionId() + "\n" +
                                    "Item: " + selectedItem.getItemName() + "\n" +
                                    "Quantity used: " + quantity + "\n" +
                                    "New stock level: " + newQuantity);

                    // Log the action
                    if (SessionManager.isLoggedIn()) {
                        transactionDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                                "ADD_INVENTORY: " + selectedItem.getItemName() +
                                        " (Qty: " + quantity + ") to Transaction #" + transaction.getTransactionId());
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update clinic inventory stock.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add item to service inventory.");
            }
        }
    }

    private void updateTransactionStatus(ServiceTransaction transaction, TableView<ServiceTransaction> table) {
        ChoiceDialog<TransactionStatus> dialog = new ChoiceDialog<>(transaction.getStatus(), TransactionStatus.values());
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Transaction ID: " + transaction.getTransactionId());
        dialog.setContentText("Select new status:");

        Optional<TransactionStatus> result = dialog.showAndWait();
        result.ifPresent(status -> {
            if (transactionDAO.updateTransactionStatus(transaction.getTransactionId(), status)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated!");
                loadTransactions(table);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
            }
        });
    }

    private void deleteTransaction(ServiceTransaction transaction, TableView<ServiceTransaction> table) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Transaction");
        confirm.setHeaderText("Delete Transaction ID: " + transaction.getTransactionId() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (transactionDAO.deleteTransaction(transaction.getTransactionId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Transaction deleted!");
                loadTransactions(table);
            }
        }
    }

    private void loadTransactions(TableView<ServiceTransaction> table) {
        List<ServiceTransaction> list = transactionDAO.getAllTransactions();
        list.sort((t1, t2) -> Integer.compare(t2.getTransactionId(), t1.getTransactionId()));
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadServiceInventory(TableView<ServiceInventory> table) {
        List<ServiceInventory> list = serviceInventoryDAO.getAllServiceInventory();
        list.sort((i1, i2) -> Integer.compare(i2.getInventoryId(), i1.getInventoryId()));
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}