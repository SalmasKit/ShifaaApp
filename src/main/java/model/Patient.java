package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Patient {
    private int id;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String phoneNumber;
    private String email;
    private String bloodGroup;
    private String disease;
    private String address;
    private int doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String appointmentType; // "consultation" or "surgery"
    
    
    
    
   public Patient() {
     // constructeur vide
     }

    // Full constructor (with id)
    public Patient(int id, String firstName, String lastName, int age,
                   String gender, String phoneNumber, String email,
                   String bloodGroup, String disease, String address,
                   int doctorId, LocalDate appointmentDate,
                   LocalTime appointmentTime, String appointmentType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.disease = disease;
        this.address = address;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.appointmentType = appointmentType;
    }

    // Constructor for new patient (without id)
    public Patient(String firstName, String lastName, int age,
                   String gender, String phoneNumber, String email,
                   String bloodGroup, String disease, String address,
                   int doctorId, LocalDate appointmentDate,
                   LocalTime appointmentTime, String appointmentType) {
        this(0, firstName, lastName, age, gender, phoneNumber, email,
             bloodGroup, disease, address, doctorId, appointmentDate,
             appointmentTime, appointmentType);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

   @Override
public String toString() {
    return "ID: " + id +
           ", First Name: " + firstName +
           ", Last Name: " + lastName +
           ", Age: " + age +
           ", Gender: " + gender +
           ", Phone: " + phoneNumber +
           ", Email: " + email +
           ", Blood Group: " + bloodGroup +
           ", Disease: " + disease +
           ", Address: " + address +
           ", Doctor ID: " + doctorId +
           ", Appointment Date: " + appointmentDate +
           ", Appointment Time: " + appointmentTime +
           ", Appointment Type: " + appointmentType;
}

  
    }
