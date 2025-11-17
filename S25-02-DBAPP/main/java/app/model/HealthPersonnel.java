package app.model;

public class HealthPersonnel {
    private int personnelId;
    private String lastName;
    private String firstName;
    private String role;
    private String specialization;
    private String contactNumber;

    public HealthPersonnel(int personnelId, String lastName, String firstName,
                           String role, String specialization, String contactNumber) {
        this.personnelId = personnelId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.role = role;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
    }

    public HealthPersonnel() {}

    public int getPersonnelId() { return personnelId; }
    public void setPersonnelId(int personnelId) { this.personnelId = personnelId; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
