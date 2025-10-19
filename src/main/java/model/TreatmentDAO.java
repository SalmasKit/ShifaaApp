package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.Database;

public class TreatmentDAO {

    private final Connection connection;

    public TreatmentDAO() throws SQLException {
        Database db = new Database();
        this.connection = db.getConnection();
    }

        // Méthode pour ajouter un traitement dans la DB
    public boolean insertTreatment(Treatment treatment) {
        String query = "INSERT INTO treatments (treatmentName, prescription, duration, image) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, treatment.getTreatmentName());
            stmt.setString(2, treatment.getPrescription());
            stmt.setInt(3, treatment.getDuration());
            stmt.setString(4, treatment.getImage());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Récupérer tous les traitements
    public List<Treatment> getAllTreatments() {
        List<Treatment> treatments = new ArrayList<>();
        String query = "SELECT * FROM treatments";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Treatment treatment = new Treatment(
                    rs.getInt("id"),
                    rs.getString("treatmentName"),
                    rs.getString("prescription"),
                    rs.getInt("duration"),
                    rs.getString("image")
                );
                treatments.add(treatment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return treatments;
    }
            // Delete a treatment by id
        public boolean deleteTreatment(int id) {
            String query = "DELETE FROM treatments WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, id);
                int rowsDeleted = stmt.executeUpdate();
                return rowsDeleted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        public boolean updateTreatment(Treatment treatment) {
    String sql = "UPDATE treatments SET treatmentName = ?, prescription = ?, duration = ?, image = ? WHERE id = ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, treatment.getTreatmentName());
        stmt.setString(2, treatment.getPrescription());
        stmt.setInt(3, treatment.getDuration());
        stmt.setString(4, treatment.getImage());
        stmt.setInt(5, treatment.getId());
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}

