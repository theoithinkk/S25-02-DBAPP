package app.model;

import java.sql.Date;

public class ServiceTransaction {
    private int transactionId;
    private int serviceId;
    private int residentId;
    private int personnelId;
    private Date dateProvided;
    private String remarks;

    // Derived display fields
    private String residentName;
    private String serviceType;
    private String personnelName;

    // Getters & Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int id) { this.transactionId = id; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int id) { this.serviceId = id; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int id) { this.residentId = id; }

    public int getPersonnelId() { return personnelId; }
    public void setPersonnelId(int id) { this.personnelId = id; }

    public Date getDateProvided() { return dateProvided; }
    public void setDateProvided(Date date) { this.dateProvided = date; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String name) { this.residentName = name; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String type) { this.serviceType = type; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String name) { this.personnelName = name; }
}
