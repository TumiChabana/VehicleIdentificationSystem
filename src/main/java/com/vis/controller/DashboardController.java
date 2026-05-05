package com.vis.controller;

import com.vis.dao.CustomerDAO;
import com.vis.dao.InsuranceDAO;
import com.vis.dao.PoliceDAO;
import com.vis.dao.VehicleDAO;
import com.vis.model.*;
import com.vis.util.DBConnection;
import com.vis.util.DataCache;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import java.io.File;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;

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

    // ── VEHICLE HOVER OVERLAY ─────────────────────
    @FXML private StackPane overlayPane;       // injected from FXML
    @FXML private VBox vehicleDetailCard;
    @FXML private ImageView overlayCarImage;
    @FXML private Label overlayReg;
    @FXML private Label overlayMake;
    @FXML private Label overlayModel;
    @FXML private Label overlayYear;
    @FXML private Label overlayColor;
    @FXML private Label overlayOwner;

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
    private final VehicleDAO vehicleDAO     = new VehicleDAO();
    private final CustomerDAO customerDAO   = new CustomerDAO();
    private final PoliceDAO policeDAO       = new PoliceDAO();
    private final InsuranceDAO insuranceDAO = new InsuranceDAO();

    private User currentUser;
    private List<Violation> allViolations;
    private static final int VIOLATIONS_PER_PAGE = 5;
    private Vehicle currentOverlayVehicle = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDate();
        setupTableColumns();
        setupDropShadow();
    }

    public void initUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
        roleLabel.setText(user.getRole());

        // Show spinner while loading
        dbProgressIndicator.setProgress(-1);
        systemProgressBar.setProgress(0);

        // Load data in background — no more freezing
        loadDashboardDataAsync();
        populateActivityList();
    }

    // ── SETUP ─────────────────────────────────────

    private void setupDate() {
        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        dateLabel.setText(today);
    }

    private void setupTableColumns() {
        // Vehicle table — no DB calls in cell factories

        colReg.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getRegistrationNumber()));
        colMake.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMake()));
        colModel.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getModel()));
        colYear.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getYear())));
        colColor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getColor()));



        // ⚠Owner column — we'll populate this from
        // pre-loaded data, not per-row DB calls
        colOwner.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getOwnerName() != null
                                ? d.getValue().getOwnerName()
                                : "—"));

        // Violation table
        colViolationType.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getViolationType()));
        colViolationFine.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "M " + d.getValue().getFineAmount()));
        colViolationStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        // Color code violation status
        colViolationStatus.setCellFactory(col -> new TableCell<>() {
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

        vehicleTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && row.getItem() != null) {
                    if (overlayPane.isVisible()
                            && row.getItem().equals(currentOverlayVehicle)) {
                        // Clicking the same row again closes it
                        hideVehicleOverlay();
                    } else {
                        showVehicleOverlay(row.getItem());
                    }
                }
            });
            return row;
        });


// Hide overlay when mouse leaves it
// Wire this in FXML via onMouseExited="#hideVehicleOverlay"
    }

    // ── VEHICLE OVERLAY ───────────────────────────

    private void showVehicleOverlay(Vehicle v) {
        currentOverlayVehicle = v;

        // Populate text
        overlayReg.setText(v.getRegistrationNumber());
        overlayMake.setText(v.getMake());
        overlayModel.setText(v.getModel());
        overlayYear.setText(String.valueOf(v.getYear()));
        overlayColor.setText(v.getColor());
        overlayOwner.setText(v.getOwnerName() != null
                ? v.getOwnerName() : "—");

        // Load image safely
        Image img = null;
        String path = v.getImagePath();
        if (path != null && !path.isBlank()) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    img = new Image("file:" + path,
                            340, 210, false, true);
                } catch (Exception ignored) {}
            }
        }

        if (img == null) {
            try {
                var stream = getClass()
                        .getResourceAsStream("/images/logo3.png");
                if (stream != null) {
                    img = new Image(stream, 340, 210, false, true);
                }
            } catch (Exception ignored) {}
        }

        overlayCarImage.setManaged(true);
        overlayCarImage.setVisible(true);
        overlayCarImage.setFitWidth(340);
        overlayCarImage.setFitHeight(210);
        overlayCarImage.setPreserveRatio(false);

        if (img != null) {
            overlayCarImage.setImage(img);
        } else {
            overlayCarImage.setVisible(false);
            overlayCarImage.setManaged(false);
        }

        // If already visible, just swap content — no re-animation
        if (overlayPane.isVisible()) return;

        // Animate in from the right
        overlayPane.setVisible(true);
        overlayPane.setManaged(false);
        overlayPane.setOpacity(0);

        // Slide card in from right
        vehicleDetailCard.setTranslateX(60);

        FadeTransition fade = new FadeTransition(
                Duration.millis(260), overlayPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(
                Duration.millis(260), vehicleDetailCard);
        slide.setFromX(60);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    @FXML
    public void hideVehicleOverlay() {
        if (!overlayPane.isVisible()) return;
        currentOverlayVehicle = null;

        FadeTransition fade = new FadeTransition(
                Duration.millis(200), overlayPane);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition slide = new TranslateTransition(
                Duration.millis(200), vehicleDetailCard);
        slide.setFromX(0);
        slide.setToX(60);
        slide.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setOnFinished(e -> {
            overlayPane.setVisible(false);
            vehicleDetailCard.setTranslateX(0);
        });
        pt.play();
    }

    private void setupDropShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#e63946"));
        shadow.setRadius(18);
        shadow.setSpread(0.25);
        addVehicleBtn.setEffect(shadow);
    }

    // ── ASYNC DATA LOADING ────────────────────────
    // This is the key fix — all DB work happens off the UI thread

    private void loadDashboardDataAsync() {

        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() {
                DashboardData data = new DashboardData();
                DataCache cache = DataCache.getInstance();

                if (cache.isReady()) {
                    // Cache hit — instant, no DB call needed
                    data.vehicles   = cache.vehicles;
                    data.customers  = cache.customers;
                    data.violations = cache.violations;
                    data.insurance  = cache.insurance;
                } else {
                    // Cache miss — fall back to normal DB fetch
                    data.vehicles   =
                            vehicleDAO.getAllVehiclesWithOwners();
                    data.customers  =
                            customerDAO.getAllCustomers();
                    data.violations =
                            policeDAO.getAllViolations();
                    data.insurance  =
                            insuranceDAO.getAllRecords();
                }

                updateMessage("Loading vehicles...");
                data.vehicles  = vehicleDAO.getAllVehiclesWithOwners();

                updateMessage("Loading customers...");
                data.customers = customerDAO.getAllCustomers();

                updateMessage("Loading violations...");
                data.violations = policeDAO.getAllViolations();

                updateMessage("Loading insurance...");
                data.insurance  = insuranceDAO.getAllRecords();

                return data;
            }
        };

        // Runs on UI thread when task SUCCEEDS
        task.setOnSucceeded(e -> {
            DashboardData data = task.getValue();
            updateDashboardUI(data);
            // Stop spinner, animate progress bar
            dbProgressIndicator.setProgress(1.0);
            animateProgressBar();
        });

        // Runs on UI thread when task FAILS
        task.setOnFailed(e -> {
            dbProgressIndicator.setProgress(0);
            System.err.println("Dashboard load failed: "
                    + task.getException().getMessage());
        });

        // Start the background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // dies when app closes
        thread.start();
    }

    // ── UI UPDATE — always runs on UI thread ──────

    private void updateDashboardUI(DashboardData data) {
        // Vehicles
        vehicleTable.setItems(
                FXCollections.observableArrayList(data.vehicles));
        totalVehicles.setText(
                String.valueOf(data.vehicles.size()));

        // Customers
        totalCustomers.setText(
                String.valueOf(data.customers.size()));

        // Violations
        allViolations = data.violations;
        long unpaid = data.violations.stream()
                .filter(v -> "Unpaid".equals(v.getStatus()))
                .count();
        totalViolations.setText(String.valueOf(unpaid));
        setupViolationPagination();

        // Insurance
        long active = data.insurance.stream()
                .filter(i -> "Active".equals(i.getStatus()))
                .count();
        totalInsurance.setText(String.valueOf(active));
    }

    private void animateProgressBar() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(
                                systemProgressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(
                                systemProgressBar.progressProperty(), 0.72))
        );
        timeline.play();
    }

    // ── PAGINATION ────────────────────────────────

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

    // ── ACTIVITY LIST ─────────────────────────────

    private void populateActivityList() {
        activityList.getChildren().clear();
        String[] activities = {
                "Vehicle LSO-001-AA registered",
                "Police report filed - Accident",
                "Insurance policy ZS-2024-001 activated",
                "Violation recorded - Speeding",
                "Customer Litumelo Mokoena added",
                "Service record added - Oil Change",
                "Vehicle LSO-002-BB registered",
                "Fine paid — LSO-001-AA",
                "Theft report filed - LSO-004-DD",
                "Policy ALI-2024-002 activated",
                "Customer Thabo Lekhoa added",
                "Violation - Illegal Parking",
                "Service - Full Service LSO-003-CC",
                "Vehicle LSO-005-EE registered",
                "Suspicious vehicle report filed",
                "Customer Palesa Mokitimi added",
                "Violation - Running Red Light",
                "Policy MET-2023-003 expired",
                "Service - Brake Service LSO-001-AA",
                "Vehicle LSO-007-GG registered",
                "Customer Mpho Serati added",
                "Fine paid - LSO-005-EE"
        };

        for (int i = 0; i < activities.length; i++) {
            HBox item = new HBox();
            item.getStyleClass().add("activity-item");
            item.setSpacing(8);

            VBox content = new VBox(4);
            Label text = new Label(activities[i]);
            text.getStyleClass().add("activity-text");

            Label time = new Label(
                    LocalDate.now().minusDays(i)
                            .format(DateTimeFormatter
                                    .ofPattern("dd MMM yyyy")));
            time.getStyleClass().add("activity-time");

            content.getChildren().addAll(text, time);
            item.getChildren().add(content);

            // Staggered fade in
            item.setOpacity(0);
            FadeTransition ft = new FadeTransition(
                    Duration.millis(300), item);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * 40L));
            ft.play();

            activityList.getChildren().add(item);
        }
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML private void handleRefresh() {
        dbProgressIndicator.setProgress(-1);
        loadDashboardDataAsync();
    }

    @FXML private void showDashboard() {}

    @FXML
    private void handleLogout() {
        try {
            DBConnection.closeConnection();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, "/styles/login.css");
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            BaseModuleController.smoothTransition(stage, scene);
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    @FXML private void handleExit() { Platform.exit(); }

    @FXML private void openWorkshop() {
        navigateTo("/fxml/Workshop.fxml", "/styles/workshop.css");
    }
    @FXML private void openCustomer() {
        navigateTo("/fxml/Customer.fxml", "/styles/customer.css");
    }
    @FXML private void openPolice() {
        navigateTo("/fxml/Police.fxml", "/styles/police.css");
    }
    @FXML private void openInsurance() {
        navigateTo("/fxml/Insurance.fxml", "/styles/insurance.css");
    }
    @FXML private void openAdmin() {
        navigateTo("/fxml/Admin.fxml", "/styles/dashboard.css");
    }

    private void navigateTo(String fxmlPath, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, cssPath);

            Object controller = loader.getController();
            if (controller instanceof BaseModuleController bmc) {
                bmc.initUser(currentUser);
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            BaseModuleController.smoothTransition(stage, scene);

        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void applyStyles(Scene scene, String pageCSS) {
        URL base = getClass().getResource("/styles/base.css");
        URL page = getClass().getResource(pageCSS);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }


    // ── INNER CLASS — bundles all loaded data ─────
    // Keeps the Task clean and type-safe

    private static class DashboardData {
        List<Vehicle>         vehicles;
        List<Customer>        customers;
        List<Violation>       violations;
        List<InsuranceRecord> insurance;
    }


}