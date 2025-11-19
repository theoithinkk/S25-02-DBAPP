package app.controller;

import app.dao.HealthPersonnelDAO;
import app.dao.InventoryMovementDAO;
import app.model.ClinicInventory;
import app.model.HealthPersonnel;
import app.dao.ClinicInventoryDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class RestockInventoryController {

    @FXML private TextField txtSearchItem;
    @FXML private TableView<ClinicInventory> tableInventory;
    @FXML private TableColumn<ClinicInventory, Integer> colItemId;
    @FXML private TableColumn<ClinicInventory, String> colItemName;
    @FXML private TableColumn<ClinicInventory, String> colCategory;
    @FXML private TableColumn<ClinicInventory, Integer> colStock;

    @FXML private Label lblSelectedItem;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<HealthPersonnel> comboPersonnel;
    @FXML private TextArea txtRemarks;
    @FXML private Label lblStatus;

    private ClinicInventoryDAO inventoryDAO;
    private HealthPersonnelDAO personnelDAO;

    private ClinicInventory selectedItem;

    public void initialize() {
        inventoryDAO = new ClinicInventoryDAO();
        personnelDAO = new HealthPersonnelDAO();

        // Initialize table columns
        colItemId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getItemId()).asObject());
        colItemName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colStock.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        // Load clinic inventory
        refreshInventoryTable();

        // Listen for selection
        tableInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedItem = newSel;
            lblSelectedItem.setText(selectedItem != null ? selectedItem.getItemName() : "No item selected");
        });

        // Load health personnel
        List<HealthPersonnel> personnelList = personnelDAO.getAllPersonnel();
        comboPersonnel.setItems(FXCollections.observableArrayList(personnelList));

        // Customize how personnel appears in combo box
        comboPersonnel.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(HealthPersonnel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName() + " (" + item.getRole() + ")");
            }
        });
        comboPersonnel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(HealthPersonnel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName() + " (" + item.getRole() + ")");
            }
        });
    }

    private void refreshInventoryTable() {
        List<ClinicInventory> items = inventoryDAO.getAllItems();
        tableInventory.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void handleSearchItem(ActionEvent event) {
        String searchText = txtSearchItem.getText().trim();
        if (!searchText.isEmpty()) {
            List<ClinicInventory> filtered = inventoryDAO.searchItemsByName(searchText);
            tableInventory.setItems(FXCollections.observableArrayList(filtered));
        } else {
            refreshInventoryTable();
        }
    }

    @FXML
    private void handleRestock(ActionEvent event) {
        if (selectedItem == null) {
            showStatus("Please select an item to restock.", "#fca5a5");
            return;
        }

        String quantityText = txtQuantity.getText().trim();
        if (quantityText.isEmpty()) {
            showStatus("Please enter quantity to add.", "#fca5a5");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showStatus("Quantity must be a positive number.", "#fca5a5");
            return;
        }

        HealthPersonnel restocker = comboPersonnel.getValue();
        if (restocker == null) {
            showStatus("Please select personnel.", "#fca5a5");
            return;
        }

        String remarks = txtRemarks.getText().trim();

        try {
            // Update stock in clinicinventory table
            inventoryDAO.addQuantity(selectedItem.getItemId(), quantity);

            // Log restock in inventorymovement table
            InventoryMovementDAO movementDAO = new InventoryMovementDAO();
            movementDAO.insertRestockMovement(
                    selectedItem.getItemId(),
                    quantity,
                    restocker.getPersonnelId(),
                    remarks
            );
            // Refresh table
            refreshInventoryTable();

            // Clear fields
            txtQuantity.clear();
            txtRemarks.clear();
            comboPersonnel.getSelectionModel().clearSelection();
            lblSelectedItem.setText("No item selected");
            tableInventory.getSelectionModel().clearSelection();

            showStatus("Restock successful!", "#d1fae5"); // green
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error during restock: " + e.getMessage(), "#fca5a5");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) txtQuantity.getScene().getWindow();
        stage.close();
    }

    private void showStatus(String message, String color) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        lblStatus.setVisible(true);
    }
}