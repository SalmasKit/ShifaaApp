/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.Database;


public class DoctorDAO {

    private final Connection connection;

    public DoctorDAO() throws SQLException {
        Database db = new Database();
        this.connection = db.getConnection();
    }

    public boolean addDoctor(Doctor doctor) {
        String query = "INSERT INTO doctors (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getEmail());
            stmt.setString(4, doctor.getPassword());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Doctor getDoctorById(int id) {
    Doctor doctor = null;
    String query = "SELECT * FROM doctors WHERE id = ?"; // Adapt table name if needed

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            doctor = new Doctor(
                rs.getInt("id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getString("email"),
                rs.getString("password")
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return doctor;
}

}
