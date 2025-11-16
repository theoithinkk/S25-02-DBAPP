package app.model;

public class HealthService {
    private int serviceId;
    private String serviceType;
    private String description;
    private double fee;
    private String remarks;

    public HealthService(int serviceId, String serviceType, String description, double fee, String remarks) {
        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.description = description;
        this.fee = fee;
        this.remarks = remarks;
    }

    public HealthService() {}

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
