package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.scene.control.Label;

public class MainController extends BaseController {

    @FXML
    private BorderPane chartContainer;

    private BarChart<String, Number> barChart;

    @Override
    public void initialize() {
        super.initialize();  // Call initializeBase() in BaseController
        super.initializeBase();
        createBarChart();

        
        populateChartWithData();
       

        // Apply dark theme if enabled
        Platform.runLater(() -> {
            if (chartContainer.getScene() != null && darkModeEnabled) {
                String darkStyleSheet = getClass().getResource(darkStylePath).toExternalForm();
                if (!chartContainer.getScene().getStylesheets().contains(darkStyleSheet)) {
                    chartContainer.getScene().getStylesheets().add(darkStyleSheet);
                }
            }
        });
         Platform.runLater(this::updateTodayCounts);
    }

    @Override
    public void setLoggedInDoctorId(int id) {
         super.setLoggedInDoctorId(id);
    
    populateChartWithData();
    
    }
    
    
    
    @FXML
    private Label consultationsLabel;

    @FXML
    private Label surgeriesLabel;

private void updateTodayCounts() {
    // Only run when this view actually defines those labels
    if (consultationsLabel == null || surgeriesLabel == null) {
        return;
    }

    int doctorId = getLoggedInDoctorId();
    int consultations = 0, surgeries = 0;

    String sql =
        "SELECT appointment_type, COUNT(*) AS cnt " +
        "FROM patients " +
        "WHERE doctorId = ? AND DATE(appointment_date) = CURDATE() " +
        "GROUP BY appointment_type";

    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, doctorId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("appointment_type");
                int cnt = rs.getInt("cnt");
                if ("consultation".equalsIgnoreCase(type)) {
                    consultations = cnt;
                } else if ("surgery".equalsIgnoreCase(type)) {
                    surgeries = cnt;
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Retrieve the static labels from your bundle
    ResourceBundle bundle = BaseController.bundle;
    String consultText = bundle.getString("dashboard.Appa"); // "Consultations"
    String surgeryText = bundle.getString("dashboard.Apps"); // "Surgeries"

    // Compose final display strings
    String consultDisplay = consultations + " " + consultText;
    String surgeryDisplay = surgeries   + " " + surgeryText;

    // Update on the JavaFX Application Thread
    Platform.runLater(() -> {
        consultationsLabel.setText(consultDisplay);
        surgeriesLabel.setText(surgeryDisplay);
    });
}




    private void createBarChart() {
        ResourceBundle bundle = BaseController.bundle;

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(FXCollections.observableArrayList(
            bundle.getString("dashboard.diagram.x1"), // Monday
            bundle.getString("dashboard.diagram.x2"), // Tuesday
            bundle.getString("dashboard.diagram.x3"), // Wednesday
            bundle.getString("dashboard.diagram.x4"), // Thursday
            bundle.getString("dashboard.diagram.x5"), // Friday
            bundle.getString("dashboard.diagram.x6"), // Saturday
            bundle.getString("dashboard.diagram.x7")  // Sunday
        ));

        NumberAxis yAxis = new NumberAxis(0, 10, 1);
        yAxis.setLabel(bundle.getString("dashboard.diagram.y"));
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(true);

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefWidth(300);
        barChart.setPrefHeight(250);
        barChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        barChart.setBarGap(5); // smaller gap between bars (default 10)
        barChart.setCategoryGap(15); // space between categories, controls bar width indirectly

        chartContainer.setCenter(barChart);
    }

    private void populateChartWithData() {

    ResourceBundle bundle = BaseController.bundle;

    int[] patientCounts = new int[7]; // Monday to Sunday
    Arrays.fill(patientCounts, 0);

    int loggedInDoctorId = getLoggedInDoctorId();

    try (Connection conn = Database.getConnection()) {
        String sql = "SELECT DAYOFWEEK(appointment_date) AS day_of_week, COUNT(*) AS patient_count " +
                     "FROM patients " +
                     "WHERE doctorId = ? AND YEARWEEK(appointment_date, 1) = YEARWEEK(CURDATE(), 1) " +
                     "GROUP BY day_of_week";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, loggedInDoctorId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int mysqlDay = rs.getInt("day_of_week"); // 1=Sunday ... 7=Saturday
                int count = rs.getInt("patient_count");

                int javaIndex = (mysqlDay + 5) % 7; // Map Sunday=1 to index 6, Monday=2 to index 0, etc.
                patientCounts[javaIndex] = count;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return;
    }

    String[] days = new String[] {
        bundle.getString("dashboard.diagram.x1"), // Monday
        bundle.getString("dashboard.diagram.x2"), // Tuesday
        bundle.getString("dashboard.diagram.x3"), // Wednesday
        bundle.getString("dashboard.diagram.x4"), // Thursday
        bundle.getString("dashboard.diagram.x5"), // Friday
        bundle.getString("dashboard.diagram.x6"), // Saturday
        bundle.getString("dashboard.diagram.x7")  // Sunday
    };

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    for (int i = 0; i < days.length; i++) {
        series.getData().add(new XYChart.Data<>(days[i], patientCounts[i]));
    }

    barChart.getData().clear();
    barChart.getData().add(series);

    // Ne pas fixer de max ni limites sur l’axe Y, il s’adapte automatiquement
}
    
    
    

}
