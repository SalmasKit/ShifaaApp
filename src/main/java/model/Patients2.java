package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Patients2  {

    private int patientId;
    private String name;
    private String gender;
    private int age;
    private String disease;
    private String bloodGroup;

    private ObservableList<PatientTreatment> treatments = FXCollections.observableArrayList();

    public Patients2(int patientId, String name, String gender, int age, String disease, String bloodGroup) {
        this.patientId = patientId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.disease = disease;
        this.bloodGroup = bloodGroup;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public String getDisease() {
        return disease;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public ObservableList<PatientTreatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(ObservableList<PatientTreatment> treatments) {
        this.treatments = treatments;
    }


    // Classe interne PatientTreatment
    public static class PatientTreatment {
        private int patientId;        // patient_id de la table
        private String treatmentName; // nom du traitement (ex: "Radiothérapie")
        private String startDate;
        private String endDate;

        public PatientTreatment(int patientId, String treatmentName, String startDate, String endDate) {
            this.patientId = patientId;
            this.treatmentName = treatmentName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getPatientId() {
            return patientId;
        }

        public String getTreatmentName() {
            return treatmentName;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setPatientId(int patientId) {
            this.patientId = patientId;
        }

        public void setTreatmentName(String treatmentName) {
            this.treatmentName = treatmentName;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}
