package controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import model.Patient;
import model.PatientDAO;

public class AppointmentsController extends BaseController {

    @FXML private Label breadcrumbLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevYearBtn, prevMonthBtn, prevWeekBtn, nextWeekBtn, nextMonthBtn, nextYearBtn;
    @FXML private Label monthYearLabel;
    @FXML private VBox calendarContainer;
    @FXML private TextField searchField;
     private String searchQuery = null;
    @FXML
      private ImageView searchIcon;

    private LocalDate currentWeekStart;
    private PatientDAO patientDAO;
    private ResourceBundle bundle = BaseController.bundle;
     
    
     @FXML
    public void initialize() {
        super.initialize();
        super.initializeBase();

        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(bundle.getString("dashboard.appointments"));
        }

        try {
            patientDAO = new PatientDAO();
        } catch (SQLException e) {
            logError(e);
            return;
        }
        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        setupGridConstraints();

        prevYearBtn.setOnAction(e -> shiftWeek(-52));
        prevMonthBtn.setOnAction(e -> shiftWeek(-4));
        prevWeekBtn.setOnAction(e -> shiftWeek(-1));
        nextWeekBtn.setOnAction(e -> shiftWeek(1));
        nextMonthBtn.setOnAction(e -> shiftWeek(4));
        nextYearBtn.setOnAction(e -> shiftWeek(52));

        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        calendarGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Platform.runLater(this::updateCalendarSafe);

        if (searchIcon != null) {    
            searchIcon.setOnMouseClicked(event -> handleSearch());
        }
        if (searchField != null) {
            searchField.setOnAction(event -> handleSearch());
        }
    }  
    
    
    private void setupGridConstraints() {
        calendarGrid.getColumnConstraints().clear();
        double colPercent = 100.0 / 7;
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(colPercent);
            cc.setMinWidth(80);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }

        calendarGrid.getRowConstraints().clear();
        RowConstraints rc1 = new RowConstraints();
        rc1.setPercentHeight(20);
        rc1.setMinHeight(50);
        rc1.setVgrow(Priority.NEVER);
        RowConstraints rc2 = new RowConstraints();
        rc2.setPercentHeight(80);
        rc2.setMinHeight(100);
        rc2.setVgrow(Priority.ALWAYS);
        calendarGrid.getRowConstraints().addAll(rc1, rc2);
    }

    private void updateCalendarSafe() {
        try {
            updateCalendar();
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    private void updateCalendar() throws SQLException {
    calendarGrid.getChildren().clear();
    YearMonth ym = YearMonth.from(currentWeekStart);
    Locale displayLocale = bundle.getLocale();
    monthYearLabel.setText(
        ym.getMonth().getDisplayName(TextStyle.FULL, displayLocale) + " " + ym.getYear()
    );
    List<Patient> appointments = patientDAO.getAppointmentsForDoctorAndWeek(
        getLoggedInDoctorId(),
        currentWeekStart,
        currentWeekStart.plusDays(6)
    );
    List<LocalDate> allDays = IntStream.range(0, 7)
        .mapToObj(i -> currentWeekStart.plusDays(i))
        .toList();
    List<DayEntry> dayEntries = new ArrayList<>();
    for (LocalDate day : allDays) {
        List<Patient> daily = appointments.stream()
            .filter(p -> day.equals(p.getAppointmentDate()))
            .toList();
        dayEntries.add(new DayEntry(day, daily));
    }
    if (searchQuery != null && !searchQuery.isBlank()) {
        dayEntries.sort((e1, e2) -> {
            boolean match1 = e1.daily.stream()
                .anyMatch(p -> (p.getFirstName() + " " + p.getLastName())
                                .toLowerCase().contains(searchQuery));
            boolean match2 = e2.daily.stream()
                .anyMatch(p -> (p.getFirstName() + " " + p.getLastName())
                                .toLowerCase().contains(searchQuery));
            if (match1 && !match2) return -1;
            if (!match1 && match2) return 1;
            return e1.day.compareTo(e2.day);
        });
    }
    for (int col = 0; col < dayEntries.size(); col++) {
        DayEntry entry = dayEntries.get(col);
        LocalDate day = entry.day;
        List<Patient> daily = entry.daily;
        calendarGrid.add(createHeaderCell(day, displayLocale), col, 0);
        calendarGrid.add(createAppointmentCell(daily), col, 1);
    }
}
   private static class DayEntry {
    final LocalDate day;
    final List<Patient> daily;
    DayEntry(LocalDate day, List<Patient> daily) {
        this.day = day;
        this.daily = daily;
    }
   }


   private StackPane createHeaderCell(LocalDate day, Locale locale) {
    StackPane cell = new StackPane();
    cell.getStyleClass().add("calendar-header-cell");

    Label name = new Label(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale));
    name.getStyleClass().add("calendar-header-cell-label");

    Label num = new Label(String.valueOf(day.getDayOfMonth()));
    num.getStyleClass().add("calendar-header-cell-daynum");

    VBox vbox = new VBox(5, name, num);
    vbox.setAlignment(Pos.CENTER);
    cell.getChildren().add(vbox);

    return cell;
}
    private void shiftWeek(int weeks) {
        currentWeekStart = currentWeekStart.plusWeeks(weeks);
        updateCalendarSafe();
    }

    private void logError(Exception ex) {
        Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, ex);
    }
    public void changeLanguage() {
        Locale newLocale = bundle.getLocale().equals(Locale.ENGLISH) 
            ? Locale.FRANCE 
            : Locale.ENGLISH;
        bundle = ResourceBundle.getBundle("lang.messages", newLocale);
        BaseController.bundle = bundle;
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(bundle.getString("dashboard.appointments"));
        }
        updateCalendarSafe();
    }
  
   @FXML
   private void handleSearch() {
    System.out.println(">>> handleSearch() appelé. Texte recherché = " + searchField.getText());
    String input = searchField.getText();
    searchQuery = (input != null && !input.isBlank()) ? input.trim().toLowerCase() : null;
    updateCalendarSafe();
     }

   private StackPane createAppointmentCell(List<Patient> list) {
    StackPane cell = new StackPane();
    cell.getStyleClass().add("calendar-appointment-cell");

    VBox vbox = new VBox(4);
    vbox.setPadding(new Insets(5));
    vbox.setAlignment(Pos.TOP_LEFT);

    if (list.isEmpty()) {
        Label none = new Label(bundle.getString("label.none"));
        none.getStyleClass().add("calendar-appointment-label-none");
        vbox.getChildren().add(none);
    } else {
        List<Patient> prioritized = list;
        if (searchQuery != null && !searchQuery.isBlank()) {
            prioritized = list.stream()
                .sorted((p1, p2) -> {
                    boolean match1 = (p1.getFirstName() + " " + p1.getLastName())
                                        .toLowerCase()
                                        .contains(searchQuery);
                    boolean match2 = (p2.getFirstName() + " " + p2.getLastName())
                                        .toLowerCase()
                                        .contains(searchQuery);
                    if (match1 == match2) return 0;
                    return match1 ? -1 : 1;
                })
                .toList();
        }

        for (Patient p : prioritized) {
            String timePart = (p.getAppointmentTime() != null) ? p.getAppointmentTime().toString() : "";
            String text = timePart + " - " + p.getFirstName() + " " + p.getLastName();
            Label lbl = new Label(text);
            lbl.setWrapText(true);
            lbl.getStyleClass().add("calendar-appointment-label");
            vbox.getChildren().add(lbl);
        }
    }

    cell.getChildren().add(vbox);
    return cell;
}

}
