package app.controller;

import app.dao.*;
import app.model.*;
import app.util.SessionManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import javafx.stage.Modality;
import javafx.stage.Window;

import java.sql.Date;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class TransactionController {

    @FXML private StackPane contentPane;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();
    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();
    private final InventoryMovementDAO movementDAO = new InventoryMovementDAO();
    private final ClinicVisitsDAO clinicVisitsDAO = new ClinicVisitsDAO();
    private final ResidentDAO residentsDAO = new ResidentDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        showTransactions();
    }

    @FXML
    private void showTransactions() {
        contentPane.getChildren().setAll(createTransactionsView());
    }

    @FXML
    private void showInventoryMovement() {
        contentPane.getChildren().setAll(createInventoryMovementView());
    }

    @FXML
    private void showClinicVisits() {
        contentPane.getChildren().setAll(createClinicVisitsView());
    }

    // Get owner window for dialogs
    private Window getOwnerWindow(javafx.scene.Node node) {
        if (node == null || node.getScene() == null) return null;
        return node.getScene().getWindow();
    }

    // =====================================================
    //                 TRANSACTIONS VIEW
    // =====================================================

    private VBox createTransactionsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Service Transactions Management");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitle = new Label("📌 To create a new transaction, use 'Avail Health Service' from the Dashboard.");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        TableView<ServiceTransaction> table = new TableView<>();
        table.setPrefHeight(420);

        TableColumn<ServiceTransaction, Integer> colId = new TableColumn<>("Transaction ID");
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTransactionId()));
        colId.setPrefWidth(110);

        TableColumn<ServiceTransaction, String> colResident = new TableColumn<>("Resident");
        colResident.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResidentName()));
        colResident.setPrefWidth(150);

        TableColumn<ServiceTransaction, String> colService = new TableColumn<>("Service");
        colService.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getServiceType()));
        colService.setPrefWidth(150);

        TableColumn<ServiceTransaction, String> colPersonnel = new TableColumn<>("Personnel");
        colPersonnel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersonnelName()));
        colPersonnel.setPrefWidth(150);

        TableColumn<ServiceTransaction, Date> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDateProvided()));
        colDate.setPrefWidth(100);

        TableColumn<ServiceTransaction, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> {
            TransactionStatus s = d.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.getDisplayName() : "PENDING");
        });
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colId, colResident, colService, colPersonnel, colDate, colStatus);
        loadTransactions(table);

        VBox buttonBox = new VBox(10);

        Button btnAddInventory = new Button("📦 Add Inventory Item to Transaction");
        btnAddInventory.setStyle("-fx-background-color:#3498db; -fx-text-fill:white;");
        btnAddInventory.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showAddInventoryDialog(selected, table);
            else showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.", table);
        });

        Button btnUpdateStatus = new Button("🔄 Update Transaction Status");
        btnUpdateStatus.setStyle("-fx-background-color:#f39c12; -fx-text-fill:white;");
        btnUpdateStatus.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) updateTransactionStatus(selected, table);
            else showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.", table);
        });

        Button btnDelete = new Button("🗑️ Delete Transaction");
        btnDelete.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white;");
        btnDelete.setOnAction(e -> {
            ServiceTransaction selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) deleteTransaction(selected, table);
            else showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a transaction first.", table);
        });

        if (!SessionManager.canDelete()) {
            btnDelete.setDisable(true);
            btnDelete.setOpacity(0.4);
        }

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadTransactions(table));

        buttonBox.getChildren().addAll(btnAddInventory, btnUpdateStatus, btnDelete, btnRefresh);

        vbox.getChildren().addAll(title, subtitle, table, buttonBox);
        return vbox;
    }

    // =====================================================
    //        INVENTORY MOVEMENT VIEW (UNIFIED)
    // =====================================================

    private VBox createInventoryMovementView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Inventory Movement Log");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Track all inventory movements - RESTOCK, ISSUE, and SERVICE transactions.");
        subtitle.setStyle("-fx-font-size:13; -fx-text-fill:#7f8c8d;");

        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        Label filterLabel = new Label("Filter by Type:");
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("ALL", "RESTOCK", "ISSUE", "SERVICE");
        filterCombo.setValue("ALL");
        filterCombo.setPrefWidth(150);

        filterBox.getChildren().addAll(filterLabel, filterCombo);

        TableView<InventoryMovement> table = new TableView<>();
        table.setPrefHeight(420);

        // Preload maps for displaying names
        Map<Integer, String> itemNames = new HashMap<>();
        for (ClinicInventory i : inventoryDAO.getAllItems()) {
            itemNames.put(i.getItemId(), i.getItemName());
        }

        Map<Integer, String> personnelNames = new HashMap<>();
        for (HealthPersonnel p : personnelDAO.getAllPersonnel()) {
            personnelNames.put(p.getPersonnelId(), p.getFirstName() + " " + p.getLastName());
        }

        Map<Integer, String> residentNames = new HashMap<>();
        for (Resident r : residentsDAO.getAllResidents()) {
            residentNames.put(r.getResidentId(), r.getFirstName() + " " + r.getLastName());
        }

        // Table columns
        TableColumn<InventoryMovement, Integer> colId = new TableColumn<>("Movement ID");
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getMovementId()));
        colId.setPrefWidth(100);

        TableColumn<InventoryMovement, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovementType()));
        colType.setPrefWidth(90);

        TableColumn<InventoryMovement, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(d -> new SimpleStringProperty(
                itemNames.getOrDefault(d.getValue().getItemId(), "Unknown")
        ));
        colItem.setPrefWidth(150);

        TableColumn<InventoryMovement, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getQuantity()));
        colQty.setPrefWidth(80);

        TableColumn<InventoryMovement, String> colActor = new TableColumn<>("Personnel");
        colActor.setCellValueFactory(d -> new SimpleStringProperty(
                personnelNames.getOrDefault(d.getValue().getActorId(), "Unknown")
        ));
        colActor.setPrefWidth(150);

        TableColumn<InventoryMovement, String> colResident = new TableColumn<>("Resident");
        colResident.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getResidentId() == null
                        ? "N/A"
                        : residentNames.getOrDefault(d.getValue().getResidentId(), "Unknown")
        ));
        colResident.setPrefWidth(150);

        TableColumn<InventoryMovement, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMovementDate() != null ? d.getValue().getMovementDate().toString() : "N/A"
        ));
        colDate.setPrefWidth(150);

        TableColumn<InventoryMovement, String> colRemarks = new TableColumn<>("Remarks");
        colRemarks.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRemarks()));
        colRemarks.setPrefWidth(200);

        table.getColumns().addAll(colId, colType, colItem, colQty, colActor, colResident, colDate, colRemarks);

        // Load data
        loadInventoryMovements(table, "ALL");

        // Filter action
        filterCombo.setOnAction(e -> loadInventoryMovements(table, filterCombo.getValue()));

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadInventoryMovements(table, filterCombo.getValue()));

        VBox buttonBox = new VBox(10, btnRefresh);

        vbox.getChildren().addAll(title, subtitle, filterBox, table, buttonBox);
        return vbox;
    }

    // =====================================================
    //                CLINIC VISITS VIEW
    // =====================================================

    private VBox createClinicVisitsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Clinic Visits Management");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Walk-ins and scheduled appointments.");
        subtitle.setStyle("-fx-font-size:13; -fx-text-fill:#7f8c8d;");

        TableView<ClinicVisits> table = new TableView<>();

        TableColumn<ClinicVisits, Integer> colVisit = new TableColumn<>("Visit ID");
        colVisit.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getVisitId()));
        colVisit.setPrefWidth(80);

        TableColumn<ClinicVisits, String> colRes = new TableColumn<>("Resident");
        colRes.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResidentName()));
        colRes.setPrefWidth(150);

        TableColumn<ClinicVisits, Date> colDate = new TableColumn<>("Visit Date");
        colDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getVisitDate()));
        colDate.setPrefWidth(100);

        TableColumn<ClinicVisits, String> colType = new TableColumn<>("Visit Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVisitType()));
        colType.setPrefWidth(130);

        TableColumn<ClinicVisits, String> colDiag = new TableColumn<>("Diagnosis");
        colDiag.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiagnosis()));
        colDiag.setPrefWidth(200);

        table.getColumns().addAll(colVisit, colRes, colDate, colType, colDiag);

        loadClinicVisits(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadClinicVisits(table));

        vbox.getChildren().addAll(title, subtitle, table, btnRefresh);
        return vbox;
    }

    // =====================================================
    //         LOADING FUNCTIONS
    // =====================================================

    private void loadTransactions(TableView<ServiceTransaction> table) {
        List<ServiceTransaction> list = transactionDAO.getAllTransactions();
        list.sort((a, b) -> b.getTransactionId() - a.getTransactionId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadClinicVisits(TableView<ClinicVisits> table) {
        List<ClinicVisits> list = clinicVisitsDAO.getAllClinicVisits();
        list.sort((a, b) -> b.getVisitId() - a.getVisitId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadInventoryMovements(TableView<InventoryMovement> table, String filterType) {
        List<InventoryMovement> list;
        if ("ALL".equals(filterType)) {
            list = movementDAO.getAll();
        } else {
            list = movementDAO.getByMovementType(filterType);
        }
        list.sort((a, b) -> b.getMovementId() - a.getMovementId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    // =====================================================
    //       ADD INVENTORY TO TRANSACTION (MODAL)
    // =====================================================

    private void showAddInventoryDialog(ServiceTransaction transaction, TableView<ServiceTransaction> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(getOwnerWindow(table));
        dialog.initModality(Modality.WINDOW_MODAL);

        dialog.setTitle("Add Inventory to Transaction");
        dialog.setHeaderText("Transaction ID: " + transaction.getTransactionId() +
                "\nResident: " + transaction.getResidentName() +
                "\nService: " + transaction.getServiceType());

        ButtonType addButton = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 15, 10, 15));

        ComboBox<ClinicInventory> comboItem = new ComboBox<>();
        TextField txtQuantity = new TextField("1");
        DatePicker expirationPicker = new DatePicker(LocalDate.now().plusYears(1));

        List<ClinicInventory> items = inventoryDAO.getAllItems();
        comboItem.setItems(FXCollections.observableArrayList(items));
        comboItem.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ClinicInventory item) {
                return item == null ? "" : item.getItemName() + " (Stock: " + item.getQuantity() + ")";
            }

            @Override
            public ClinicInventory fromString(String s) { return null; }
        });

        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtQuantity.setText(newVal.replaceAll("[^\\d]", ""));
        });

        grid.add(new Label("Select Item:"), 0, 0);
        grid.add(comboItem, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(txtQuantity, 1, 1);
        grid.add(new Label("Expiration:"), 0, 2);
        grid.add(expirationPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != addButton) return;

        ClinicInventory selected = comboItem.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item.", table);
            return;
        }

        int qty = Integer.parseInt(txtQuantity.getText());
        if (qty <= 0 || qty > selected.getQuantity()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Quantity",
                    "Please enter a valid quantity (max: " + selected.getQuantity() + ").", table);
            return;
        }

        try {
            // Update inventory quantity
            selected.setQuantity(selected.getQuantity() - qty);
            inventoryDAO.updateItem(selected);

            // Log as SERVICE movement in InventoryMovement table
            if (transaction.getResidentId() > 0 && transaction.getPersonnelId() > 0) {
                movementDAO.insertServiceMovement(
                        selected.getItemId(),
                        qty,
                        transaction.getPersonnelId(),
                        transaction.getResidentId(),
                        "Used in Transaction #" + transaction.getTransactionId()
                );
            }

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Inventory added successfully.", table);

            transactionDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                    "Added inventory item to Transaction #" + transaction.getTransactionId());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add inventory: " + e.getMessage(), table);
        }
    }

    // =====================================================
    //        UPDATE STATUS + DELETE TRANSACTION
    // =====================================================

    private void updateTransactionStatus(ServiceTransaction transaction, TableView<ServiceTransaction> table) {
        ChoiceDialog<TransactionStatus> dialog =
                new ChoiceDialog<>(transaction.getStatus(), TransactionStatus.values());

        dialog.initOwner(getOwnerWindow(table));
        dialog.initModality(Modality.WINDOW_MODAL);

        dialog.setTitle("Update Transaction Status");
        dialog.setHeaderText("Transaction ID: " + transaction.getTransactionId());

        Optional<TransactionStatus> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        if (transactionDAO.updateTransactionStatus(transaction.getTransactionId(), result.get())) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated!", table);
            loadTransactions(table);
        }
    }

    private void deleteTransaction(ServiceTransaction transaction, TableView<ServiceTransaction> table) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(getOwnerWindow(table));
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Delete Transaction");
        confirm.setHeaderText("Delete Transaction ID: " + transaction.getTransactionId() + "?");

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            if (transactionDAO.deleteTransaction(transaction.getTransactionId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Transaction deleted!", table);
                loadTransactions(table);
            }
        }
    }

    // =====================================================
    //                  ALERT HANDLER
    // =====================================================

    private void showAlert(Alert.AlertType type, String title, String message, javafx.scene.Node node) {
        Alert alert = new Alert(type);

        Window owner = null;
        try {
            if (node != null && node.getScene() != null) {
                owner = node.getScene().getWindow();
            }
        } catch (Exception ignored) {}

        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL); // FIX
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}