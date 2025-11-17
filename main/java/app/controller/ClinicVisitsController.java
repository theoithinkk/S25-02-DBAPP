package app.controller;

import app.dao.*;
import app.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class ClinicVisitsController {

    @FXML private ComboBox<Resident> residentCombo;
    @FXML private ComboBox<HealthPersonnel> personnelCombo;
    @FXML private ComboBox<String> visitTypeCombo;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea treatmentArea;
    @FXML private TextArea notesArea;
    @FXML private DatePicker visitDatePicker;
    @FXML private Label lblStatus;

    private final ClinicVisitsDAO clinicVisitsDAO = new ClinicVisitsDAO();
    private final ResidentDAO residentsDAO = new ResidentDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {
        setupForm();
    }

    private void setupForm() {
        // Load residents and personnel
        residentCombo.setItems(FXCollections.observableArrayList(residentsDAO.getAllResidents()));
        personnelCombo.setItems(FXCollections.observableArrayList(personnelDAO.getAllPersonnel()));

        // Set up visit type options
        visitTypeCombo.setItems(FXCollections.observableArrayList("WALK_IN", "SCHEDULED"));
        visitTypeCombo.setValue("WALK_IN");

        // Set default date to today
        visitDatePicker.setValue(LocalDate.now());

        // Set up converters for combo boxes
        residentCombo.setConverter(new javafx.util.StringConverter<Resident>() {
            @Override
            public String toString(Resident resident) {
                return resident == null ? "" : resident.getFirstName() + " " + resident.getLastName() +
                        " (ID: " + resident.getResidentId() + ")";
            }
            @Override
            public Resident fromString(String string) { return null; }
        });

        personnelCombo.setConverter(new javafx.util.StringConverter<HealthPersonnel>() {
            @Override
            public String toString(HealthPersonnel personnel) {
                return personnel == null ? "" : personnel.getFirstName() + " " + personnel.getLastName() +
                        " - " + personnel.getRole();
            }
            @Override
            public HealthPersonnel fromString(String string) { return null; }
        });
    }

    @FXML
    private void handleSaveVisit() {
        // Validation
        if (residentCombo.getValue() == null) {
            showStatus("Please select a patient", true);
            return;
        }

        if (personnelCombo.getValue() == null) {
            showStatus("Please select attending personnel", true);
            return;
        }

        if (diagnosisArea.getText().trim().isEmpty()) {
            showStatus("Please enter a diagnosis", true);
            return;
        }

        if (visitDatePicker.getValue() == null) {
            showStatus("Please select visit date", true);
            return;
        }

        // Create clinic visit
        ClinicVisits clinicVisit = new ClinicVisits();
        clinicVisit.setResidentId(residentCombo.getValue().getResidentId());
        clinicVisit.setPersonnelId(personnelCombo.getValue().getPersonnelId());
        clinicVisit.setVisitType(visitTypeCombo.getValue());
        clinicVisit.setDiagnosis(diagnosisArea.getText().trim());
        clinicVisit.setTreatment(treatmentArea.getText().trim());
        clinicVisit.setNotes(notesArea.getText().trim());
        clinicVisit.setVisitDate(Date.valueOf(visitDatePicker.getValue()));

        // Save to database
        if (clinicVisitsDAO.addClinicVisit(clinicVisit)) {
            showStatus("✓ Clinic visit recorded successfully!", false);
            clearForm();

            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Clinic Visit Recorded");
            alert.setContentText("The clinic visit has been successfully recorded in the system.");
            alert.showAndWait();

        } else {
            showStatus("Failed to save clinic visit. Please try again.", true);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) residentCombo.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        residentCombo.setValue(null);
        personnelCombo.setValue(null);
        visitTypeCombo.setValue("WALK_IN");
        diagnosisArea.clear();
        treatmentArea.clear();
        notesArea.clear();
        visitDatePicker.setValue(LocalDate.now());
    }

    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError ?
                "-fx-text-fill: #e74c3c; -fx-background-color: #fdeded; -fx-border-color: #e74c3c; -fx-padding: 10; -fx-background-radius: 5;" :
                "-fx-text-fill: #27ae60; -fx-background-color: #edf7ed; -fx-border-color: #27ae60; -fx-padding: 10; -fx-background-radius: 5;");
        lblStatus.setVisible(true);
    }
}