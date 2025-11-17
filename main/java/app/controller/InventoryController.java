package app.controller;

import app.dao.ClinicInventoryDAO;
import app.model.ClinicInventory;
import app.model.User;
import app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class InventoryController {

    @FXML private TableView<ClinicInventory> tableInventory;
    @FXML private TableColumn<ClinicInventory, Integer> colId;
    @FXML private TableColumn<ClinicInventory, String> colName;
    @FXML private TableColumn<ClinicInventory, String> colCategory;
    @FXML private TableColumn<ClinicInventory, Integer> colQuantity;

    @FXML private TextField txtItemName, txtCategory, txtQuantity;
    @FXML private Label lblItemCount;
    @FXML private Button btnDelete;

    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getItemId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colQuantity.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));

        refreshInventory();

        // Configure permissions
        configurePermissions();

        tableInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtItemName.setText(newSel.getItemName());
                txtCategory.setText(newSel.getCategory());
                txtQuantity.setText(String.valueOf(newSel.getQuantity()));
            }
        });
    }

    private void configurePermissions() {
        // Hide delete button for Staff (Admin and Personnel can delete inventory)
        if (btnDelete != null) {
            boolean canDelete = SessionManager.getCurrentUser() != null &&
                    SessionManager.getCurrentUser().canDeleteInventory();
            btnDelete.setVisible(canDelete);
            btnDelete.setManaged(canDelete);
        }
    }

    @FXML
    private void addItem() {
        if (!validateInput()) {
            return;
        }

        try {
            ClinicInventory item = new ClinicInventory();
            item.setItemName(txtItemName.getText().trim());
            item.setCategory(txtCategory.getText().trim());
            item.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

            if (inventoryDAO.addItem(item)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Item added successfully!");
                refreshInventory();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add item.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid quantity.");
        }
    }

    @FXML
    private void updateItem() {
        ClinicInventory selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select an item to update.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selected.setItemName(txtItemName.getText().trim());
            selected.setCategory(txtCategory.getText().trim());
            selected.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

            if (inventoryDAO.updateItem(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Item updated successfully!");
                refreshInventory();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update item.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid quantity.");
        }
    }

    @FXML
    private void deleteItem() {
        // Double-check permission
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.canDeleteInventory()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Admins and Personnel can delete inventory items.");
            return;
        }

        ClinicInventory selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select an item to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this item?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && inventoryDAO.deleteItem(selected.getItemId())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Item deleted successfully!");
                refreshInventory();
                clearFields();
            }
        });
    }

    @FXML
    private void refreshInventory() {
        List<ClinicInventory> list = inventoryDAO.getAllItems();
        ObservableList<ClinicInventory> obsList = FXCollections.observableArrayList(list);
        tableInventory.setItems(obsList);

        // Update count
        if (lblItemCount != null) {
            lblItemCount.setText(list.size() + " item" + (list.size() != 1 ? "s" : ""));
        }
    }

    @FXML
    private void restockInventory(){
        ClinicInventory selected = tableInventory.getSelectionModel().getSelectedItem();
        if(selected==null){
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select item to restock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restocking...");
        dialog.setHeaderText("Restocking: " + selected.getItemName());
        dialog.setContentText("Enter quantity to add: ");

        dialog.showAndWait().ifPresent(input -> {
            try{
                int qty = Integer.parseInt(input);

                if(qty <= 0){
                    showAlert(Alert.AlertType.WARNING, "Invalid input", "Quantity must be greater than 0.");
                    return;
                }

                selected.setQuantity(selected.getQuantity() + qty);

                if(inventoryDAO.updateItem(selected)){
                    showAlert(Alert.AlertType.INFORMATION, "Restocked", "Item was restocked successfully.");
                    refreshInventory();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to restock item.");
                }
            } catch (NumberFormatException e){
                showAlert(Alert.AlertType.ERROR, "Invalid input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void clearFields() {
        txtItemName.clear();
        txtCategory.clear();
        txtQuantity.clear();
        tableInventory.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtItemName.getText().trim().isEmpty()) {
            errors.append("• Item name is required\n");
        }
        if (txtCategory.getText().trim().isEmpty()) {
            errors.append("• Category is required\n");
        }
        if (txtQuantity.getText().trim().isEmpty()) {
            errors.append("• Quantity is required\n");
        } else {
            try {
                int qty = Integer.parseInt(txtQuantity.getText().trim());
                if (qty < 0) {
                    errors.append("• Quantity must be 0 or greater\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Quantity must be a valid number\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please fix the following:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}