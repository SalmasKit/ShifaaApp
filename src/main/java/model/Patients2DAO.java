package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Patients2.PatientTreatment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO pour récupérer les traitements d’un patient avec le nom du traitement.
 */
public class Patients2DAO {

    private final Connection connection;

    public Patients2DAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Retourne la liste des traitements (PatientTreatment) pour un patient donné,
     * avec le nom du traitement récupéré via une jointure.
     *
     * @param patientId Identifiant du patient
     * @return Liste observable de PatientTreatment
     */
    public ObservableList<PatientTreatment> getTreatmentsByPatientId(int patientId) {
        ObservableList<PatientTreatment> treatments = FXCollections.observableArrayList();

        String sql = """
            SELECT pt.patient_id, t.treatmentName, pt.start_date, pt.end_date
            FROM patients_treatments pt
            JOIN treatments t ON pt.treatment_id = t.id
            WHERE pt.patient_id = ?
            ORDER BY pt.start_date ASC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("patient_id");
                    String treatmentName = rs.getString("treatmentName");

                    Date sqlStart = rs.getDate("start_date");
                    Date sqlEnd = rs.getDate("end_date");

                    String startDate = (sqlStart != null) ? sqlStart.toLocalDate().toString() : null;
                    String endDate = (sqlEnd != null) ? sqlEnd.toLocalDate().toString() : null;

                    PatientTreatment treatment = new PatientTreatment(pid, treatmentName, startDate, endDate);
                    treatments.add(treatment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return treatments;
    }
}
