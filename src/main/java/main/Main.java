package main;

import controller.BaseController;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Setup de la langue
        BaseController.currentLocale = Locale.ENGLISH;
        BaseController.bundle = ResourceBundle.getBundle("lang.messages", BaseController.currentLocale);

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/Login.fxml"),
            BaseController.bundle
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setUserData("/view/Login.fxml");

        primaryStage.setTitle(BaseController.bundle.getString("app.title"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}