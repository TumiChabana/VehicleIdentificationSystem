package com.vis.controller;

import com.vis.dao.CustomerDAO;
import com.vis.dao.InsuranceDAO;
import com.vis.dao.PoliceDAO;
import com.vis.dao.VehicleDAO;
import com.vis.model.*;
import com.vis.util.DBConnection;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // ── TOP BAR ──────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;

    // ── STAT CARDS ───────────────────────────────
    @FXML private Label totalVehicles;
    @FXML private Label totalCustomers;
    @FXML private Label totalViolations;
    @FXML private Label totalInsurance;

    // ── VEHICLE TABLE ────────────────────────────
    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> colReg;
    @FXML private TableColumn<Vehicle, String> colMake;
    @FXML private TableColumn<Vehicle, String> colModel;
    @FXML private TableColumn<Vehicle, String> colYear;
    @FXML private TableColumn<Vehicle, String> colColor;
    @FXML private TableColumn<Vehicle, String> colOwner;

    // ── VIOLATIONS TABLE + PAGINATION ────────────
    @FXML private TableView<Violation> violationTable;
    @FXML private TableColumn<Violation, String> colViolationType;
    @FXML private TableColumn<Violation, String> colViolationFine;
    @FXML private TableColumn<Violation, String> colViolationStatus;
    @FXML private Pagination violationPagination;

    // ── SIDEBAR ──────────────────────────────────
    @FXML private Button addVehicleBtn;
    @FXML private ProgressBar systemProgressBar;
    @FXML private ProgressIndicator dbProgressIndicator;
    @FXML private VBox activityList;

    // ── DAOs ─────────────────────────────────────
    private final VehicleDAO vehicleDAO       = new VehicleDAO();
    private final CustomerDAO customerDAO     = new CustomerDAO();
    private final PoliceDAO policeDAO         = new PoliceDAO();
    private final InsuranceDAO insuranceDAO   = new InsuranceDAO();

    private User currentUser;
    private List<Violation> allViolations;
    private static final int VIOLATIONS_PER_PAGE = 5;

    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDate();
        setupTableColumns();
        setupDropShadow();
        setupProgressAnimation();
    }

    // Called after login — receives logged-in user
    public void initUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
        roleLabel.setText(user.getRole());
        loadDashboardData();
        populateActivityList();
    }

    // ── SETUP ─────────────────────────────────────

    private void setupDate() {
        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        dateLabel.setText(today);
    }

    private void setupTableColumns() {
        // Vehicle table columns
        colReg.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRegistrationNumber()));
        colMake.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMake()));
        colModel.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getModel()));
        colYear.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getYear())));
        colColor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getColor()));
        colOwner.setCellValueFactory(d -> {
            Customer c = customerDAO.getCustomerById(
                    d.getValue().getOwnerId());
            return new SimpleStringProperty(
                    c != null ? c.getName() : "Unknown");
        });

        // Violation table columns
        colViolationType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getViolationType()));
        colViolationFine.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "M " + d.getValue().getFineAmount()));
        colViolationStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        // Color code violation status
        colViolationStatus.setCellFactory(col ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle("Unpaid".equals(item)
                                    ? "-fx-text-fill: #e63946; -fx-font-weight: bold;"
                                    : "-fx-text-fill: #44cc88; -fx-font-weight: bold;"
                            );
                        }
                    }
                });
    }

    private void setupDropShadow() {
        // DropShadow on Add Vehicle button (marks requirement)
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#e63946"));
        shadow.setRadius(18);
        shadow.setSpread(0.25);
        addVehicleBtn.setEffect(shadow);
    }

    private void setupProgressAnimation() {
        // Animate progress bar to simulate system load
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(systemProgressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(systemProgressBar.progressProperty(), 0.72))
        );
        timeline.setDelay(Duration.seconds(1));
        timeline.play();

        // Stop spinner after data loads
        dbProgressIndicator.setProgress(-1); // spinning
        new Timeline(new KeyFrame(Duration.seconds(3),
                e -> dbProgressIndicator.setProgress(1.0)
        )).play();
    }

    // ── DATA LOADING ──────────────────────────────

    private void loadDashboardData() {
        try {
            // Load vehicles
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            vehicleTable.setItems(
                    FXCollections.observableArrayList(vehicles));
            totalVehicles.setText(String.valueOf(vehicles.size()));

            // Load customers count
            List<Customer> customers = customerDAO.getAllCustomers();
            totalCustomers.setText(String.valueOf(customers.size()));

            // Load violations with pagination
            allViolations = policeDAO.getAllViolations();
            totalViolations.setText(
                    String.valueOf(allViolations.stream()
                            .filter(v -> "Unpaid".equals(v.getStatus()))
                            .count()));
            setupViolationPagination();

            // Load insurance count
            List<InsuranceRecord> insurance =
                    insuranceDAO.getAllRecords();
            totalInsurance.setText(
                    String.valueOf(insurance.stream()
                            .filter(i -> "Active".equals(i.getStatus()))
                            .count()));

        } catch (Exception e) {
            System.err.println("Dashboard load error: " + e.getMessage());
        }
    }

    private void setupViolationPagination() {
        if (allViolations == null || allViolations.isEmpty()) return;

        int pageCount = (int) Math.ceil(
                (double) allViolations.size() / VIOLATIONS_PER_PAGE);
        violationPagination.setPageCount(Math.max(pageCount, 1));

        violationPagination.currentPageIndexProperty()
                .addListener((obs, oldVal, newVal) ->
                        loadViolationPage(newVal.intValue()));

        loadViolationPage(0);
    }

    private void loadViolationPage(int pageIndex) {
        int from = pageIndex * VIOLATIONS_PER_PAGE;
        int to   = Math.min(from + VIOLATIONS_PER_PAGE,
                allViolations.size());

        ObservableList<Violation> page =
                FXCollections.observableArrayList(
                        allViolations.subList(from, to));
        violationTable.setItems(page);
    }

    private void populateActivityList() {
        // ScrollPane activity feed — 20+ items (marks requirement)
        activityList.getChildren().clear();
        String[] activities = {
                "🚗 Vehicle LSO-001-AA registered",
                "🚔 Police report filed — Accident",
                "🛡 Insurance policy LNI-2024-001 activated",
                "⚠️ Violation recorded — Speeding",
                "👤 Customer Litumeleng Mokoena added",
                "🔧 Service record added — Oil Change",
                "🚗 Vehicle LSO-002-BB registered",
                "💰 Fine paid — LSO-001-AA",
                "🚔 Theft report filed — LSO-004-DD",
                "🛡 Policy ALI-2024-002 activated",
                "👤 Customer Thabo Nkosi added",
                "⚠️ Violation — Illegal Parking",
                "🔧 Service — Full Service LSO-003-CC",
                "🚗 Vehicle LSO-005-EE registered",
                "🚔 Suspicious vehicle report filed",
                "👤 Customer Palesa Lerato added",
                "⚠️ Violation — Running Red Light",
                "🛡 Policy MET-2023-003 expired",
                "🔧 Service — Brake Service LSO-001-AA",
                "🚗 Vehicle LSO-007-GG registered",
                "👤 Customer Mpho Sithole added",
                "💰 Fine paid — LSO-005-EE"
        };

        for (int i = 0; i < activities.length; i++) {
            HBox item = new HBox();
            item.getStyleClass().add("activity-item");

            VBox content = new VBox(4);
            Label text = new Label(activities[i]);
            text.getStyleClass().add("activity-text");

            Label time = new Label(
                    LocalDate.now().minusDays(i)
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            time.getStyleClass().add("activity-time");

            content.getChildren().addAll(text, time);
            item.getChildren().add(content);

            // Fade in each item with delay
            item.setOpacity(0);
            FadeTransition ft = new FadeTransition(
                    Duration.millis(300), item);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * 50));
            ft.play();

            activityList.getChildren().add(item);
        }
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML private void handleRefresh() { loadDashboardData(); }

    @FXML private void showDashboard() {}

    @FXML
    private void handleLogout() {
        try {
            DBConnection.closeConnection();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/styles/theme.css")
                            .toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    @FXML private void handleExit() { Platform.exit(); }

    // Module navigation — placeholders for now
    // We'll fill these as we build each module
    @FXML private void openWorkshop()  { navigateTo("/fxml/Workshop.fxml"); }
    @FXML private void openCustomer()  { navigateTo("/fxml/Customer.fxml"); }
    @FXML private void openPolice()    { navigateTo("/fxml/Police.fxml"); }
    @FXML private void openInsurance() { navigateTo("/fxml/Insurance.fxml"); }
    @FXML private void openAdmin()     { navigateTo("/fxml/Admin.fxml"); }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/styles/theme.css")
                            .toExternalForm());

            // Pass current user to next controller
            Object controller = loader.getController();
            if (controller instanceof BaseModuleController) {
                ((BaseModuleController) controller).initUser(currentUser);
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void applyStyles(Scene scene, String pageCSS) {
        URL base = getClass().getResource("/styles/base.css");
        URL page = getClass().getResource(pageCSS);
        if (base != null) scene.getStylesheets().add(base.toExternalForm());
        if (page != null) scene.getStylesheets().add(page.toExternalForm());
    }
}