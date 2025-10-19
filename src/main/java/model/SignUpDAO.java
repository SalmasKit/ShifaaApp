/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import utils.Database;


public class SignUpDAO {

    private final Connection connection;

    public SignUpDAO() throws SQLException {
        Database db = new Database();
        this.connection = db.getConnection();
    }

    public boolean registerUser(SignUp signup) {
        String query = "INSERT INTO doctors (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, signup.getFirstName());
            stmt.setString(2, signup.getLastName());
            stmt.setString(3, signup.getEmail());
            stmt.setString(4, signup.getPassword());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
 
    }
    
    public int insertDoctorAndGetId(Doctor doctor) {
    int generatedId = -1;
    String sql = "INSERT INTO doctors (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
    Database db = new Database();
    try (Connection conn = db.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        ps.setString(1, doctor.getFirstName());
        ps.setString(2, doctor.getLastName());
        ps.setString(3, doctor.getEmail());
        ps.setString(4, doctor.getPassword());

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Inserting doctor failed, no rows affected.");
        }

        try (var generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Inserting doctor failed, no ID obtained.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return generatedId;
}


}
