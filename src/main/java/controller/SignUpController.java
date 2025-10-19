package controller;

import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import model.Doctor;
import model.LogInDAO;
import javafx.event.ActionEvent;

public class SignUpController extends BaseController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    private LogInDAO utilisateurDAO;

    public SignUpController() throws SQLException {
        utilisateurDAO = new LogInDAO();
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (utilisateurDAO.isEmailExist(email)) {
            showAlert("Error", "Email already exists.");
            return;
        }

        Doctor newUser = new Doctor(firstName, lastName, email, password);
        int newDoctorId = utilisateurDAO.insertDoctorAndGetId(newUser);

        if (newDoctorId > 0) {
            setLoggedInDoctorId(newDoctorId); // Store doctor ID
            loadView("/view/Dashboard.fxml", event);
        } else {
            showAlert("Error", "Failed to create an account.");
        }
    }
    public void handlelogininSignUp(ActionEvent event) {
        loadView("/view/Login.fxml", event);
    }

  
}
