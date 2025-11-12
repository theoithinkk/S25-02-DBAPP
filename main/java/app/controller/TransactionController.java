package app.controller;

import app.dao.*;
import app.model.*;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionController {

    @FXML private StackPane contentPane;

    private final ServiceTransactionDAO transactionDAO = new ServiceTransactionDAO();
    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();
    private final ServiceInventoryDAO serviceInventoryDAO = new ServiceInventoryDAO();

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
                showAddInventoryDialog(transaction); // Re-open dialog
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(txtQuantity.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Please enter a valid number.");
                showAddInventoryDialog(transaction); // Re-open dialog
                return;
            }

            // Validate quantity
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be greater than 0.");
                showAddInventoryDialog(transaction); // Re-open dialog
                return;
            }

            // CRITICAL: Check against actual current stock
            if (quantity > selectedItem.getQuantity()) {
                showAlert(Alert.AlertType.ERROR, "Insufficient Stock",
                        "Cannot take " + quantity + " items.\n" +
                                "You requested: " + quantity + "\n" +
                                "Available stock: " + selectedItem.getQuantity() + "\n\n" +
                                "Please enter a quantity of " + selectedItem.getQuantity() + " or less.");
                showAddInventoryDialog(transaction); // Re-open dialog
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
        table.setItems(FXCollections.observableArrayList(transactionDAO.getAllTransactions()));
    }

    private void loadServiceInventory(TableView<ServiceInventory> table) {
        table.setItems(FXCollections.observableArrayList(serviceInventoryDAO.getAllServiceInventory()));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}