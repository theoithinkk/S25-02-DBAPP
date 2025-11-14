package app.dao;

import app.model.ClinicVisits;
import app.dao.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClinicVisitsDAO {

    public static boolean addClinicVisit(ClinicVisits visit) {
        String sql = "INSERT INTO ClinicVisits (resident_id, personnel_id, visit_type, diagnosis, treatment, notes, visit_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, visit.getResidentId());
            stmt.setInt(2, visit.getPersonnelId());
            stmt.setString(3, visit.getVisitType());
            stmt.setString(4, visit.getDiagnosis());
            stmt.setString(5, visit.getTreatment());
            stmt.setString(6, visit.getNotes());
            stmt.setDate(7, visit.getVisitDate());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClinicVisits> getAllClinicVisits() {
        List<ClinicVisits> visits = new ArrayList<>();
        String sql = "SELECT cv.*, r.first_name, r.last_name, " +
                "p.first_name as p_first_name, p.last_name as p_last_name " +
                "FROM ClinicVisits cv " +
                "LEFT JOIN Residents r ON cv.resident_id = r.resident_id " +
                "LEFT JOIN HealthPersonnel p ON cv.personnel_id = p.personnel_id " +
                "ORDER BY cv.visit_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClinicVisits visit = new ClinicVisits();
                visit.setVisitId(rs.getInt("visit_id"));
                visit.setResidentId(rs.getInt("resident_id"));
                visit.setPersonnelId(rs.getInt("personnel_id"));
                visit.setVisitType(rs.getString("visit_type"));
                visit.setDiagnosis(rs.getString("diagnosis"));
                visit.setTreatment(rs.getString("treatment"));
                visit.setNotes(rs.getString("notes"));
                visit.setVisitDate(rs.getDate("visit_date"));

                // Set display names
                String residentFirstName = rs.getString("first_name");
                String residentLastName = rs.getString("last_name");
                String personnelFirstName = rs.getString("p_first_name");
                String personnelLastName = rs.getString("p_last_name");

                visit.setResidentName((residentFirstName != null ? residentFirstName : "") + " " +
                        (residentLastName != null ? residentLastName : ""));
                visit.setPersonnelName((personnelFirstName != null ? personnelFirstName : "") + " " +
                        (personnelLastName != null ? personnelLastName : ""));

                visits.add(visit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visits;
    }

    public boolean updateClinicVisit(ClinicVisits visit) {
        String sql = "UPDATE ClinicVisits SET resident_id=?, personnel_id=?, visit_type=?, " +
                "diagnosis=?, treatment=?, notes=?, visit_date=? WHERE visit_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, visit.getResidentId());
            stmt.setInt(2, visit.getPersonnelId());
            stmt.setString(3, visit.getVisitType());
            stmt.setString(4, visit.getDiagnosis());
            stmt.setString(5, visit.getTreatment());
            stmt.setString(6, visit.getNotes());
            stmt.setDate(7, visit.getVisitDate());
            stmt.setInt(8, visit.getVisitId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClinicVisit(int visitId) {
        String sql = "DELETE FROM ClinicVisits WHERE visit_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, visitId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}