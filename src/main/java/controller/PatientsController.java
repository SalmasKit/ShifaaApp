package controller;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.Patient;
import model.PatientDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Comparator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import utils.Session;

public class PatientsController extends BaseController {
    @FXML
    private ScrollPane mainScrollPane;  // The ScrollPane wrapping my content
    @FXML
    private VBox formContainer;

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> patientNameColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> bloodGroupColumn;
    @FXML private TableColumn<Patient, String> diseaseColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> addressColumn;
    @FXML private TableColumn<Patient, LocalDate> appointmentDateColumn;
    @FXML private TableColumn<Patient, String> appointmentTimeColumn;
    @FXML private TableColumn<Patient, String> appointmentTypeColumn;

    @FXML private TableColumn<Patient, Void> actionsColumn;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private RadioButton femaleRadio;
    @FXML private RadioButton maleRadio;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField diseaseField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private TextField emailField;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ChoiceBox<String> bloodGroupChoiceBox;
    

    // Optional: You may add UI fields for appointmentTime and appointmentType if needed
    @FXML private TextField appointmentTimeField; // format: HH:mm, or use a TimePicker control if you have
    @FXML private ChoiceBox<String> appointmentTypeChoiceBox; // e.g. "consultation", "surgery"

    @FXML private Button addPatientButton;

    private final ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    private PatientDAO patientDAO;
    private ToggleGroup genderGroup;
    private Patient patientBeingEdited = null;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        super.initializeBase();

        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(bundle.getString("dashboard.patients"));
        }

        try {
            patientDAO = new PatientDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            showTranslatedAlert("alert.error.title", "error.loadpatients.message");
            return;
        }
        
        setupForm();
        setupTableColumns();
        setupRowStyles();

        // Apply dark mode if enabled when scene ready
        patientsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyDarkModeIfEnabled(newScene);
            }
        });

        Scene currentScene = patientsTable.getScene();
        if (currentScene != null) {
            applyDarkModeIfEnabled(currentScene);
        }
    }
    
private final String darkStylePath = "/css/style-dark.css";
   private void applyDarkModeIfEnabled(Scene scene) {
    if (darkModeEnabled) {
        URL resource = getClass().getResource(darkStylePath);
        if (resource != null) {
            String darkStyleSheet = resource.toExternalForm();
            if (!scene.getStylesheets().contains(darkStyleSheet)) {
                scene.getStylesheets().add(darkStyleSheet);
            }
        } else {
            System.err.println("Erreur : Feuille de style introuvable pour le chemin : " + darkStylePath);
        }
    }
}


    @Override
    public void setLoggedInDoctorId(int id) {
        super.setLoggedInDoctorId(id);
        loadPatients();
    }

    private void setupForm() {
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 120, 25));
        ageSpinner.setEditable(true);

        genderGroup = new ToggleGroup();
        femaleRadio.setToggleGroup(genderGroup);
        maleRadio.setToggleGroup(genderGroup);

        bloodGroupChoiceBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

        appointmentTypeChoiceBox.getItems().addAll("consultation", "surgery");

        addPatientButton.setText(bundle.getString("patients.addPatient"));
    }

    private void setupTableColumns() {
        patientNameColumn.setCellValueFactory(cd -> {
            Patient p = cd.getValue();
            return new ReadOnlyStringWrapper(p.getFirstName() + " " + p.getLastName());
        });

        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("disease"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        appointmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        appointmentDateColumn.setCellFactory(col -> new TableCell<Patient, LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : fmt.format(item));
            }
        });
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        appointmentTimeColumn.setCellValueFactory(cd -> {
            LocalTime time = cd.getValue().getAppointmentTime();
            return new ReadOnlyStringWrapper(time != null ? time.format(timeFormatter) : "");
        });

        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));


        addActionButtonsToTable();

        patientsTable.setItems(patientsList);
    }

    private void setupRowStyles() {
    patientsTable.setRowFactory(tv -> new TableRow<Patient>() {
        @Override
        protected void updateItem(Patient patient, boolean empty) {
            super.updateItem(patient, empty);
            if (patient == null || empty) {
                setStyle("");
            } else {
                LocalDate today = LocalDate.now();
                LocalDate apptDate = patient.getAppointmentDate();
                if (apptDate != null) {
                    if (!apptDate.isBefore(today)) {
                        // Today or in the future
                        setStyle("-fx-background-color: #CCFFCC;");
                    } else {
                        // Past
                        setStyle("-fx-background-color: #FFCCCC;");
                    }
                } else {
                    setStyle("");
                }
            }
        }
    });
}

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(col -> new TableCell<Patient, Void>() {
            private final HBox box = new HBox(10);
            private final Button btnView = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();

            {
                ImageView ivView   = new ImageView(getClass().getResource("/images/view.png").toExternalForm());
                ImageView ivEdit   = new ImageView(getClass().getResource("/images/edit.png").toExternalForm());
                ImageView ivDelete = new ImageView(getClass().getResource("/images/delete.png").toExternalForm());
                for (ImageView iv : new ImageView[]{ivView, ivEdit, ivDelete}) {
                    iv.setFitWidth(20);
                    iv.setFitHeight(20);
                }
                btnView.setGraphic(ivView);
                btnEdit.setGraphic(ivEdit);
                btnDelete.setGraphic(ivDelete);
                btnView.setStyle("-fx-background-color: transparent;");
                btnEdit.setStyle("-fx-background-color: transparent;");
                btnDelete.setStyle("-fx-background-color: transparent;");

                btnView.setOnAction(e -> handleViewPatient(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEditPatient(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDeletePatient(getTableView().getItems().get(getIndex())));

                box.getChildren().addAll(btnView, btnEdit, btnDelete);
                box.setStyle("-fx-alignment: center;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

 

private void loadPatients() {
    patientsList.clear();
    try {
        List<Patient> loadedPatients = patientDAO.getPatientsByDoctorId(loggedInDoctorId);
     // Sort by appointmentDate descending (null dates last)
    loadedPatients.sort(Comparator.comparing(
        Patient::getAppointmentDate, Comparator.nullsLast(Comparator.reverseOrder())
    ));

        patientsList.addAll(loadedPatients);
    } catch (SQLException e) {
        e.printStackTrace();
        showTranslatedAlert("alert.error.title", "error.loadpatients.message");
    }
}

@FXML
private void handleAddPatient() {
    String firstName          = safeTrim(firstNameField);
    String lastName           = safeTrim(lastNameField);
    String gender             = getSelectedGender();
    int age                   = ageSpinner.getValue();
    LocalDate appointmentDate = appointmentDatePicker.getValue();
    String appointmentType    = appointmentTypeChoiceBox.getValue();
    String disease            = safeTrim(diseaseField);

    if (!validateInputs(firstName, lastName, gender, age, appointmentDate, appointmentType, disease)) {
        return;
    }

    String phone              = safeTrim(phoneField);
    String address            = safeTrim(addressField);
    String email              = safeTrim(emailField);
    String bloodGroup         = bloodGroupChoiceBox.getValue();
    String appointmentTimeStr = safeTrim(appointmentTimeField);

    LocalTime appointmentTime = null;
    if (!appointmentTimeStr.isEmpty()) {
        try {
            appointmentTime = LocalTime.parse(
                appointmentTimeStr,
                DateTimeFormatter.ofPattern("HH:mm")
            );
        } catch (Exception e) {
            showErrorAlert("alert.error.title", "error.invalidtime.message");
            return;
        }
    }

    try {
        if (patientBeingEdited == null) {
            // ✅ Confirm before adding
            if (!showConfirmation("patients.confirm.add", "patients.confirmation.add", firstName + " " + lastName)) {
                return;
            }

            // ➕ Add new patient
            Patient newPatient = new Patient(
                0,
                firstName, lastName, age, gender, phone,
                email, bloodGroup, disease, address,
                loggedInDoctorId, appointmentDate,
                appointmentTime, appointmentType
            );
            patientDAO.addPatient(newPatient);
            patientsList.add(newPatient);

            showInfoAlert("alert.success.title", "patients.success.add");

        } else {
            // ✅ Confirm before editing
            if (!showConfirmation("patients.confirm.edit", "patients.confirmation.edit", firstName + " " + lastName)) {
                return;
            }

            int index = patientsList.indexOf(patientBeingEdited);

            // ✏️ Update existing patient’s fields
            patientBeingEdited.setFirstName(firstName);
            patientBeingEdited.setLastName(lastName);
            patientBeingEdited.setGender(gender);
            patientBeingEdited.setAge(age);
            patientBeingEdited.setAppointmentDate(appointmentDate);
            patientBeingEdited.setAppointmentType(appointmentType);
            patientBeingEdited.setDisease(disease);
            patientBeingEdited.setPhoneNumber(phone);
            patientBeingEdited.setAddress(address);
            patientBeingEdited.setEmail(email);
            patientBeingEdited.setBloodGroup(bloodGroup);
            patientBeingEdited.setAppointmentTime(appointmentTime);

            // Persist to DB
            patientDAO.updatePatient(patientBeingEdited);

            // 🔄 Trigger table update
            if (index >= 0) {
                patientsList.set(index, patientBeingEdited);
            }

            showInfoAlert("alert.success.title", "patients.success.edit");

            patientBeingEdited = null;
        }

        // Re-sort, clear form, reset button
        patientsList.sort(Comparator.comparing(
            Patient::getAppointmentDate,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));
        clearForm();
        addPatientButton.setText(bundle.getString("patients.addPatient"));

    } catch (SQLException ex) {
        ex.printStackTrace();
        showErrorAlert("alert.error.title", "patients.error.addFailed");
    }
}

/** Helper to show a confirmation dialog and return true if OK was pressed */
private boolean showConfirmation(String titleKey, String headerKey, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(bundle.getString(titleKey));
    alert.setHeaderText(bundle.getString(headerKey));
    alert.setContentText(content);
    return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
}

/** Helper to show an INFORMATION alert */
private void showInfoAlert(String titleKey, String messageKey) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(bundle.getString(titleKey));
    alert.setHeaderText(null);
    alert.setContentText(bundle.getString(messageKey));
    alert.showAndWait();
}

/** Helper to show an ERROR alert */
private void showErrorAlert(String titleKey, String messageKey) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(bundle.getString(titleKey));
    alert.setHeaderText(null);
    alert.setContentText(bundle.getString(messageKey));
    alert.showAndWait();
}


// Null‑safe helper
private String safeTrim(TextField field) {
    String t = field.getText();
    return (t == null ? "" : t.trim());
}

// Validate only required fields
private boolean validateInputs(
    String firstName,
    String lastName,
    String gender,
    int age,
    LocalDate appointmentDate,
    String appointmentType,
    String disease
) {
    if (firstName.isEmpty()
     || lastName.isEmpty()
     || gender.isEmpty()
     || age <= 0
     || appointmentDate == null
     || appointmentType == null
     || appointmentType.isEmpty()
     || disease.isEmpty()
    ) {
        showTranslatedAlert("alert.error.title", "patients.error.missingFields");
        return false;
    }
    return true;
}


private String getSelectedGender() {
    Toggle sel = genderGroup.getSelectedToggle();
    return (sel instanceof RadioButton) ? ((RadioButton) sel).getText() : "";
}


private void clearForm() {
    firstNameField.clear();
    lastNameField.clear();
    genderGroup.selectToggle(null);
    phoneField.clear();
    addressField.clear();
    diseaseField.clear();
    emailField.clear();
    ageSpinner.getValueFactory().setValue(25);
    appointmentDatePicker.setValue(null);
    bloodGroupChoiceBox.setValue(null);
    appointmentTimeField.clear();
    appointmentTypeChoiceBox.setValue(null);
    addPatientButton.setText(bundle.getString("patients.addPatient"));
    patientBeingEdited = null;
}
private void handleEditPatient(Patient patient) {
    patientBeingEdited = patient;
    firstNameField.setText(patient.getFirstName());
    lastNameField.setText(patient.getLastName());
    RadioButton toSelect = patient.getGender().equalsIgnoreCase(maleRadio.getText())
        ? maleRadio : femaleRadio;
    genderGroup.selectToggle(toSelect);
    phoneField.setText(patient.getPhoneNumber());
    addressField.setText(patient.getAddress());
    diseaseField.setText(patient.getDisease());
    emailField.setText(patient.getEmail());
    ageSpinner.getValueFactory().setValue(patient.getAge());
    appointmentDatePicker.setValue(patient.getAppointmentDate());
    bloodGroupChoiceBox.setValue(patient.getBloodGroup());
    appointmentTimeField.setText(
        patient.getAppointmentTime() != null
        ? patient.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        : ""
    );
    appointmentTypeChoiceBox.setValue(patient.getAppointmentType());
    addPatientButton.setText(bundle.getString("patients.updatePatient"));

    Platform.runLater(() -> {
        Bounds b = formContainer.localToScene(formContainer.getBoundsInLocal());
        mainScrollPane.setVvalue(b.getMinY() / mainScrollPane.getContent().getBoundsInLocal().getHeight());
    });

    // After populating form for edit, re-sort immediately if user changed date in the form
    // (optional: you could sort here, but usually after saving is enough)
    patientsList.sort(Comparator.comparing(
        Patient::getAppointmentDate,
        Comparator.nullsLast(Comparator.reverseOrder())
    ));
}

private void handleDeletePatient(Patient patient) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(bundle.getString("patients.confirm.delete"));
    alert.setHeaderText(bundle.getString("patients.confirmation.delete"));
    alert.setContentText(patient.getFirstName() + " " + patient.getLastName());

    Optional<ButtonType> res = alert.showAndWait();
    if (res.orElse(ButtonType.CANCEL) == ButtonType.OK) {
        try {
            patientDAO.deletePatient(patient.getId());
            patientsList.remove(patient);

            // ✅ Show success message
            showTranslatedAlert("alert.success.title", "patients.success.delete");

        } catch (SQLException e) {
            e.printStackTrace();
            showTranslatedAlert("alert.error.title", "patients.error.deleteFailed");
        }
    }
}




@FXML
private void handleViewPatient(Patient p) {
    try {
        // 1) Grab the already-updated bundle from BaseController
        ResourceBundle bundle = BaseController.bundle;

        // 2) Load Patients2.fxml with that exact bundle
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/Patients2.fxml"),
            bundle
        );
        Parent root = loader.load();

        // 3) Initialize the new Patients2Controller just as before
        Patients2Controller ctrl2 = loader.getController();
        ctrl2.setLoggedInDoctorId(getLoggedInDoctorId());
        ctrl2.initData(p);
        ctrl2.setPreviousScene(mainScrollPane.getScene());

        // 4) Swap the root on the existing Scene, and store the FXML path
        Stage stage = (Stage) mainScrollPane.getScene().getWindow();
        Scene scene = stage.getScene();
        boolean wasMaximized = stage.isMaximized();

        Session.setDoctorId(getLoggedInDoctorId());
        Session.setSelectedPatient(p);

        scene.setRoot(root);
        // <-- This is crucial: your reloadCurrentView() uses scene.getUserData()
        scene.setUserData("/view/Patients2.fxml");
        applyCurrentTheme(scene);

        stage.setMaximized(wasMaximized);
    } catch (IOException e) {
        e.printStackTrace();
        showTranslatedAlert("alert.error.title", "patients.error.loadview");
    }
}


@FXML
private void onSearchClicked() {
    String searchText = searchField.getText().trim().toLowerCase();

    if (searchText.isEmpty()) {
        patientsTable.setItems(patientsList);
        return;
    }

    Optional<Patient> patientFound = patientsList.stream()
        .filter(p -> {
            String lastName = p.getLastName() == null ? "" : p.getLastName().toLowerCase();
            String firstName = p.getFirstName() == null ? "" : p.getFirstName().toLowerCase();

            String fullName1 = lastName + " " + firstName;  
            String fullName2 = firstName + " " + lastName;  

            return fullName1.contains(searchText) || fullName2.contains(searchText)
                || lastName.contains(searchText) || firstName.contains(searchText);
        })
        .findFirst();

    if (patientFound.isPresent()) {
        Patient found = patientFound.get();

        List<Patient> reorderedList = new ArrayList<>();
        reorderedList.add(found);
        patientsList.stream()
            .filter(p -> !p.equals(found))
            .forEach(reorderedList::add);

        patientsTable.setItems(FXCollections.observableArrayList(reorderedList));
        patientsTable.getSelectionModel().select(found);
        patientsTable.scrollTo(found);
    } else {
        System.out.println("Aucun patient trouvé pour : " + searchText);
    }
}

}