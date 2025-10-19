package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import utils.Database;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.input.MouseEvent;



public class BaseController {

    @FXML
    protected Label breadcrumbLabel;

    @FXML
    protected TextField searchField;

    @FXML
    protected ImageView settingsIcon;

    @FXML
    protected Label doctorNameLabel;

    @FXML
    protected Label nameLabel;
    
    @FXML
    protected ImageView doctorImage;

    protected ContextMenu settingsMenu;
    protected Database db = new Database();
    protected static boolean darkModeEnabled = false; // ← Centralisé ici
    protected final String darkStylePath = "/Css/style-dark.css";
;

    private ImageView darkThemeIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/dark_icon.png")));
    private ImageView lightThemeIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/light_icon.png")));
    private MenuItem themeToggleMenuItem;

    public static Locale currentLocale = Locale.ENGLISH;
    public static ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", currentLocale);

    @FXML
public void initialize() {
    initializeBase();
    setDoctorImageToGif();

    if (notificationIcon != null) {
        setupNotificationMenu();
    }

    if (notificationMenu != null) {
        notificationMenu.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-pref-width: 300px; " +
            "-fx-text-fill: black; " +
            "-fx-background-color: white;" +
            "-fx-border-color: black;"
        );
    }

    // Ajout ici pour appliquer le thème selon le mode
    Scene scene = null;
    if (settingsIcon != null) {
        scene = settingsIcon.getScene();
    }
    if (scene != null) {
        applyCurrentTheme(scene);
    }
}


    protected void initializeBase() {
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(bundle.getString("breadcrumb.dashboard"));
        }
        if (searchField != null) {
            searchField.requestFocus();
        }

        setupSettingsMenu();

        if (loggedInDoctorId > 0) {
            loadDoctorNameFromDatabase(loggedInDoctorId);
        }
    }

    //set the doctor image to gif
    public void setDoctorImageToGif() {
        if (doctorImage != null) {
            Image gif = new Image(getClass().getResource("/images/doctor.gif").toExternalForm());
            doctorImage.setImage(gif);
        }
    }
    
    
  
    public void setLoggedInDoctorId(int id) {
        this.loggedInDoctorId = id;
        loadDoctorNameFromDatabase(loggedInDoctorId);
        onDoctorIdSet();
    }

    protected int loggedInDoctorId = -1;
    public int getLoggedInDoctorId() {
    return loggedInDoctorId;
}

    protected void onDoctorIdSet() {
        // Par défaut rien
    }
   

    protected void loadDoctorNameFromDatabase(int doctorId) {
        String sql = "SELECT firstname, lastname FROM doctors WHERE id = ?";
        try (Connection conn = Database.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                updateDoctorName(firstName, lastName);
                updateGreetingLabel(lastName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateDoctorName(String firstName, String lastName) {
        if (doctorNameLabel != null) {
            doctorNameLabel.setText(firstName + " " + lastName);
        }
    }

    protected void updateGreetingLabel(String lastName) {
        if (nameLabel != null) {
            String baseText = bundle.getString("dashboard.welcome1");
            nameLabel.setText(baseText + ", Dr. " + lastName + "!");
        }
    }
    
    @FXML
protected void handleSettingsClick(MouseEvent event) {
    if (settingsMenu == null || settingsIcon == null) return;

    if (settingsMenu.isShowing()) {
        settingsMenu.hide();
    } else {
        settingsMenu.show(settingsIcon, event.getScreenX(), event.getScreenY());
    }
}

    private void setupSettingsMenu() {
        if (settingsIcon == null) return;

        darkThemeIcon.setFitWidth(24);
        darkThemeIcon.setFitHeight(24);
        lightThemeIcon.setFitWidth(24);
        lightThemeIcon.setFitHeight(24);

        ImageView languageIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/translation.png")));
        languageIcon.setFitWidth(24);
        languageIcon.setFitHeight(24);

        ImageView logoutIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/logout.png")));
        logoutIcon.setFitWidth(24);
        logoutIcon.setFitHeight(24);

        MenuItem changeLanguage = new MenuItem(bundle.getString("dashboard.languagec"), languageIcon);
        MenuItem logout = new MenuItem(bundle.getString("dashboard.languagel"), logoutIcon);

        ImageView themeIcon = darkModeEnabled ? lightThemeIcon : darkThemeIcon;
        String themeText = darkModeEnabled ? bundle.getString("theme.light") : bundle.getString("theme.dark");

        themeToggleMenuItem = new MenuItem(themeText, themeIcon);

        String menuItemStyle = "-fx-padding: 10 30 10 30; -fx-font-size: 14px;";
        changeLanguage.setStyle(menuItemStyle);
        themeToggleMenuItem.setStyle(menuItemStyle);
        logout.setStyle(menuItemStyle);

        changeLanguage.setOnAction(e -> handleChangeLanguage());
        themeToggleMenuItem.setOnAction(e -> handleToggleTheme());
        logout.setOnAction(e -> handleLogout());

        settingsMenu = new ContextMenu(changeLanguage, themeToggleMenuItem, logout);

        settingsIcon.setOnMouseClicked(e -> {
            settingsMenu.show(settingsIcon, e.getScreenX(), e.getScreenY());
        });
    }

    protected void handleToggleTheme() {
        darkModeEnabled = !darkModeEnabled;

        Scene scene = settingsIcon.getScene();
        if (scene == null) return;

        applyCurrentTheme(scene); // ← méthode centralisée

        themeToggleMenuItem.setText(
            darkModeEnabled ? bundle.getString("theme.light") : bundle.getString("theme.dark"));
        themeToggleMenuItem.setGraphic(darkModeEnabled ? lightThemeIcon : darkThemeIcon);
    }

    protected void applyCurrentTheme(Scene scene) {
    String lightStyleSheet = getClass().getResource("/Css/style-light.css").toExternalForm();
    String darkStyleSheet = getClass().getResource("/Css/style-dark.css").toExternalForm();

    scene.getStylesheets().clear();
    if (darkModeEnabled) {
        scene.getStylesheets().add(darkStyleSheet);
    } else {
        scene.getStylesheets().add(lightStyleSheet);
    }
}


    protected void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) settingsIcon.getScene().getWindow();
            Scene scene = new Scene(root);
            applyCurrentTheme(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void handleChangeLanguage() {
        if (currentLocale.equals(Locale.ENGLISH)) {
            currentLocale = Locale.FRANCE;
        } else {
            currentLocale = Locale.ENGLISH;
        }
        bundle = ResourceBundle.getBundle("lang.messages", currentLocale);
        reloadCurrentView();
    }

    protected void reloadCurrentView() {
    try {
        // 1) Grab the Stage, its Scene, and remember maximized state
        Stage stage = (Stage) settingsIcon.getScene().getWindow();
        Scene scene = stage.getScene();
        boolean wasMax = stage.isMaximized();

        // 2) Determine which FXML to load
        String currentFxml = (String) scene.getUserData();
        if (currentFxml == null) {
            currentFxml = "/view/Dashboard.fxml";
        }

        // 3) Load the FXML with the new bundle
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(currentFxml),
            bundle
        );
        Parent root = loader.load();

        // 4) Inject doctorId into the new controller
        BaseController controller = loader.getController();
        if (this.loggedInDoctorId > 0) {
            controller.setLoggedInDoctorId(this.loggedInDoctorId);
        }

        // 5) Swap the root of the existing scene
        scene.setRoot(root);
        scene.setUserData(currentFxml);

        // 6) Reapply your theme
        applyCurrentTheme(scene);

        // 7) Restore maximized state on next pulse
        Platform.runLater(() -> stage.setMaximized(wasMax));

    } catch (IOException e) {
        e.printStackTrace();
        showTranslatedAlert("error.reload.title", "error.reload.message");
    }
}


    @FXML
    protected void switchToDashboardView(ActionEvent event) {
        loadView("/view/Dashboard.fxml", event);
    }

    @FXML
    protected void switchToPatientsView(ActionEvent event) {
        loadView("/view/Patients.fxml", event);
    }

    @FXML
    protected void switchToTreatmentsView(ActionEvent event) {
        loadView("/view/Treatments.fxml", event);
    }

    @FXML
    protected void switchToAppointmentsView(ActionEvent event) {
        loadView("/view/Appointments.fxml", event);
    }

    @FXML
    protected void switchToLoginView(ActionEvent event) {
        loadView("/view/Login.fxml", event);
    }

   protected void loadView(String fxmlPath, ActionEvent event) {
    try {
        // 1. find stage & existing scene
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        // 2. load new FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
        Parent root = loader.load();
        BaseController controller = loader.getController();
        if (this.loggedInDoctorId > 0) {
            controller.setLoggedInDoctorId(this.loggedInDoctorId);
        }

        // 3. swap root, update userData & re-apply theme
        scene.setRoot(root);
        scene.setUserData(fxmlPath);
        applyCurrentTheme(scene);

        // 4. no need to call setScene or restore maximized!
    } catch (IOException e) {
        e.printStackTrace();
        showTranslatedAlert("error.navigation.title", "error.navigation.message");
    }
}



    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showTranslatedAlert(String titleKey, String messageKey) {
        String title = bundle.containsKey(titleKey) ? bundle.getString(titleKey) : titleKey;
        String message = bundle.containsKey(messageKey) ? bundle.getString(messageKey) : messageKey;
        showAlert(title, message);
    }
    
    @FXML
private Node notificationIcon;

private ContextMenu notificationMenu;



 private void setupNotificationMenu() {
        notificationMenu = new ContextMenu();

        notificationIcon.setOnMouseClicked(event -> {
            if (notificationMenu.isShowing()) {
                notificationMenu.hide();
            } else {
                populateNotificationMenu();
                notificationMenu.show(notificationIcon, Side.BOTTOM, 0, 5);
            }
        });
    }

 private void populateNotificationMenu() {
    notificationMenu.getItems().clear();

    try (Connection conn = Database.getConnection()) {
        String sql = """
            SELECT appointment_time, firstname, lastname, appointment_type
            FROM patients
            WHERE DATE(appointment_date) = CURDATE() AND doctorId = ?
            ORDER BY appointment_time
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getLoggedInDoctorId());
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String time = rs.getString("appointment_time");
                String firstname = rs.getString("firstname");
                String lastname = rs.getString("lastname");
                String name = firstname + " " + lastname;  // concaténation
                String type = rs.getString("appointment_type");

                String label = String.format("%s - %s (%s)", time, name, type);
                MenuItem item = new MenuItem(label);
                item.setDisable(true); // display-only
                notificationMenu.getItems().add(item);
            }

            if (!hasData) {
                MenuItem emptyItem = new MenuItem("Aucun rendez-vous aujourd'hui.");
                emptyItem.setDisable(true);
                notificationMenu.getItems().add(emptyItem);
            }

        }
    } catch (SQLException e) {
        e.printStackTrace();
        MenuItem errorItem = new MenuItem("Erreur lors du chargement.");
        errorItem.setDisable(true);
        notificationMenu.getItems().add(errorItem);
    }
}

}

  




