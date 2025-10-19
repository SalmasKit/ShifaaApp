
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.Database;

public class LogInDAO {

    private final Connection connection;

    public LogInDAO() throws SQLException {
        this.connection = new Database().getConnection();
    }

    // Vérifie si l'utilisateur existe avec cet email et mot de passe
    public boolean isValidUser(String email, String password) {
        String query = "SELECT * FROM doctors WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Un utilisateur trouvé
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Vérifie si l'email est déjà utilisé
    public boolean isEmailExist(String email) {
        String query = "SELECT id FROM doctors WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Email déjà existant
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Insère un nouveau docteur dans la base de données
    public boolean insertDoctor(Doctor doctor) {
        String query = "INSERT INTO doctors(firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getEmail());
            stmt.setString(4, doctor.getPassword());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Vérifie si l'insertion a réussi
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
   public int getDoctorIdByCredentials(String email, String password) {
    String sql = "SELECT id FROM doctors WHERE email = ? AND password = ?";

    try (Connection conn = new Database().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, email);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; // not found or error
}

 public int insertDoctorAndGetId(Doctor doctor) {
    String query = "INSERT INTO doctors(firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, doctor.getFirstName());
        stmt.setString(2, doctor.getLastName());
        stmt.setString(3, doctor.getEmail());
        stmt.setString(4, doctor.getPassword());

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected == 0) {
            throw new SQLException("Creating doctor failed, no rows affected.");
        }

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);  // Return the generated ID
            } else {
                throw new SQLException("Creating doctor failed, no ID obtained.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return -1;  // Return -1 if error
    }
}



}
      
