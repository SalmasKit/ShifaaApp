package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import utils.Database;

public class PatientDAO {

    private final Connection connection;

    public PatientDAO() throws SQLException {
        this.connection = Database.getConnection();
    }

    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients "
                   + "(firstname, lastname, age, gender, phone, email, bloodgroup, disease, address, doctorID, "
                   + "appointment_date, appointment_time, appointment_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setInt(3, patient.getAge());
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getPhoneNumber());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getBloodGroup());
            stmt.setString(8, patient.getDisease());
            stmt.setString(9, patient.getAddress());
            stmt.setInt(10, patient.getDoctorId());

            // appointment_date (never null because validated earlier)
            stmt.setDate(11, Date.valueOf(patient.getAppointmentDate()));

            // appointment_time (may be null)
            if (patient.getAppointmentTime() != null) {
                stmt.setTime(12, Time.valueOf(patient.getAppointmentTime()));
            } else {
                stmt.setNull(12, Types.TIME);
            }

            // appointment_type (may be null)
            if (patient.getAppointmentType() != null) {
                stmt.setString(13, patient.getAppointmentType());
            } else {
                stmt.setNull(13, Types.VARCHAR);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE doctorID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("appointment_date");
                    LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    Time sqlTime = rs.getTime("appointment_time");
                    LocalTime time = (sqlTime != null) ? sqlTime.toLocalTime() : null;

                    String type = rs.getString("appointment_type");

                    Patient p = new Patient(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("bloodgroup"),
                        rs.getString("disease"),
                        rs.getString("address"),
                        rs.getInt("doctorID"),
                        date,
                        time,
                        type
                    );
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET firstname=?, lastname=?, age=?, gender=?, phone=?, email=?, "
                   + "bloodgroup=?, disease=?, address=?, appointment_date=?, appointment_time=?, appointment_type=? "
                   + "WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setInt(3, patient.getAge());
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getPhoneNumber());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getBloodGroup());
            stmt.setString(8, patient.getDisease());
            stmt.setString(9, patient.getAddress());
            stmt.setDate(10, Date.valueOf(patient.getAppointmentDate()));

            // appointment_time (may be null)
            if (patient.getAppointmentTime() != null) {
                stmt.setTime(11, Time.valueOf(patient.getAppointmentTime()));
            } else {
                stmt.setNull(11, Types.TIME);
            }

            // appointment_type (may be null)
            if (patient.getAppointmentType() != null) {
                stmt.setString(12, patient.getAppointmentType());
            } else {
                stmt.setNull(12, Types.VARCHAR);
            }

            stmt.setInt(13, patient.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public void deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    public List<Patient> getAppointmentsForDoctorAndWeek(int doctorId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Patient> appointments = new ArrayList<>();
        String sql = "SELECT id, firstname, lastname, appointment_date, appointment_time " +
                     "FROM patients WHERE doctorId= ? AND appointment_date BETWEEN ? AND ? " +
                     "ORDER BY appointment_date, appointment_time";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String firstName = rs.getString("firstname");
                    String lastName = rs.getString("lastname");
                    LocalDate date = rs.getDate("appointment_date").toLocalDate();

                    Time sqlTime = rs.getTime("appointment_time");
                    LocalTime time = (sqlTime != null) ? sqlTime.toLocalTime() : null;

                    Patient p = new Patient(id, firstName, lastName, 0, null, null, null, null,
                                            null, null, doctorId, date, time, null);
                    appointments.add(p);
                }
            }
        }

        return appointments;
    }

    public boolean isTimeSlotAvailable(int doctorId, LocalDate date, LocalTime time, Integer patientIdToExclude) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE doctorId = ? AND appointment_date = ? AND appointment_time = ?";
        if (patientIdToExclude != null) {
            sql += " AND id <> ?";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            if (time != null) {
                stmt.setTime(3, Time.valueOf(time));
            } else {
                stmt.setNull(3, Types.TIME);
            }
            if (patientIdToExclude != null) {
                stmt.setInt(4, patientIdToExclude);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }
}
