package app.model;

import java.util.Date;

public class ClinicInventory {
    private int itemId;
    private int id;
    private String itemName;
    private int quantity;
    private Date expirationDate;
    private String category;

    public ClinicInventory(int itemId, String itemName, String category, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
    }

    public ClinicInventory() {
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void deductQuantity(int quantityToDeduct) {
        this.quantity -= quantityToDeduct;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
