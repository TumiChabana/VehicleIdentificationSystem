package com.vis.controller;

import com.vis.dao.PoliceDAO;
import com.vis.dao.VehicleDAO;
import com.vis.model.PoliceReport;
import com.vis.model.Vehicle;
import com.vis.model.Violation;
import com.vis.util.DBConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PoliceController
        extends BaseModuleController implements Initializable {

    // ── REPORTS TAB ──────────────────────────────
    @FXML private ComboBox<String> reportTypeFilter;
    @FXML private TextField reportSearchField;
    @FXML private TableView<PoliceReport> reportTable;
    @FXML private TableColumn<PoliceReport, String> colRVehicle;
    @FXML private TableColumn<PoliceReport, String> colRDate;
    @FXML private TableColumn<PoliceReport, String> colRType;
    @FXML private TableColumn<PoliceReport, String> colROfficer;
    @FXML private TableColumn<PoliceReport, String> colRDesc;
    @FXML private Label reportStatusLabel;
    @FXML private Label reportFormStatus;

    @FXML private ComboBox<Vehicle> reportVehicleCombo;
    @FXML private DatePicker reportDatePicker;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextField reportOfficerField;
    @FXML private TextArea reportDescField;

    // ── VIOLATIONS TAB ───────────────────────────
    @FXML private ComboBox<String> violationStatusFilter;
    @FXML private TextField violationSearchField;
    @FXML private TableView<Violation> violationTable;
    @FXML private TableColumn<Violation, String> colVVehicle;
    @FXML private TableColumn<Violation, String> colVDate;
    @FXML private TableColumn<Violation, String> colVType;
    @FXML private TableColumn<Violation, String> colVFine;
    @FXML private TableColumn<Violation, String> colVStatus;
    @FXML private Label violationStatusLabel;
    @FXML private Label violationFormStatus;

    @FXML private ComboBox<Vehicle> violationVehicleCombo;
    @FXML private DatePicker violationDatePicker;
    @FXML private ComboBox<String> violationTypeCombo;
    @FXML private TextField violationFineField;
    @FXML private ComboBox<String> violationStatusCombo;

    @FXML private Label userLabel;
    @FXML private Label moduleTitleLabel;
    @FXML private Label moduleSubLabel;
    @FXML private Button dashboardBtn;
    @FXML private MenuBar policeMenuBar;

    // ── DAOs ─────────────────────────────────────
    private final PoliceDAO policeDAO   = new PoliceDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    private List<PoliceReport> allReports;
    private List<Violation> allViolations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupReportTableColumns();
        setupViolationTableColumns();
        setupComboOptions();
    }


    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());
        dashboardBtn.setVisible(
                "ADMIN".equals(currentUser.getRole()));
        dashboardBtn.setManaged(
                "ADMIN".equals(currentUser.getRole()));
        setupForRole();
        loadAllDataAsync();
    }

    // ── ASYNC LOADING ─────────────────────────────

    private void loadAllDataAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String role = currentUser.getRole();
                int userId  = currentUser.getUserId();

                if ("POLICE".equals(role)) {
                    // Officer sees only their own records
                    allReports    =
                            policeDAO.getReportsByUserId(userId);
                    allViolations =
                            policeDAO.getViolationsByUserId(userId);
                } else {
                    // Admin sees everything
                    allReports    = policeDAO.getAllReports();
                    allViolations = policeDAO.getAllViolations();
                }

                List<Vehicle> vehicles =
                        vehicleDAO.getAllVehiclesWithOwners();

                Platform.runLater(() -> {
                    reportTable.setItems(
                            FXCollections.observableArrayList(
                                    allReports));
                    violationTable.setItems(
                            FXCollections.observableArrayList(
                                    allViolations));
                    reportVehicleCombo.setItems(
                            FXCollections.observableArrayList(
                                    vehicles));
                    violationVehicleCombo.setItems(
                            FXCollections.observableArrayList(
                                    vehicles));
                });
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── TABLE COLUMNS ─────────────────────────────

    private void setupReportTableColumns() {
        reportTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colRVehicle.prefWidthProperty().bind(
                reportTable.widthProperty().multiply(0.15));
        colRDate.prefWidthProperty().bind(
                reportTable.widthProperty().multiply(0.13));
        colRType.prefWidthProperty().bind(
                reportTable.widthProperty().multiply(0.15));
        colROfficer.prefWidthProperty().bind(
                reportTable.widthProperty().multiply(0.20));
        colRDesc.prefWidthProperty().bind(
                reportTable.widthProperty().multiply(0.34));

        colRVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colRDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDate().toString()));
        colRType.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getReportType()));
        colROfficer.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getOfficerName()));
        colRDesc.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDescription()));

        // Color code report type
        colRType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "Theft" ->
                                "-fx-text-fill: #e63946; " +
                                        "-fx-font-weight: bold;";
                        case "Accident" ->
                                "-fx-text-fill: #ffaa00; " +
                                        "-fx-font-weight: bold;";
                        case "Suspicious" ->
                                "-fx-text-fill: #aa88ff; " +
                                        "-fx-font-weight: bold;";
                        default ->
                                "-fx-text-fill: #aaaacc;";
                    });
                }
            }
        });
    }

    private void setupViolationTableColumns() {
        violationTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colVVehicle.prefWidthProperty().bind(
                violationTable.widthProperty().multiply(0.18));
        colVDate.prefWidthProperty().bind(
                violationTable.widthProperty().multiply(0.15));
        colVType.prefWidthProperty().bind(
                violationTable.widthProperty().multiply(0.25));
        colVFine.prefWidthProperty().bind(
                violationTable.widthProperty().multiply(0.17));
        colVStatus.prefWidthProperty().bind(
                violationTable.widthProperty().multiply(0.22));

        colVVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colVDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getViolationDate().toString()));
        colVType.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getViolationType()));
        colVFine.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "M " + d.getValue().getFineAmount()));
        colVStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        // Color code status
        colVStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle("Paid".equals(item)
                            ? "-fx-text-fill: #44cc88; " +
                            "-fx-font-weight: bold;"
                            : "-fx-text-fill: #e63946; " +
                            "-fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupComboOptions() {
        // Report type filter
        reportTypeFilter.setItems(
                FXCollections.observableArrayList(
                        "All", "Accident", "Theft",
                        "Suspicious", "Other"));

        // Report type form
        reportTypeCombo.setItems(
                FXCollections.observableArrayList(
                        "Accident", "Theft",
                        "Suspicious", "Other"));

        // Violation type
        violationTypeCombo.setItems(
                FXCollections.observableArrayList(
                        "Speeding", "Running Red Light",
                        "No Seatbelt", "Illegal Parking",
                        "Drunk Driving", "No License",
                        "Unregistered Vehicle",
                        "No Insurance", "Other"));

        // Violation status filter + form
        violationStatusFilter.setItems(
                FXCollections.observableArrayList(
                        "All", "Paid", "Unpaid"));
        violationStatusCombo.setItems(
                FXCollections.observableArrayList(
                        "Unpaid", "Paid"));
        violationStatusCombo.setValue("Unpaid");
    }

    // ── REPORT CRUD ───────────────────────────────

    @FXML
    private void handleAddReport() {
        reportVehicleCombo.setValue(null);
        reportDatePicker.setValue(LocalDate.now());
        reportTypeCombo.setValue(null);
        reportOfficerField.clear();
        reportDescField.clear();
    }

    @FXML
    private void handleSaveReport() {
        if (reportVehicleCombo.getValue() == null
                || reportDatePicker.getValue() == null
                || reportTypeCombo.getValue() == null
                || reportOfficerField.getText().isEmpty()) {
            showStatus(reportFormStatus,
                    "Please fill in all required fields.",
                    false);
            return;
        }

        PoliceReport r = new PoliceReport();
        r.setVehicleId(
                reportVehicleCombo.getValue().getVehicleId());
        r.setDate(reportDatePicker.getValue());
        r.setReportType(reportTypeCombo.getValue());
        r.setOfficerName(reportOfficerField.getText().trim());
        r.setDescription(reportDescField.getText().trim());
        r.setCreatedByUserId(currentUser.getUserId());

        boolean success = policeDAO.addReport(r);
        showStatus(reportFormStatus,
                success ? "Report filed successfully."
                        : "Failed to file report.", success);
        if (success) {
            handleAddReport();
            loadAllDataAsync();
        }
    }

    @FXML
    private void handleDeleteReport() {
        PoliceReport r = reportTable.getSelectionModel()
                .getSelectedItem();
        if (r == null) {
            showStatus(reportStatusLabel,
                    "Select a report to delete.", false);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete this report?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = policeDAO.deleteReport(
                        r.getRecordId());
                showStatus(reportStatusLabel,
                        ok ? "Report deleted."
                                : "Delete failed.", ok);
                if (ok) loadAllDataAsync();
            }
        });
    }

    @FXML
    private void handleReportFilter() {
        String type = reportTypeFilter.getValue();
        if (type == null || "All".equals(type)) {
            reportTable.setItems(
                    FXCollections.observableArrayList(allReports));
            return;
        }
        List<PoliceReport> filtered = allReports.stream()
                .filter(r -> type.equals(r.getReportType()))
                .collect(Collectors.toList());
        reportTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReportSearch() {
        String q = reportSearchField.getText()
                .toLowerCase().trim();
        if (q.isEmpty()) {
            reportTable.setItems(
                    FXCollections.observableArrayList(allReports));
            return;
        }
        List<PoliceReport> filtered = allReports.stream()
                .filter(r ->
                        (r.getOfficerName() != null &&
                                r.getOfficerName().toLowerCase().contains(q))
                                || (r.getVehicleReg() != null &&
                                r.getVehicleReg().toLowerCase()
                                        .contains(q)))
                .collect(Collectors.toList());
        reportTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearReport() {
        reportSearchField.clear();
        reportTypeFilter.setValue(null);
        reportTable.setItems(
                FXCollections.observableArrayList(allReports));
    }

    // ── VIOLATION CRUD ────────────────────────────

    @FXML
    private void handleAddViolation() {
        violationVehicleCombo.setValue(null);
        violationDatePicker.setValue(LocalDate.now());
        violationTypeCombo.setValue(null);
        violationFineField.clear();
        violationStatusCombo.setValue("Unpaid");
    }

    @FXML
    private void handleSaveViolation() {
        if (violationVehicleCombo.getValue() == null
                || violationDatePicker.getValue() == null
                || violationTypeCombo.getValue() == null
                || violationFineField.getText().isEmpty()) {
            showStatus(violationFormStatus,
                    "Please fill in all required fields.",
                    false);
            return;
        }

        try {
            Violation v = new Violation();
            v.setVehicleId(
                    violationVehicleCombo.getValue()
                            .getVehicleId());
            v.setViolationDate(
                    violationDatePicker.getValue());
            v.setViolationType(
                    violationTypeCombo.getValue());
            v.setFineAmount(Double.parseDouble(
                    violationFineField.getText().trim()));
            v.setStatus(
                    violationStatusCombo.getValue() != null
                            ? violationStatusCombo.getValue()
                            : "Unpaid");
            v.setCreatedByUserId(currentUser.getUserId());

            boolean success = policeDAO.addViolation(v);
            showStatus(violationFormStatus,
                    success ? "Violation recorded."
                            : "Failed.", success);
            if (success) {
                handleAddViolation();
                loadAllDataAsync();
            }
        } catch (NumberFormatException e) {
            showStatus(violationFormStatus,
                    "Fine amount must be a number.", false);
        }
    }

    @FXML
    private void handleMarkPaid() {
        updateViolationStatus("Paid");
    }

    @FXML
    private void handleMarkUnpaid() {
        updateViolationStatus("Unpaid");
    }

    private void updateViolationStatus(String status) {
        Violation v = violationTable.getSelectionModel()
                .getSelectedItem();
        if (v == null) {
            showStatus(violationStatusLabel,
                    "⚠ Select a violation first.", false);
            return;
        }
        boolean ok = policeDAO.updateViolationStatus(
                v.getViolationId(), status);
        showStatus(violationStatusLabel,
                ok ? "Status updated to " + status + "."
                        : "Update failed.", ok);
        if (ok) loadAllDataAsync();
    }

    @FXML
    private void handleDeleteViolation() {
        Violation v = violationTable.getSelectionModel()
                .getSelectedItem();
        if (v == null) {
            showStatus(violationStatusLabel,
                    "Select a violation to delete.", false);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete this violation?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = policeDAO.deleteViolation(
                        v.getViolationId());
                showStatus(violationStatusLabel,
                        ok ? "Deleted." : "Failed.", ok);
                if (ok) loadAllDataAsync();
            }
        });
    }

    @FXML
    private void handleViolationFilter() {
        String status = violationStatusFilter.getValue();
        if (status == null || "All".equals(status)) {
            violationTable.setItems(
                    FXCollections.observableArrayList(
                            allViolations));
            return;
        }
        List<Violation> filtered = allViolations.stream()
                .filter(v -> status.equals(v.getStatus()))
                .collect(Collectors.toList());
        violationTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleViolationSearch() {
        String q = violationSearchField.getText()
                .toLowerCase().trim();
        if (q.isEmpty()) {
            violationTable.setItems(
                    FXCollections.observableArrayList(
                            allViolations));
            return;
        }
        List<Violation> filtered = allViolations.stream()
                .filter(v ->
                        (v.getViolationType() != null &&
                                v.getViolationType().toLowerCase()
                                        .contains(q))
                                || (v.getVehicleReg() != null &&
                                v.getVehicleReg().toLowerCase()
                                        .contains(q)))
                .collect(Collectors.toList());
        violationTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearViolation() {
        violationSearchField.clear();
        violationStatusFilter.setValue(null);
        violationTable.setItems(
                FXCollections.observableArrayList(allViolations));
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML private void goToDashboard() {
        goToDashboard(userLabel);
    }
    @FXML private void handleRefresh() { loadAllDataAsync(); }
    @FXML private void handleExit() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void handleLogout() {
        try {
            DBConnection.closeConnection();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, "/styles/login.css");
            Stage stage =
                    (Stage) userLabel.getScene().getWindow();
            stage.setScene(scene);
            BaseModuleController.applyStageDefaults(stage);
        } catch (Exception e) {
            System.err.println("Logout: " + e.getMessage());
        }
    }

    @FXML private void openWorkshop() {
        navigateToModule("/fxml/Workshop.fxml",
                "/styles/workshop.css", userLabel);
    }
    @FXML private void openCustomer() {
        navigateToModule("/fxml/Customer.fxml",
                "/styles/customer.css", userLabel);
    }
    @FXML private void openInsurance() {
        navigateToModule("/fxml/Insurance.fxml",
                "/styles/insurance.css", userLabel);
    }

    // ── HELPERS ───────────────────────────────────

    private void showStatus(Label label,
                            String msg, boolean ok) {
        label.setText(msg);
        label.setStyle(ok
                ? "-fx-text-fill: #44cc88;"
                : "-fx-text-fill: #e63946;");
        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        Duration.seconds(4),
                        e -> label.setText("")
                )).play();
    }

    private void applyStyles(Scene scene, String css) {
        java.net.URL base =
                getClass().getResource("/styles/base.css");
        java.net.URL page =
                getClass().getResource(css);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }

    private void setupForRole() {
        boolean isPolice =
                "POLICE".equals(currentUser.getRole());
        boolean isAdmin =
                "ADMIN".equals(currentUser.getRole());

        // Dashboard button — admin only
        dashboardBtn.setVisible(isAdmin);
        dashboardBtn.setManaged(isAdmin);

        if (isPolice) {
            // Personalize header for officer
            moduleTitleLabel.setText("OFFICER DASHBOARD");
            moduleSubLabel.setText(
                    "Your reports and recorded violations");

            // Officer cannot delete reports
            // they didn't file — hide delete for safety
            // They CAN add new ones though
        }

        applyRoleBasedMenuBar(policeMenuBar);

        if (isAdmin) {
            moduleTitleLabel.setText("POLICE MODULE");
            moduleSubLabel.setText(
                    "All Reports & Violations");
        }
    }


}