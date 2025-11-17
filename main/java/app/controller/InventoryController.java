package app.controller;

import app.dao.ClinicInventoryDAO;
import app.model.ClinicInventory;
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

    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getItemId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colQuantity.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));

        refreshInventory();

        tableInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtItemName.setText(newSel.getItemName());
                txtCategory.setText(newSel.getCategory());
                txtQuantity.setText(String.valueOf(newSel.getQuantity()));
            }
        });
    }

    @FXML
    private void addItem() {
        ClinicInventory item = new ClinicInventory();
        item.setItemName(txtItemName.getText());
        item.setCategory(txtCategory.getText());
        item.setQuantity(Integer.parseInt(txtQuantity.getText()));

        if (inventoryDAO.addItem(item)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Item added successfully!");
            refreshInventory();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add item.");
        }
    }

    @FXML
    private void updateItem() {
        ClinicInventory selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select an item to update.");
            return;
        }

        selected.setItemName(txtItemName.getText());
        selected.setCategory(txtCategory.getText());
        selected.setQuantity(Integer.parseInt(txtQuantity.getText()));

        if (inventoryDAO.updateItem(selected)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Item updated successfully!");
            refreshInventory();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update item.");
        }
    }

    @FXML
    private void deleteItem() {
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
        dialog.setContentText("Enter new quantity: ");

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
                showAlert(Alert.AlertType.ERROR, "Invalid input", "Please enter a valid input.");
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
