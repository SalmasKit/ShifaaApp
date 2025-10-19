package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.LogInDAO;

public class LogInController extends BaseController {

    @FXML private Label titleLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private Button langButton;
    @FXML private Label welcomeText;

    @FXML private TextField emailField;
    @FXML private TextField passwordField; // Changed from PasswordField to TextField to handle both types

    private LogInDAO utilisateurDAO;
    private ResourceBundle bundle;

    public LogInController() throws SQLException {
        utilisateurDAO = new LogInDAO();
    }

    @FXML
    public void initialize() {
        bundle = BaseController.bundle;
        updateTexts();
    }

    private void updateTexts() {
        titleLabel.setText(bundle.getString("app.name"));
        welcomeText.setText(bundle.getString("login.titre"));
        emailLabel.setText(bundle.getString("login.email"));
        passwordLabel.setText(bundle.getString("login.password"));
        loginButton.setText(bundle.getString("login.button"));
        signUpButton.setText(bundle.getString("login.signup"));
        emailField.setPromptText(bundle.getString("login.e2"));
        if (passwordField != null) {
            passwordField.setPromptText(bundle.getString("login.e1"));
        }
        langButton.setText(
            BaseController.currentLocale.equals(Locale.ENGLISH) ? "Fr" : "En"
        );
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (utilisateurDAO.isValidUser(email, password)) {
            int doctorId = utilisateurDAO.getDoctorIdByCredentials(email, password);
            if (doctorId != -1) {
                setLoggedInDoctorId(doctorId);
                loadView("/view/Dashboard.fxml", event);
            } else {
                showTranslatedAlert("alert.error.title", "login.error.accountinfo");
            }
        } else {
            showTranslatedAlert("alert.error.title", "login.error.credentials");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        loadView("/view/SignUp.fxml", event);
    }

    @FXML
    private void handleLanguageSwitch(ActionEvent event) {
        if (BaseController.currentLocale.equals(Locale.ENGLISH)) {
            BaseController.currentLocale = Locale.FRENCH;
        } else {
            BaseController.currentLocale = Locale.ENGLISH;
        }
        
        BaseController.bundle = ResourceBundle.getBundle(
            "lang.messages", 
            BaseController.currentLocale
        );
        
        // Reload the current view to apply language changes
        loadView("/view/Login.fxml", event);
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (passwordField != null) {
            try {
                // Toggle between TextField and PasswordField
                if (passwordField instanceof PasswordField) {
                    // Show password
                    TextField textField = new TextField(passwordField.getText());
                    textField.setPromptText(passwordField.getPromptText());
                    textField.setLayoutX(passwordField.getLayoutX());
                    textField.setLayoutY(passwordField.getLayoutY());
                    textField.setPrefWidth(passwordField.getWidth());
                    textField.setStyle(passwordField.getStyle());
                    
                    HBox parent = (HBox) passwordField.getParent();
                    int index = parent.getChildren().indexOf(passwordField);
                    parent.getChildren().remove(passwordField);
                    parent.getChildren().add(index, textField);
                    
                    // Update the reference
                    passwordField = textField;
                    
                    // Change icon to hide password
                    Button toggleButton = (Button) event.getSource();
                    ImageView imageView = (ImageView) toggleButton.getGraphic();
                    if (imageView != null) {
                        try {
                            imageView.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
                        } catch (Exception e) {
                            System.err.println("Error loading hide.png: " + e.getMessage());
                        }
                    }
                    
                    // Switch back to password field when focus is lost
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            PasswordField newPasswordField = new PasswordField();
                            newPasswordField.setText(textField.getText());
                            newPasswordField.setPromptText(textField.getPromptText());
                            newPasswordField.setLayoutX(textField.getLayoutX());
                            newPasswordField.setLayoutY(textField.getLayoutY());
                            newPasswordField.setPrefWidth(textField.getWidth());
                            newPasswordField.setStyle(textField.getStyle());
                            
                            HBox p = (HBox) textField.getParent();
                            int i = p.getChildren().indexOf(textField);
                            p.getChildren().remove(textField);
                            p.getChildren().add(i, newPasswordField);
                            
                            passwordField = newPasswordField;
                            
                            // Change icon back to show password
                            if (imageView != null) {
                                try {
                                    imageView.setImage(new Image(getClass().getResourceAsStream("/images/view.png")));
                                } catch (Exception e) {
                                    System.err.println("Error loading view.png: " + e.getMessage());
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void showTranslatedAlert(String titleKey, String messageKey) {
        showAlert(
            bundle.getString(titleKey),
            bundle.getString(messageKey)
        );
    }
}