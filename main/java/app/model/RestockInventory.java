package app.model;

import java.sql.Timestamp;

public class RestockInventory {

    private Integer restockId;
    private Integer itemId;
    private Integer quantityAdded;
    private Integer restockedBy; // nullable
    private Timestamp restockDate;
    private String remarks;

    public RestockInventory() {}

    public RestockInventory(Integer restockId, Integer itemId, Integer quantityAdded,
                            Integer restockedBy, Timestamp restockDate, String remarks) {
        this.restockId = restockId;
        this.itemId = itemId;
        this.quantityAdded = quantityAdded;
        this.restockedBy = restockedBy;
        this.restockDate = restockDate;
        this.remarks = remarks;
    }

    // Getters & Setters
    public Integer getRestockId() { return restockId; }
    public void setRestockId(Integer restockId) { this.restockId = restockId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(Integer quantityAdded) { this.quantityAdded = quantityAdded; }

    public Integer getRestockedBy() { return restockedBy; }
    public void setRestockedBy(Integer restockedBy) { this.restockedBy = restockedBy; }

    public Timestamp getRestockDate() { return restockDate; }
    public void setRestockDate(Timestamp restockDate) { this.restockDate = restockDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
