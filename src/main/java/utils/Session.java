package utils;

import model.Patient;

public class Session {
    private static int doctorId;
    private static Patient selectedPatient;

    public static void setDoctorId(int id) {
        doctorId = id;
    }

    public static int getDoctorId() {
        return doctorId;
    }

    public static void setSelectedPatient(Patient patient) {
        selectedPatient = patient;
    }

    public static Patient getSelectedPatient() {
        return selectedPatient;
    }

    public static void clear() {
        doctorId = 0;
        selectedPatient = null;
    }
}

