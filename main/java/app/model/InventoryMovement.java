package app.model;

import java.sql.Timestamp;

public class InventoryMovement {

    private Integer movementId;
    private Integer itemId;
    private String movementType; // ENUM: RESTOCK, ISSUE, SERVICE
    private Integer quantity;
    private Integer actorId; // personnel_id from healthpersonnel table
    private Integer residentId; // nullable
    private Timestamp movementDate;
    private String remarks;

    public InventoryMovement() {}

    public InventoryMovement(Integer movementId, Integer itemId, String movementType,
                            Integer quantity, Integer actorId, Integer residentId,
                            Timestamp movementDate, String remarks) {
        this.movementId = movementId;
        this.itemId = itemId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.actorId = actorId;
        this.residentId = residentId;
        this.movementDate = movementDate;
        this.remarks = remarks;
    }

    // Getters & Setters
    public Integer getMovementId() { return movementId; }
    public void setMovementId(Integer movementId) { this.movementId = movementId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getActorId() { return actorId; }
    public void setActorId(Integer actorId) { this.actorId = actorId; }

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }

    public Timestamp getMovementDate() { return movementDate; }
    public void setMovementDate(Timestamp movementDate) { this.movementDate = movementDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
