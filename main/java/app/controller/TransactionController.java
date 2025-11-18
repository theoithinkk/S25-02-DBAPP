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
    private final ServiceInventoryDAO serviceInventoryDAO = new ServiceInventoryDAO();
    private final ClinicVisitsDAO clinicVisitsDAO = new ClinicVisitsDAO();
    private final ResidentDAO residentsDAO = new ResidentDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();
    private final RestockInventoryDAO restockDAO = new RestockInventoryDAO();

    @FXML
    private void initialize() {
        showTransactions();
    }

    @FXML
    private void showTransactions() {
        contentPane.getChildren().setAll(createTransactionsView());
    }

    @FXML
    private void showServiceInventory() {
        contentPane.getChildren().setAll(createServiceInventoryView());
    }

    @FXML
    private void showRestockInventory() {
        contentPane.getChildren().setAll(createRestockInventoryView());
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
    //             RESTOCK INVENTORY VIEW
    // =====================================================

    private VBox createRestockInventoryView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Restock Inventory Management");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Track and log all restocking activities.");
        subtitle.setStyle("-fx-font-size:13; -fx-text-fill:#7f8c8d;");

        TableView<RestockInventory> table = new TableView<>();
        table.setPrefHeight(450);

        // preload maps
        Map<Integer, String> itemNames = new HashMap<>();
        for (ClinicInventory i : inventoryDAO.getAllItems()) {
            itemNames.put(i.getItemId(), i.getItemName());
        }

        Map<Integer, String> personnelNames = new HashMap<>();
        for (HealthPersonnel p : personnelDAO.getAllPersonnel()) {
            personnelNames.put(p.getPersonnelId(), p.getFirstName() + " " + p.getLastName());
        }

        TableColumn<RestockInventory, Integer> colId = new TableColumn<>("Restock ID");
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRestockId()));
        colId.setPrefWidth(90);

        TableColumn<RestockInventory, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(d -> new SimpleStringProperty(
                itemNames.getOrDefault(d.getValue().getItemId(), "Unknown")
        ));
        colItem.setPrefWidth(150);

        TableColumn<RestockInventory, String> colBy = new TableColumn<>("Restocked By");
        colBy.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRestockedBy() == null
                        ? "N/A"
                        : personnelNames.getOrDefault(d.getValue().getRestockedBy(), "Unknown")
        ));
        colBy.setPrefWidth(150);

        TableColumn<RestockInventory, Integer> colQty = new TableColumn<>("Qty Added");
        colQty.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getQuantityAdded()));
        colQty.setPrefWidth(100);

        TableColumn<RestockInventory, String> colRemarks = new TableColumn<>("Remarks");
        colRemarks.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRemarks()));
        colRemarks.setPrefWidth(200);

        TableColumn<RestockInventory, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRestockDate() != null ? d.getValue().getRestockDate().toString() : "N/A"
        ));
        colDate.setPrefWidth(150);

        table.getColumns().addAll(colId, colItem, colBy, colQty, colRemarks, colDate);

        loadRestockInventory(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadRestockInventory(table));

        VBox buttonBox = new VBox(10, btnRefresh);

        vbox.getChildren().addAll(title, subtitle, table, buttonBox);
        return vbox;
    }

    // =====================================================
    //        SERVICE INVENTORY VIEW
    // =====================================================

    private VBox createServiceInventoryView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Service Inventory Management");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Items used during service transactions.");
        subtitle.setStyle("-fx-font-size:13; -fx-text-fill:#7f8c8d;");

        TableView<ServiceInventory> table = new TableView<>();
        table.setPrefHeight(420);

        TableColumn<ServiceInventory, Integer> colId = new TableColumn<>("Inventory ID");
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getInventoryId()));
        colId.setPrefWidth(100);

        TableColumn<ServiceInventory, Integer> colTrans = new TableColumn<>("Transaction ID");
        colTrans.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTransactionId()));
        colTrans.setPrefWidth(110);

        TableColumn<ServiceInventory, String> colItem = new TableColumn<>("Item Name");
        colItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItemName()));
        colItem.setPrefWidth(200);

        TableColumn<ServiceInventory, Integer> colQty = new TableColumn<>("Quantity Used");
        colQty.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getQuantity()));
        colQty.setPrefWidth(120);

        TableColumn<ServiceInventory, Date> colExp = new TableColumn<>("Expiration");
        colExp.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getExpiration()));
        colExp.setPrefWidth(130);

        table.getColumns().addAll(colId, colTrans, colItem, colQty, colExp);

        loadServiceInventory(table);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setOnAction(e -> loadServiceInventory(table));

        vbox.getChildren().addAll(title, subtitle, table, btnRefresh);
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

    private void loadServiceInventory(TableView<ServiceInventory> table) {
        List<ServiceInventory> list = serviceInventoryDAO.getAllServiceInventory();
        list.sort((a, b) -> b.getInventoryId() - a.getInventoryId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadClinicVisits(TableView<ClinicVisits> table) {
        List<ClinicVisits> list = clinicVisitsDAO.getAllClinicVisits();
        list.sort((a, b) -> b.getVisitId() - a.getVisitId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadRestockInventory(TableView<RestockInventory> table) {
        List<RestockInventory> list = restockDAO.getAll();
        list.sort((a, b) -> b.getRestockId() - a.getRestockId());
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

        ServiceInventory si = new ServiceInventory();
        si.setItemId(selected.getItemId());
        si.setServiceId(transaction.getServiceId());
        si.setTransactionId(transaction.getTransactionId());
        si.setQuantity(qty);
        si.setExpiration(Date.valueOf(expirationPicker.getValue()));

        if (serviceInventoryDAO.addServiceInventory(si)) {
            selected.setQuantity(selected.getQuantity() - qty);
            inventoryDAO.updateItem(selected);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Inventory added successfully.", table);

            transactionDAO.logAction(SessionManager.getCurrentUser().getUserId(),
                    "Added inventory item to Transaction #" + transaction.getTransactionId());
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