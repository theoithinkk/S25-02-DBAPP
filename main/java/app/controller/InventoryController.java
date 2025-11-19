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
    @FXML private TableColumn<ClinicInventory, String> colExpirationDate;

    @FXML private TextField txtItemName, txtCategory, txtQuantity;
    @FXML private DatePicker dateExpiration;
    @FXML private Label lblItemCount;
    @FXML private Button btnDelete;

    private final ClinicInventoryDAO inventoryDAO = new ClinicInventoryDAO();

    @FXML
    private void initialize() {
        setupTable();
        refreshInventory();
        configurePermissions();
        checkExpiringSoon();

        // Load selection into input fields
        tableInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                txtItemName.setText(selected.getItemName());
                txtCategory.setText(selected.getCategory());
                txtQuantity.setText(String.valueOf(selected.getQuantity()));

                if (selected.getExpirationDate() != null) {
                    java.sql.Date sqlDate = new java.sql.Date(selected.getExpirationDate().getTime());
                    dateExpiration.setValue(sqlDate.toLocalDate());
                } else {
                    dateExpiration.setValue(null);
                }
            }
        });

        tableInventory.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ClinicInventory item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getExpirationDate() == null) {
                    setStyle("");
                    return;
                }

                java.util.Date exp = item.getExpirationDate();
                java.util.Date today = new java.util.Date();

                long diff = exp.getTime() - today.getTime();
                long daysLeft = diff / (1000 * 60 * 60 * 24);

                if (daysLeft < 0) {
                    // EXPIRED (red)
                    setStyle("-fx-background-color: #ffcccc;");
                } else if (daysLeft <= 7) {
                    // Expiring within 7 days (yellow)
                    setStyle("-fx-background-color: #fff2cc;");
                } else {
                    setStyle(""); // normal
                }
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getItemId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colQuantity.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        colExpirationDate.setCellValueFactory(data -> {
            if (data.getValue().getExpirationDate() == null) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }

            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String formatted = fmt.format(data.getValue().getExpirationDate());

            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
    }

    private void configurePermissions() {
        if (btnDelete != null) {
            boolean canDelete = SessionManager.getCurrentUser() != null &&
                    SessionManager.getCurrentUser().canDeleteInventory();
            btnDelete.setVisible(canDelete);
            btnDelete.setManaged(canDelete);
        }
    }

    @FXML
    private void addItem() {
        if (!validateInput()) return;

        try {
            ClinicInventory item = new ClinicInventory();
            item.setItemName(txtItemName.getText().trim());
            item.setCategory(txtCategory.getText().trim());
            item.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

            if (dateExpiration.getValue() != null) {
                item.setExpirationDate(java.sql.Date.valueOf(dateExpiration.getValue()));
            }

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

        if (!validateInput()) return;

        try {
            selected.setItemName(txtItemName.getText().trim());
            selected.setCategory(txtCategory.getText().trim());
            selected.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

            if (dateExpiration.getValue() != null) {
                selected.setExpirationDate(java.sql.Date.valueOf(dateExpiration.getValue()));
            } else {
                selected.setExpirationDate(null);
            }

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

        if (lblItemCount != null) {
            lblItemCount.setText(list.size() + " item" + (list.size() != 1 ? "s" : ""));
        }
    }

    @FXML
    private void restockInventory() {
        ClinicInventory selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select item to restock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restocking...");
        dialog.setHeaderText("Restocking: " + selected.getItemName());
        dialog.setContentText("Enter quantity to add:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input);
                if (qty <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Invalid input", "Quantity must be greater than 0.");
                    return;
                }

                selected.setQuantity(selected.getQuantity() + qty);

                if (inventoryDAO.updateItem(selected)) {
                    showAlert(Alert.AlertType.INFORMATION, "Restocked", "Item was restocked successfully.");
                    refreshInventory();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to restock item.");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void filterExpiringSoon() {
        List<ClinicInventory> all = inventoryDAO.getAllItems();
        ObservableList<ClinicInventory> filtered = FXCollections.observableArrayList();

        java.util.Date today = new java.util.Date();

        for (ClinicInventory item : all) {
            if (item.getExpirationDate() == null) continue;

            long diff = item.getExpirationDate().getTime() - today.getTime();
            long daysLeft = diff / (1000 * 60 * 60 * 24);

            if (daysLeft >= 0 && daysLeft <= 7) {
                filtered.add(item);
            }
        }

        tableInventory.setItems(filtered);
    }

    @FXML
    private void filterExpired() {
        List<ClinicInventory> all = inventoryDAO.getAllItems();
        ObservableList<ClinicInventory> filtered = FXCollections.observableArrayList();

        java.util.Date today = new java.util.Date();

        for (ClinicInventory item : all) {
            if (item.getExpirationDate() == null) continue;

            long diff = item.getExpirationDate().getTime() - today.getTime();
            long daysLeft = diff / (1000 * 60 * 60 * 24);

            if (daysLeft < 0) {
                filtered.add(item);
            }
        }

        tableInventory.setItems(filtered);
    }

    private void checkExpiringSoon() {
        List<ClinicInventory> all = inventoryDAO.getAllItems();

        StringBuilder warning = new StringBuilder();

        java.util.Date today = new java.util.Date();

        for (ClinicInventory item : all) {
            if (item.getExpirationDate() == null) continue;

            long diff = item.getExpirationDate().getTime() - today.getTime();
            long daysLeft = diff / (1000 * 60 * 60 * 24);

            if (daysLeft >= 0 && daysLeft <= 3) {
                warning.append(String.format("• %s expires in %d day(s)\n",
                        item.getItemName(), daysLeft));
            }
        }

        if (warning.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Expiration Warning");
            alert.setHeaderText("Items expiring soon!");
            alert.setContentText(warning.toString());
            alert.showAndWait();
        }
    }


    @FXML
    private void clearFields() {
        txtItemName.clear();
        txtCategory.clear();
        txtQuantity.clear();
        dateExpiration.setValue(null);
        tableInventory.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtItemName.getText().trim().isEmpty()) errors.append("• Item name is required\n");
        if (txtCategory.getText().trim().isEmpty()) errors.append("• Category is required\n");

        if (txtQuantity.getText().trim().isEmpty()) {
            errors.append("• Quantity is required\n");
        } else {
            try {
                int qty = Integer.parseInt(txtQuantity.getText().trim());
                if (qty < 0) errors.append("• Quantity must be 0 or greater\n");
            } catch (NumberFormatException e) {
                errors.append("• Quantity must be a valid number\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fix the following:\n\n" + errors);
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
