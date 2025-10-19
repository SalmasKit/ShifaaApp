package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import model.Treatment;
import model.TreatmentDAO;

public class TreatmentsController extends BaseController {

    @FXML private FlowPane treatmentsContainer;
    @FXML private TextField medecineNameField;
    @FXML private TextArea prescriptionTextArea;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Button uploadImageButton;
    @FXML private Button addMedecineButton;
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox formContainer;
    @FXML private ImageView  searchButton;

    private String imagePath = null;
    private TreatmentDAO treatmentDAO;
    private Treatment treatmentBeingEdited = null;
    private final ResourceBundle bundle = BaseController.bundle;

    @Override
    public void initialize() {
        super.initialize();
        super.initializeBase();

        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(bundle.getString("dashboard.treatments"));
        }

        
        if (searchButton != null) {
        searchButton.setOnMouseClicked(e -> {
            String searchTerm = searchField.getText().trim();
            searchTreatments(searchTerm);
        });
    }
    if (searchField != null) {
        searchField.setOnAction(e -> {
            String searchTerm = searchField.getText().trim();
            searchTreatments(searchTerm);
        });
    }
        try {
            treatmentDAO = new TreatmentDAO();
            loadTreatments();
        } catch (SQLException e) {
            e.printStackTrace();
            showTranslatedAlertOrRaw(Alert.AlertType.ERROR,
                bundle.getString("error.navigation.title"),
                bundle.getString("error.navigation.message"));
        }

        treatmentsContainer.setHgap(10);
        treatmentsContainer.setVgap(10);

        mainScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            treatmentsContainer.setPrefWrapLength(newBounds.getWidth());
            treatmentsContainer.setPrefWidth(newBounds.getWidth());
            treatmentsContainer.setMaxWidth(newBounds.getWidth());
        });

        durationSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 0));

        uploadImageButton.setOnAction(e -> uploadImage());
        addMedecineButton.setOnAction(e -> addNewTreatment());
    }

    private void loadTreatments() {
        List<Treatment> treatments = treatmentDAO.getAllTreatments();
        treatmentsContainer.getChildren().clear();
        for (Treatment treatment : treatments) {
            treatmentsContainer.getChildren().add(createTreatmentBox(treatment));
        }
    }

    private VBox createTreatmentBox(Treatment treatment) {
        VBox box = new VBox(15);
        box.getStyleClass().add("treatment-box");
        box.setPrefSize(220, 300);
        box.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        String path = treatment.getImage();
        boolean loaded = false;
        if (path != null && !path.isEmpty()) {
            try {
                var stream = getClass().getResourceAsStream("/" + path);
                if (stream != null) {
                    imageView.setImage(new Image(stream, 180, 120, true, true));
                    loaded = true;
                }
            } catch (Exception ignored) {}
            if (!loaded) {
                try {
                    File file = new File("src/main/resources/" + path);
                    if (file.exists()) {
                        imageView.setImage(new Image(
                            file.toURI().toString(), 180, 120, true, true));
                        loaded = true;
                    }
                } catch (Exception ignored) {}
            }
        }
        if (!loaded) {
            var placeholder = getClass().getResourceAsStream("/images/no_image.png");
            if (placeholder != null) {
                imageView.setImage(new Image(placeholder, 180, 120, true, true));
            }
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);

        Label nameLabel = new Label(treatment.getTreatmentName());
        nameLabel.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("treatment-buttons");

        Button viewButton = createIconButton("view");
        Button editButton = createIconButton("edit");
        Button deleteButton = createIconButton("delete");

        viewButton.setOnAction(e -> showTreatmentInfo(treatment));
        editButton.setOnAction(e -> showEditForm(treatment));
        deleteButton.setOnAction(e -> deleteTreatment(treatment, box));

        buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);
        box.getChildren().addAll(imageView, nameLabel, buttonBox);
        return box;
    }

    private Button createIconButton(String imageName) {
        ImageView icon;
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName + ".png");
            icon = (stream != null)
                ? new ImageView(new Image(stream))
                : new ImageView();
        } catch (Exception e) {
            icon = new ImageView();
        }
        icon.setFitWidth(20);
        icon.setFitHeight(20);

        Button button = new Button();
        button.setGraphic(icon);
        button.setMinSize(30, 30);
        button.setStyle("-fx-background-radius: 5; -fx-padding: 5;");
        return button;
    }

    private void showTreatmentInfo(Treatment treatment) {
        String title = bundle.containsKey("treatment.info.title")
            ? bundle.getString("treatment.info.title") : "Treatment Info";
        String durationText = treatment.getDuration() == 0
            ? bundle.getString("treatment.duration.asNeeded")
            : treatment.getDuration() + " " + bundle.getString("treatment.duration");
        String info = String.format(
            "%s: %s\n%s: %s\n%s: %s",
            bundle.getString("treatment.name.label"), treatment.getTreatmentName(),
            bundle.getString("treatment.prescription.label"), treatment.getPrescription(),
            bundle.getString("treatment.duration.label"), durationText
        );
        showTranslatedAlertOrRaw(Alert.AlertType.INFORMATION, title, info);
    }

    private void uploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(bundle.getString("filechooser.image.title"));
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JPG Files", "*.jpg")
        );
        File file = chooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (file != null) {
            try {
                File destDir = new File("src/main/resources/Medecines");
                if (!destDir.exists()) destDir.mkdirs();
                File destFile = new File(destDir, file.getName());
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = "Medecines/" + file.getName();
                uploadImageButton.setStyle("-fx-background-color: lightgreen;");
                showTranslatedAlertOrRaw(Alert.AlertType.INFORMATION,
                    bundle.getString("upload.success.title"),
                    bundle.getString("upload.success.message")
                );
            } catch (IOException ex) {
                showTranslatedAlertOrRaw(Alert.AlertType.ERROR,
                    bundle.getString("update.error.title"), ex.getMessage());
            }
        }
    }

    private void addNewTreatment() {
        String name = medecineNameField.getText().trim();
        String prescription = prescriptionTextArea.getText().trim();
        Integer duration = durationSpinner.getValue();

        if (name.isEmpty()) {
            showTranslatedAlertOrRaw(Alert.AlertType.WARNING,
                bundle.getString("form.incomplete.title"),
                bundle.getString("form.incomplete.message"));
            return;
        }

        if (treatmentBeingEdited != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle(bundle.getString("confirm.edit.title"));
            confirmAlert.setContentText(bundle.getString("confirm.edit.message"));
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                treatmentBeingEdited.setTreatmentName(name);
                treatmentBeingEdited.setPrescription(prescription);
                treatmentBeingEdited.setDuration(duration);
                treatmentBeingEdited.setImage(imagePath);
                if (treatmentDAO.updateTreatment(treatmentBeingEdited)) {
                    postUpdateCleanup("update.success.title", "update.success.message");
                } else {
                    showTranslatedAlertOrRaw(Alert.AlertType.ERROR,
                        bundle.getString("update.error.title"),
                        bundle.getString("update.error.message"));
                }
            }
        } else {
            createAndInsertNewTreatment(name, prescription, duration);
        }
    }

    private void createAndInsertNewTreatment(String name, String prescription, Integer duration) {
        Treatment newTreatment = new Treatment(0, name, prescription, duration, imagePath);
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(bundle.getString("confirm.add.title"));
        confirmAlert.setContentText(bundle.getString("confirm.add.message"));
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (treatmentDAO.insertTreatment(newTreatment)) {
                postUpdateCleanup("insert.success.title", "insert.success.message");
            } else {
                showTranslatedAlertOrRaw(Alert.AlertType.ERROR,
                    bundle.getString("insert.error.title"),
                    bundle.getString("insert.error.message"));
            }
        }
    }

    private void postUpdateCleanup(String titleKey, String messageKey) {
        loadTreatments();
        clearForm();
        showTranslatedAlertOrRaw(Alert.AlertType.INFORMATION,
            bundle.getString(titleKey), bundle.getString(messageKey));
        treatmentBeingEdited = null;
        addMedecineButton.setText(bundle.getString("med.button"));
    }

    private void deleteTreatment(Treatment treatment, VBox box) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(bundle.getString("confirm.delete.title"));
        confirmAlert.setContentText(bundle.getString("confirm.delete.message"));
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (treatmentDAO.deleteTreatment(treatment.getId())) {
                treatmentsContainer.getChildren().remove(box);
                showTranslatedAlertOrRaw(Alert.AlertType.INFORMATION,
                    bundle.getString("delete.success.title"),
                    bundle.getString("delete.success.message"));
            } else {
                showTranslatedAlertOrRaw(Alert.AlertType.ERROR,
                    bundle.getString("delete.error.title"),
                    bundle.getString("delete.error.message"));
            }
        }
    }

    private void showEditForm(Treatment treatment) {
        medecineNameField.setText(treatment.getTreatmentName());
        prescriptionTextArea.setText(treatment.getPrescription());
        durationSpinner.getValueFactory().setValue(treatment.getDuration());
        imagePath = treatment.getImage();
        uploadImageButton.setStyle(
            imagePath != null && !imagePath.isEmpty() ? "-fx-background-color: lightgreen;" : "");
        addMedecineButton.setText(bundle.getString("med.edit"));
        treatmentBeingEdited = treatment;
        scrollToForm();
    }

    private void scrollToForm() {
        double formY = formContainer.getLayoutY();
        double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
        double vValue = formY / (contentHeight - viewportHeight);
        mainScrollPane.setVvalue(Math.min(Math.max(vValue, 0), 1));
    }

    private void clearForm() {
        medecineNameField.clear();
        prescriptionTextArea.clear();
        durationSpinner.getValueFactory().setValue(7);
        imagePath = null;
        uploadImageButton.setStyle("-fx-background-color: black;");
        addMedecineButton.setText(bundle.getString("med.button"));
        treatmentBeingEdited = null;
    }

    private void showTranslatedAlertOrRaw(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
    @FXML
public void searchTreatments(MouseEvent event) {
    String searchTerm = searchField.getText();
    searchTreatments(searchTerm);
}
private void searchTreatments(String searchTerm) {
    List<Treatment> allTreatments = treatmentDAO.getAllTreatments();
    treatmentsContainer.getChildren().clear();

    if (searchTerm == null || searchTerm.isEmpty()) {
        for (Treatment treatment : allTreatments) {
            treatmentsContainer.getChildren().add(createTreatmentBox(treatment));
        }
        return;
    }

    String lowerSearch = searchTerm.toLowerCase();
    List<Treatment> matched = allTreatments.stream()
        .filter(t -> t.getTreatmentName().toLowerCase().contains(lowerSearch))
        .collect(Collectors.toList());  

    matched.sort((t1, t2) -> {
        String name1 = t1.getTreatmentName().toLowerCase();
        String name2 = t2.getTreatmentName().toLowerCase();

        boolean starts1 = name1.startsWith(lowerSearch);
        boolean starts2 = name2.startsWith(lowerSearch);

        if (starts1 && !starts2) return -1;  
        if (!starts1 && starts2) return 1;   
        return name1.compareTo(name2);       
    });
    List<Treatment> others = allTreatments.stream()
        .filter(t -> !t.getTreatmentName().toLowerCase().contains(lowerSearch))
        .toList();

    for (Treatment treatment : matched) {
        treatmentsContainer.getChildren().add(createTreatmentBox(treatment));
    }
    for (Treatment treatment : others) {
        treatmentsContainer.getChildren().add(createTreatmentBox(treatment));
    }
}
}
