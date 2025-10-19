package model;

public class Treatment {
    private int id;
    private String treatmentName;
    private String prescription;
    private int duration;
    private String image;

    // Constructors
    public Treatment(int id, String treatmentName, String prescription, int duration, String image) {
        this.id = id;
        this.treatmentName = treatmentName;
        this.prescription = prescription;
        this.duration = duration;
        this.image = image;
    }

    public Treatment(String treatmentName, String prescription, int duration, String image) {
        this.treatmentName = treatmentName;
        this.prescription = prescription;
        this.duration = duration;
        this.image = image;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

   @Override
   public String toString() {
    return "ID: " + id +
           ", Treatment Name: " + treatmentName +
           ", Prescription: " + prescription +
           ", Duration: " + duration + " days";
}


}