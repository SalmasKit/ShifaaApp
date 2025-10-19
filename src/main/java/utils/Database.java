package utils;

import model.Patient;
import model.Treatment;
import model.Doctor;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/shifaa";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Charge tous les patients depuis la base
    public static List<Patient> loadAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Patient p = new Patient(
                    rs.getInt("id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("bloodGroup"),
                    rs.getString("disease"),
                    rs.getString("address"),
                    rs.getInt("doctorId"),
                    rs.getDate("appointment_date") != null ? rs.getDate("appointment_date").toLocalDate() : null,
                    rs.getTime("appointment_time") != null ? rs.getTime("appointment_time").toLocalTime() : null,
                    rs.getString("appointment_type")
                );
                patients.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    // Charge tous les traitements depuis la base
    public static List<Treatment> loadAllTreatments() {
        List<Treatment> treatments = new ArrayList<>();
        String sql = "SELECT * FROM treatments";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Treatment t = new Treatment(
                    rs.getInt("id"),
                    rs.getString("treatmentName"),
                    rs.getString("prescription"),
                    rs.getInt("duration"),
                    rs.getString("image")
                );
                treatments.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return treatments;
    }

    // Charge tous les médecins depuis la base
   public static List<Doctor> loadAllDoctors() {
    List<Doctor> doctors = new ArrayList<>();
    String sql = "SELECT id, firstname, lastname, email, password FROM doctors";  // nom exact des colonnes et table

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Doctor d = new Doctor(
                rs.getInt("id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getString("email"),
                rs.getString("password")
            );
            doctors.add(d);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return doctors;
}

}