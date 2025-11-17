package app.model;

public class Resident {
    private int residentId;
    private String firstName;
    private String lastName;
    private int age;
    private String sex;
    private String contactNumber;
    private String address;
    private Integer householdId; // Nullable
    private String vulnerabilityStatus; // Nullable

    public Resident() {}

    public Resident(int residentId, String firstName, String lastName, int age, String sex,
                    String contactNumber, String address, Integer householdId, String vulnerabilityStatus) {
        this.residentId = residentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.address = address;
        this.householdId = householdId;
        this.vulnerabilityStatus = vulnerabilityStatus;
    }

    // Getters and setters
    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getHouseholdId() { return householdId; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }

    public String getVulnerabilityStatus() { return vulnerabilityStatus; }
    public void setVulnerabilityStatus(String vulnerabilityStatus) { this.vulnerabilityStatus = vulnerabilityStatus; }
}
