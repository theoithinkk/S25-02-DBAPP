package app.model;

import java.sql.Date;

public class ClinicVisits {
    private int visitId;
    private int residentId;
    private int personnelId;
    private String visitType;
    private String diagnosis;
    private String treatment;
    private String notes;
    private Date visitDate;

    // For display
    private String residentName;
    private String personnelName;

    public ClinicVisits() {}

    // Getters and setters
    public int getVisitId() { return visitId; }
    public void setVisitId(int visitId) { this.visitId = visitId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public int getPersonnelId() { return personnelId; }
    public void setPersonnelId(int personnelId) { this.personnelId = personnelId; }

    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getVisitDate() { return visitDate; }
    public void setVisitDate(Date visitDate) { this.visitDate = visitDate; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String personnelName) { this.personnelName = personnelName; }
}