package controller;

import model.Patients2DAO;
import model.Patient;
import model.Patients2.PatientTreatment;
import utils.Database;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color; // Keep this for iText colors

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;

import utils.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.Scene;
import model.Doctor;
import model.DoctorDAO;
import model.Treatment;

public class Patients2Controller extends BaseController implements Initializable {

    @FXML private Label namelabel;
    @FXML private Label genderlabel;
    @FXML private Label agelabel;
    @FXML private Label diseaselabel;
    @FXML private Label bloodgrouplabel;
    @FXML private ComboBox<String> medicineComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button addButton;
    @FXML private ListView<PatientTreatment> treatmentListView;
    @FXML private Button downloadButton;
    @FXML private TextArea doctorNotes;
    @FXML private ProgressBar treatmentProgressBar;
    @FXML private Label progressLabel;
    // pour navigation

    private int currentPatientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         BaseController.bundle = resources;
        super.initialize();
        super.initializeBase();
        setDoctorImageToGif();

        treatmentProgressBar.setStyle("-fx-accent: #90EE90;");

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT treatmentName FROM treatments ORDER BY treatmentName")) {
            ObservableList<String> names = FXCollections.observableArrayList();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("treatmentName"));
                }
            }
            medicineComboBox.setItems(names);
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(
                BaseController.bundle.getString("alert.error.title"),
                BaseController.bundle.getString("patients2.error.loadTreatments")
            );
        }

        addButton.setOnAction(this::handleAddTreatment);

        treatmentListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox content = new HBox(10);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                content.getChildren().addAll(label, spacer, editButton, deleteButton);
                content.setAlignment(Pos.CENTER_LEFT);

                ImageView editIcon = new ImageView("/images/edit.png");
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: transparent;");

                ImageView deleteIcon = new ImageView("/images/delete.png");
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent;");

                deleteButton.setOnAction(e -> {
                    PatientTreatment t = getItem();
                    if (t == null) return;
                    if (showConfirmation("confirmation.deleteTreatment.title", "confirmation.deleteTreatment.message")
                        && deleteTreatmentFromDatabase(t)) {
                        getListView().getItems().remove(t);
                    }
                });

                editButton.setOnAction(e -> {
                    PatientTreatment t = getItem();
                    if (t == null) return;
                    medicineComboBox.setValue(t.getTreatmentName());
                    startDatePicker.setValue(LocalDate.parse(t.getStartDate()));
                    endDatePicker.setValue(LocalDate.parse(t.getEndDate()));
                    addButton.setText(BaseController.bundle.getString("treatment.update.button"));
                    addButton.setOnAction(ev -> handleUpdateTreatment(t));
                });
            }

            @Override
            protected void updateItem(PatientTreatment t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                } else {
                    label.setText(t.getTreatmentName() + " | " + t.getStartDate() + " → " + t.getEndDate());
                    setGraphic(content);
                }
            }
        });

        treatmentListView.getItems().addListener((ListChangeListener<PatientTreatment>) change ->
            updateTreatmentProgress(treatmentListView.getItems())
        );

        downloadButton.setOnAction(this::handleDownloadButtonAction);

        if (Session.getSelectedPatient() != null) {
            setLoggedInDoctorId(Session.getDoctorId());
            initData(Session.getSelectedPatient());
        }
    }

    @Override
    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String keyTitle, String keyMessage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(BaseController.bundle.getString(keyTitle));
        alert.setHeaderText(null);
        alert.setContentText(BaseController.bundle.getString(keyMessage));
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void initData(Patient p) {
        currentPatientId = p.getId();
        namelabel.setText(p.getFirstName() + " " + p.getLastName());
        genderlabel.setText(p.getGender());
        agelabel.setText(String.valueOf(p.getAge()));
        diseaselabel.setText(p.getDisease());
        bloodgrouplabel.setText((p.getBloodGroup() == null || p.getBloodGroup().isBlank()) ? "-" : p.getBloodGroup());

        try (Connection conn = Database.getConnection()) {
            Patients2DAO dao = new Patients2DAO(conn);
            ObservableList<PatientTreatment> treatments = dao.getTreatmentsByPatientId(currentPatientId);
            treatmentListView.setItems(treatments);
            updateTreatmentProgress(treatments);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(
                BaseController.bundle.getString("alert.error.title"),
                BaseController.bundle.getString("patients.error.loadview")
            );
        }
    }

    private void handleAddTreatment(ActionEvent evt) {
        String name = medicineComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (name == null || start == null || end == null) {
            showAlert(
                BaseController.bundle.getString("warning.title"),
                BaseController.bundle.getString("warning.fillAllFields")
            );
            return;
        }
        String sql = "INSERT INTO patients_treatments(patient_id, treatment_id, start_date, end_date) " +
                     "VALUES (?, (SELECT id FROM treatments WHERE treatmentName = ?), ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentPatientId);
            ps.setString(2, name);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            if (ps.executeUpdate() == 1) {
                treatmentListView.getItems().add(
                    new PatientTreatment(currentPatientId, name, start.toString(), end.toString())
                );
                updateTreatmentProgress(treatmentListView.getItems());
                medicineComboBox.getSelectionModel().clearSelection();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                addButton.setText(BaseController.bundle.getString("treatment.add.button"));
                addButton.setOnAction(this::handleAddTreatment);
            } else {
                showAlert(
                    BaseController.bundle.getString("alert.error.title"),
                    BaseController.bundle.getString("error.addTreatment")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(
                BaseController.bundle.getString("alert.error.title"),
                BaseController.bundle.getString("error.insertion")
            );
        }
    }

    private boolean deleteTreatmentFromDatabase(PatientTreatment t) {
        String sql = "DELETE FROM patients_treatments WHERE patient_id = ? AND treatment_id = " +
                     "(SELECT id FROM treatments WHERE treatmentName = ?) AND start_date = ? AND end_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentPatientId);
            ps.setString(2, t.getTreatmentName());
            ps.setDate(3, Date.valueOf(t.getStartDate()));
            ps.setDate(4, Date.valueOf(t.getEndDate()));
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(
                BaseController.bundle.getString("alert.error.title"),
                BaseController.bundle.getString("error.deletion")
            );
            return false;
        }
    }

    private void handleUpdateTreatment(PatientTreatment original) {
        String name = medicineComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (name == null || start == null || end == null) {
            showAlert(
                BaseController.bundle.getString("warning.title"),
                BaseController.bundle.getString("warning.fillAllFields")
            );
            return;
        }
        String sql = "UPDATE patients_treatments SET treatment_id = (SELECT id FROM treatments WHERE treatmentName = ?), start_date = ?, end_date = ? " +
                     "WHERE patient_id = ? AND treatment_id = (SELECT id FROM treatments WHERE treatmentName = ?) AND start_date = ? AND end_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ps.setInt(4, currentPatientId);
            ps.setString(5, original.getTreatmentName());
            ps.setDate(6, Date.valueOf(original.getStartDate()));
            ps.setDate(7, Date.valueOf(original.getEndDate()));
            if (ps.executeUpdate() == 1) {
                // Replace in the list
                ObservableList<PatientTreatment> list = treatmentListView.getItems();
                int index = list.indexOf(original);
                if (index >= 0) {
                    list.set(index, new PatientTreatment(currentPatientId, name, start.toString(), end.toString()));
                }
                medicineComboBox.getSelectionModel().clearSelection();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                addButton.setText(BaseController.bundle.getString("treatment.add.button"));
                addButton.setOnAction(this::handleAddTreatment);
                updateTreatmentProgress(list);
            } else {
                showAlert(
                    BaseController.bundle.getString("alert.error.title"),
                    BaseController.bundle.getString("error.updateTreatment")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(
                BaseController.bundle.getString("alert.error.title"),
                BaseController.bundle.getString("error.updateTreatment")
            );
        }
    }

private void updateTreatmentProgress(List<PatientTreatment> treatments) {
    if (treatments == null || treatments.isEmpty()) {
        treatmentProgressBar.setProgress(0);
        progressLabel.setText("");
        return;
    }

    // Trouver la date de début la plus ancienne
    LocalDate earliestStart = treatments.stream()
        .map(t -> LocalDate.parse(t.getStartDate()))
        .min(LocalDate::compareTo)
        .orElse(LocalDate.now());

    // Trouver la date de fin la plus récente
    LocalDate latestEnd = treatments.stream()
        .map(t -> LocalDate.parse(t.getEndDate()))
        .max(LocalDate::compareTo)
        .orElse(LocalDate.now());

    LocalDate today = LocalDate.now();

    // Durée totale de la période englobante (incluse)
    long totalDays = ChronoUnit.DAYS.between(earliestStart, latestEnd) + 1;

    // Jours écoulés depuis la date la plus ancienne (inclus)
    long elapsedDays = ChronoUnit.DAYS.between(earliestStart, today) + 1;

    if (elapsedDays < 0) elapsedDays = 0;
    if (elapsedDays > totalDays) elapsedDays = totalDays;

    // Progression
    double progress = totalDays > 0 ? (double) elapsedDays / totalDays : 0;

    // Jours restants
    long remainingDays = totalDays - elapsedDays;
    if (remainingDays < 0) remainingDays = 0;

    treatmentProgressBar.setProgress(progress);

    String pluralSuffix = remainingDays > 1 ? "s" : "";

    ResourceBundle bundle = BaseController.bundle;

    String progressText = java.text.MessageFormat.format(
        bundle.getString("progress.label"),
        (int)(progress * 100),   // {0}
        remainingDays,           // {1}
        pluralSuffix             // {2}
    );

    progressLabel.setText(progressText);
}



 @FXML
public void handleDownloadButtonAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(BaseController.bundle.getString("pdf.save.title"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
    File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
    if (file == null) return;

    try {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Fonts
        Color shifaaColor = new Color(0x7B, 0xAB, 0xE2);
        Font shifaaFont = new Font(Font.HELVETICA, 32, Font.BOLD, shifaaColor);
        Font subtitleFont = new Font(Font.HELVETICA, 18, Font.ITALIC, Color.DARK_GRAY);
        Font infoFont = new Font(Font.HELVETICA, 14, Font.NORMAL, Color.BLACK);
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(0x09, 0x4A, 0x8A));
        Font headerFont = new Font(Font.HELVETICA, 16, Font.BOLD, shifaaColor);
        Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);

        // First page: Shifaa title
        Paragraph mainTitle = new Paragraph("Shifaa", shifaaFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(mainTitle);

        // Subtitle
        String subtitleText = BaseController.bundle.getString("patients2.pdf.subtitle"); // e.g., "Report" or "Rapport"
        Paragraph subtitle = new Paragraph(subtitleText, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitle);

        doc.add(new Paragraph(" ")); // Empty line

        // Doctor name and date
        String doctorName = "Unknown Doctor";
        try {
            DoctorDAO doctorDAO = new DoctorDAO();
            Doctor loggedInDoctor = doctorDAO.getDoctorById(getLoggedInDoctorId()); // Assumes method exists

            if (loggedInDoctor != null) {
                String doctorPrefix = BaseController.bundle.getString("doctor.prefix");
                doctorName = doctorPrefix + " " + loggedInDoctor.getFirstName() + " " + loggedInDoctor.getLastName();
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            showAlert(BaseController.bundle.getString("alert.error.title"), "Erreur lors de la récupération du nom du médecin.");
}

        String currentDate = java.time.LocalDate.now().toString();

        Paragraph doctorInfo = new Paragraph(doctorName, infoFont);
        doctorInfo.setAlignment(Element.ALIGN_CENTER);
        doc.add(doctorInfo);

        Paragraph dateInfo = new Paragraph(currentDate, infoFont);
        dateInfo.setAlignment(Element.ALIGN_CENTER);
        doc.add(dateInfo);

        // Add a page break
        doc.newPage();

        // Second page - existing content
        Paragraph title = new Paragraph(BaseController.bundle.getString("patients2.pdf.title"), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(new Paragraph(" ")); // Empty line

        // Patient info
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.patientName") + ": " + namelabel.getText(), normalFont));
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.gender") + ": " + genderlabel.getText(), normalFont));
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.age") + ": " + agelabel.getText(), normalFont));
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.disease") + ": " + diseaselabel.getText(), normalFont));
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.bloodGroup") + ": " + bloodgrouplabel.getText(), normalFont));

        doc.add(new Paragraph(" ")); // Empty line

        // Treatments
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.treatments"), headerFont));
        for (PatientTreatment t : treatmentListView.getItems()) {
            String treatmentText = String.format("%s: %s → %s",
                    t.getTreatmentName(), t.getStartDate(), t.getEndDate());
            doc.add(new Paragraph(treatmentText, normalFont));
        }

        doc.add(new Paragraph(" ")); // Empty line

        // Doctor notes
        doc.add(new Paragraph(BaseController.bundle.getString("patients2.pdf.doctorNotes"), headerFont));
        doc.add(new Paragraph(doctorNotes.getText(), normalFont));

        doc.close();

        showInfoDialog(
            BaseController.bundle.getString("patients2.pdf.successTitle"),
            BaseController.bundle.getString("patients2.pdf.successMessage")
        );
    } catch (IOException | DocumentException e) {
        e.printStackTrace();
        showAlert(
            BaseController.bundle.getString("alert.error.title"),
            BaseController.bundle.getString("patients2.pdf.errorSaving")
        );
    }
}



    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Scene previousScene;

public void setPreviousScene(Scene previousScene) {
    this.previousScene = previousScene;
}

}
