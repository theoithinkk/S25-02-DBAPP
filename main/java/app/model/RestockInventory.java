package app.model;

import java.sql.Timestamp;

public class RestockInventory {

    private int restockId;
    private int itemId;
    private int quantityAdded;
    private Integer restockedBy;
    private Timestamp restockDate;
    private String remarks;

    public RestockInventory() {}

    public RestockInventory(int restockId, int itemId, int quantityAdded,
                            Integer restockedBy, Timestamp restockDate, String remarks) {
        this.restockId = restockId;
        this.itemId = itemId;
        this.quantityAdded = quantityAdded;
        this.restockedBy = restockedBy;
        this.restockDate = restockDate;
        this.remarks = remarks;
    }

    public int getRestockId() { return restockId; }
    public void setRestockId(int restockId) { this.restockId = restockId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(int quantityAdded) { this.quantityAdded = quantityAdded; }

    public Integer getRestockedBy() { return restockedBy; }
    public void setRestockedBy(Integer restockedBy) { this.restockedBy = restockedBy; }

    public Timestamp getRestockDate() { return restockDate; }
    public void setRestockDate(Timestamp restockDate) { this.restockDate = restockDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}