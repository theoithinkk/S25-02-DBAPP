package app.controller;

import app.dao.*;
import app.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date; //
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ClinicVisitsController {

    @FXML private TextField txtSearchPatient;
    @FXML private TableView<Resident> tablePatients;
    @FXML private TableColumn<Resident, Integer> colPatientId;
    @FXML private TableColumn<Resident, String> colPatientName;
    @FXML private TableColumn<Resident, Integer> colAge;
    @FXML private TableColumn<Resident, String> colSex;
    @FXML private TableColumn<Resident, String> colContact;
    @FXML private Label lblSelectedPatient;

    @FXML private TextField txtSearchPersonnel;
    @FXML private TableView<HealthPersonnel> tablePersonnel;
    @FXML private TableColumn<HealthPersonnel, Integer> colPersonnelId;
    @FXML private TableColumn<HealthPersonnel, String> colPersonnelName;
    @FXML private TableColumn<HealthPersonnel, String> colRole;
    @FXML private Label lblSelectedPersonnel;

    @FXML private ComboBox<String> visitTypeCombo;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea treatmentArea;
    @FXML private TextArea notesArea;
    @FXML private DatePicker visitDatePicker;
    @FXML private Label lblStatus;

    private Resident selectedPatient = null;
    private HealthPersonnel selectedPersonnel = null;

    private final ClinicVisitsDAO clinicVisitsDAO = new ClinicVisitsDAO();
    private final ResidentDAO residentsDAO = new ResidentDAO();
    private final HealthPersonnelDAO personnelDAO = new HealthPersonnelDAO();

    @FXML
    private void initialize() {

        colPatientId.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getResidentId()));
        colPatientName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colAge.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getAge()));
        colSex.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSex()));
        colContact.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getContactNumber()));

        loadAllResidents();

        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedPatient = newV;
                lblSelectedPatient.setText(
                        "✓ Selected: " + newV.getFirstName() + " " + newV.getLastName() +
                                " (ID: " + newV.getResidentId() + ")"
                );
                lblSelectedPatient.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        colPersonnelId.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getPersonnelId()));
        colPersonnelName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colRole.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole()));

        loadAllPersonnel();

        tablePersonnel.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedPersonnel = newV;
                lblSelectedPersonnel.setText(
                        "✓ Selected: " + newV.getFirstName() + " " + newV.getLastName() +
                                " (" + newV.getRole() + ")"
                );
                lblSelectedPersonnel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        visitTypeCombo.setItems(FXCollections.observableArrayList("WALK_IN", "SCHEDULED"));
        visitTypeCombo.setValue("WALK_IN");

        visitDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSearchPatient() {
        String search = txtSearchPatient.getText().trim().toLowerCase();

        List<Resident> all = residentsDAO.getAllResidents();

        List<Resident> filtered = all.stream()
                .filter(r ->
                        r.getFirstName().toLowerCase().contains(search) ||
                                r.getLastName().toLowerCase().contains(search) ||
                                String.valueOf(r.getResidentId()).contains(search)
                )
                .collect(Collectors.toList());

        tablePatients.setItems(FXCollections.observableArrayList(filtered));
    }

    private void loadAllResidents() {
        tablePatients.setItems(FXCollections.observableArrayList(residentsDAO.getAllResidents()));
    }

    @FXML
    private void handleSearchPersonnel() {
        String search = txtSearchPersonnel.getText().trim().toLowerCase();

        List<HealthPersonnel> all = personnelDAO.getAllPersonnel();

        List<HealthPersonnel> filtered = all.stream()
                .filter(p ->
                        p.getFirstName().toLowerCase().contains(search) ||
                                p.getLastName().toLowerCase().contains(search) ||
                                p.getRole().toLowerCase().contains(search) ||
                                String.valueOf(p.getPersonnelId()).contains(search)
                )
                .collect(Collectors.toList());

        tablePersonnel.setItems(FXCollections.observableArrayList(filtered));
    }

    private void loadAllPersonnel() {
        tablePersonnel.setItems(FXCollections.observableArrayList(personnelDAO.getAllPersonnel()));
    }

    @FXML
    private void handleSaveVisit() {
        if (selectedPatient == null) {
            showStatus("Please select a patient", true);
            return;
        }
        if (selectedPersonnel == null) {
            showStatus("Please select attending personnel", true);
            return;
        }
        if (diagnosisArea.getText().trim().isEmpty()) {
            showStatus("Diagnosis required", true);
            return;
        }
        if (visitDatePicker.getValue() == null) {
            showStatus("Select visit date", true);
            return;
        }

        ClinicVisits visit = new ClinicVisits();
        visit.setResidentId(selectedPatient.getResidentId());
        visit.setPersonnelId(selectedPersonnel.getPersonnelId());
        visit.setVisitType(visitTypeCombo.getValue());
        visit.setDiagnosis(diagnosisArea.getText().trim());
        visit.setTreatment(treatmentArea.getText().trim());
        visit.setNotes(notesArea.getText().trim());
        visit.setVisitDate(Date.valueOf(visitDatePicker.getValue()));

        if (clinicVisitsDAO.addClinicVisit(visit)) {
            showStatus("✓ Clinic visit saved successfully!", false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Visit Recorded");
            alert.setHeaderText("Clinic Visit Successfully Recorded");
            alert.setContentText(
                    "Patient: " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName() + "\n" +
                            "Personnel: " + selectedPersonnel.getFirstName() + " " + selectedPersonnel.getLastName() + "\n" +
                            "Visit Type: " + visitTypeCombo.getValue() + "\n" +
                            "Date: " + visitDatePicker.getValue() + "\n" +
                            "Diagnosis: " + diagnosisArea.getText().trim()
            );
            alert.showAndWait();

            clearForm();
        } else {
            showStatus("Failed to save visit", true);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtSearchPatient.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        selectedPatient = null;
        selectedPersonnel = null;
        lblSelectedPatient.setText("No patient selected");
        lblSelectedPatient.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: normal;");
        lblSelectedPersonnel.setText("No personnel selected");
        lblSelectedPersonnel.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: normal;");

        diagnosisArea.clear();
        treatmentArea.clear();
        notesArea.clear();
        visitDatePicker.setValue(LocalDate.now());

        loadAllResidents();
        loadAllPersonnel();
    }

    private void showStatus(String msg, boolean error) {
        lblStatus.setText(msg);
        lblStatus.setStyle(error ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblStatus.setVisible(true);
    }
}