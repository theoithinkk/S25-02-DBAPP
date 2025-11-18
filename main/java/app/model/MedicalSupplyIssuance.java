package app.model;

import java.sql.Date;

public class MedicalSupplyIssuance {
    private int issuanceId;
    private int residentId;
    private int itemId;
    private int personnelId;
    private int quantityIssued;
    private Date issuanceDate;
    private String remarks;

    // Derived fields for display
    private String residentName;
    private String itemName;
    private String personnelName;

    // Constructors
    public MedicalSupplyIssuance() {}

    public MedicalSupplyIssuance(int residentId, int itemId, int personnelId, int quantityIssued, Date issuanceDate, String remarks) {
        this.residentId = residentId;
        this.itemId = itemId;
        this.personnelId = personnelId;
        this.quantityIssued = quantityIssued;
        this.issuanceDate = issuanceDate;
        this.remarks = remarks;
    }

    // Getters and setters
    public int getIssuanceId() { return issuanceId; }
    public void setIssuanceId(int issuanceId) { this.issuanceId = issuanceId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getPersonnelId() { return personnelId; }
    public void setPersonnelId(int personnelId) { this.personnelId = personnelId; }

    public int getQuantityIssued() { return quantityIssued; }
    public void setQuantityIssued(int quantityIssued) { this.quantityIssued = quantityIssued; }

    public Date getIssuanceDate() { return issuanceDate; }
    public void setIssuanceDate(Date issuanceDate) { this.issuanceDate = issuanceDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String personnelName) { this.personnelName = personnelName; }
}