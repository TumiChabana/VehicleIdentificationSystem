package com.vis.controller;

import com.vis.dao.InsuranceDAO;
import com.vis.dao.VehicleDAO;
import com.vis.model.InsuranceRecord;
import com.vis.model.Vehicle;
import com.vis.util.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InsuranceController
        extends BaseModuleController implements Initializable {

    // ── POLICIES TAB ─────────────────────────────
    @FXML private ComboBox<String> policyStatusFilter;
    @FXML private TextField policySearchField;
    @FXML private TableView<InsuranceRecord> policyTable;
    @FXML private TableColumn<InsuranceRecord, String> colIVehicle;
    @FXML private TableColumn<InsuranceRecord, String> colIProvider;
    @FXML private TableColumn<InsuranceRecord, String> colIPolicy;
    @FXML private TableColumn<InsuranceRecord, String> colIStart;
    @FXML private TableColumn<InsuranceRecord, String> colIEnd;
    @FXML private TableColumn<InsuranceRecord, String> colIPremium;
    @FXML private TableColumn<InsuranceRecord, String> colIStatus;
    @FXML private Label policyStatusLabel;
    @FXML private Label policyFormStatus;

    @FXML private ComboBox<Vehicle> policyVehicleCombo;
    @FXML private TextField fieldProvider;
    @FXML private TextField fieldPolicyNumber;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField fieldPremium;
    @FXML private ComboBox<String> policyStatusCombo;

    // ── OVERVIEW TAB ─────────────────────────────
    @FXML private Label activeCount;
    @FXML private Label expiredCount;
    @FXML private Label cancelledCount;
    @FXML private Label totalPremium;
    @FXML private ProgressBar activeProgressBar;
    @FXML private ProgressBar expiredProgressBar;
    @FXML private ProgressBar cancelledProgressBar;
    @FXML private TableView<InsuranceRecord> expiringTable;
    @FXML private TableColumn<InsuranceRecord, String> colExVehicle;
    @FXML private TableColumn<InsuranceRecord, String> colExProvider;
    @FXML private TableColumn<InsuranceRecord, String> colExPolicy;
    @FXML private TableColumn<InsuranceRecord, String> colExEnd;
    @FXML private TableColumn<InsuranceRecord, String> colExDays;

    @FXML private Label userLabel;
    @FXML private Label moduleTitleLabel;
    @FXML private Label moduleSubLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button deletePolicyBtn;
    @FXML private MenuBar insuranceMenuBar;

    // ── DAOs ─────────────────────────────────────
    private final InsuranceDAO insuranceDAO = new InsuranceDAO();
    private final VehicleDAO vehicleDAO     = new VehicleDAO();

    private List<InsuranceRecord> allPolicies;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPolicyTableColumns();
        setupExpiringTableColumns();
        setupComboOptions();
    }



    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());
        setupForRole();        // ← THIS was missing
        loadAllDataAsync();
    }

    // ── ASYNC LOADING ─────────────────────────────

    private void loadAllDataAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String role = currentUser.getRole();

                if ("INSURANCE".equals(role)) {
                    // Insurer sees only their company's policies
                    String provider = currentUser.getIdentifier();
                    allPolicies = (provider != null
                            && !provider.isBlank())
                            ? insuranceDAO.getRecordsByProvider(
                            provider)
                            : insuranceDAO.getAllRecordsWithVehicle();

                } else if ("CUSTOMER".equals(role)) {
                    // Customer sees their own policies
                    allPolicies =
                            insuranceDAO.getRecordsByCustomerId(
                                    currentUser.getCustomerId());
                } else {
                    // Admin sees everything
                    allPolicies =
                            insuranceDAO.getAllRecordsWithVehicle();
                }

                List<Vehicle> vehicles =
                        vehicleDAO.getAllVehiclesWithOwners();

                Platform.runLater(() -> {
                    policyTable.setItems(
                            FXCollections.observableArrayList(
                                    allPolicies));
                    policyVehicleCombo.setItems(
                            FXCollections.observableArrayList(
                                    vehicles));
                    updateOverview();
                });
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── TABLE COLUMNS ─────────────────────────────

    private void setupPolicyTableColumns() {
        policyTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colIVehicle.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.12));
        colIProvider.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.20));
        colIPolicy.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.16));
        colIStart.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.12));
        colIEnd.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.12));
        colIPremium.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.13));
        colIStatus.prefWidthProperty().bind(
                policyTable.widthProperty().multiply(0.12));

        colIVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colIProvider.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getProviderName()));
        colIPolicy.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getPolicyNumber()));
        colIStart.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDate() != null
                                ? d.getValue().getDate().toString()
                                : "—"));
        colIEnd.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getEndDate() != null
                                ? d.getValue().getEndDate().toString()
                                : "—"));
        colIPremium.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "M " + d.getValue().getPremiumAmount()));
        colIStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        // Color code status
        colIStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "Active" ->
                                "-fx-text-fill: #44cc88; " +
                                        "-fx-font-weight: bold;";
                        case "Expired" ->
                                "-fx-text-fill: #ffaa00; " +
                                        "-fx-font-weight: bold;";
                        case "Cancelled" ->
                                "-fx-text-fill: #e63946; " +
                                        "-fx-font-weight: bold;";
                        default -> "-fx-text-fill: #aaaacc;";
                    });
                }
            }
        });
    }

    private void setupExpiringTableColumns() {
        expiringTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colExVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colExProvider.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getProviderName()));
        colExPolicy.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getPolicyNumber()));
        colExEnd.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getEndDate().toString()));

        // Days left column — color coded
        colExDays.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }

                InsuranceRecord r = getTableView()
                        .getItems().get(getIndex());
                long days = ChronoUnit.DAYS.between(
                        LocalDate.now(), r.getEndDate());
                setText(days + " days");
                setStyle(days <= 7
                        ? "-fx-text-fill: #e63946; " +
                        "-fx-font-weight: bold;"
                        : "-fx-text-fill: #ffaa00; " +
                        "-fx-font-weight: bold;");
            }
        });
        colExDays.setCellValueFactory(d ->
                new SimpleStringProperty(""));
    }

    private void setupComboOptions() {
        policyStatusFilter.setItems(
                FXCollections.observableArrayList(
                        "All", "Active", "Expired", "Cancelled"));
        policyStatusCombo.setItems(
                FXCollections.observableArrayList(
                        "Active", "Expired", "Cancelled"));
        policyStatusCombo.setValue("Active");
    }

    // ── OVERVIEW ──────────────────────────────────

    private void updateOverview() {
        if (allPolicies == null) return;

        long active = allPolicies.stream()
                .filter(p -> "Active".equals(p.getStatus()))
                .count();
        long expired = allPolicies.stream()
                .filter(p -> "Expired".equals(p.getStatus()))
                .count();
        long cancelled = allPolicies.stream()
                .filter(p -> "Cancelled".equals(p.getStatus()))
                .count();
        double total = allPolicies.stream()
                .filter(p -> "Active".equals(p.getStatus()))
                .mapToDouble(InsuranceRecord::getPremiumAmount)
                .sum();

        activeCount.setText(String.valueOf(active));
        expiredCount.setText(String.valueOf(expired));
        cancelledCount.setText(String.valueOf(cancelled));
        totalPremium.setText(
                String.format("M %.2f", total));

        // Animate progress bars
        double total2 = allPolicies.size();
        animateBar(activeProgressBar,
                total2 > 0 ? active / total2 : 0);
        animateBar(expiredProgressBar,
                total2 > 0 ? expired / total2 : 0);
        animateBar(cancelledProgressBar,
                total2 > 0 ? cancelled / total2 : 0);

        // Expiring within 30 days
        List<InsuranceRecord> expiringSoon =
                allPolicies.stream()
                        .filter(p -> "Active".equals(p.getStatus())
                                && p.getEndDate() != null
                                && ChronoUnit.DAYS.between(
                                LocalDate.now(),
                                p.getEndDate()) <= 30
                                && ChronoUnit.DAYS.between(
                                LocalDate.now(),
                                p.getEndDate()) >= 0)
                        .collect(Collectors.toList());
        expiringTable.setItems(
                FXCollections.observableArrayList(expiringSoon));
    }

    private void animateBar(ProgressBar bar, double target) {
        new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(bar.progressProperty(), target))
        ).play();
    }

    // ── POLICY CRUD ───────────────────────────────

    @FXML
    private void handleAddPolicy() {
        policyVehicleCombo.setValue(null);
        fieldProvider.clear();
        fieldPolicyNumber.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(
                LocalDate.now().plusYears(1));
        fieldPremium.clear();
        policyStatusCombo.setValue("Active");
    }

    @FXML
    private void handleSavePolicy() {
        if (policyVehicleCombo.getValue() == null
                || fieldProvider.getText().isEmpty()
                || fieldPolicyNumber.getText().isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null
                || fieldPremium.getText().isEmpty()) {
            showStatus(policyFormStatus,
                    "Please fill in all fields.", false);
            return;
        }

        try {
            InsuranceRecord r = new InsuranceRecord();
            r.setVehicleId(
                    policyVehicleCombo.getValue().getVehicleId());
            r.setProviderName(fieldProvider.getText().trim());
            r.setPolicyNumber(
                    fieldPolicyNumber.getText().trim());
            r.setDate(startDatePicker.getValue());
            r.setEndDate(endDatePicker.getValue());
            r.setPremiumAmount(Double.parseDouble(
                    fieldPremium.getText().trim()));
            r.setStatus(policyStatusCombo.getValue());

            boolean success = insuranceDAO.addRecord(r);
            showStatus(policyFormStatus,
                    success ? "Policy saved successfully."
                            : " Failed to save policy.",
                    success);
            if (success) {
                handleAddPolicy();
                loadAllDataAsync();
            }
        } catch (NumberFormatException e) {
            showStatus(policyFormStatus,
                    "Premium must be a number.", false);
        }
    }

    @FXML
    private void handleMarkActive() {
        updateSelectedStatus("Active");
    }

    @FXML
    private void handleMarkExpired() {
        updateSelectedStatus("Expired");
    }

    @FXML
    private void handleCancelPolicy() {
        updateSelectedStatus("Cancelled");
    }

    private void updateSelectedStatus(String status) {
        InsuranceRecord r = policyTable.getSelectionModel()
                .getSelectedItem();
        if (r == null) {
            showStatus(policyStatusLabel,
                    "Select a policy first.", false);
            return;
        }
        boolean ok = insuranceDAO.updateStatus(
                r.getInsuranceId(), status);
        showStatus(policyStatusLabel,
                ok ? "Status updated to " + status + "."
                        : "Update failed.", ok);
        if (ok) loadAllDataAsync();
    }

    @FXML
    private void handleDeletePolicy() {
        InsuranceRecord r = policyTable.getSelectionModel()
                .getSelectedItem();
        if (r == null) {
            showStatus(policyStatusLabel,
                    "Select a policy to delete.", false);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(
                "Delete policy " + r.getPolicyNumber() + "?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = insuranceDAO.deleteRecord(
                        r.getInsuranceId());
                showStatus(policyStatusLabel,
                        ok ? "Policy deleted."
                                : "Delete failed.", ok);
                if (ok) loadAllDataAsync();
            }
        });
    }

    @FXML
    private void handleStatusFilter() {
        String status = policyStatusFilter.getValue();
        if (status == null || "All".equals(status)) {
            policyTable.setItems(
                    FXCollections.observableArrayList(
                            allPolicies));
            return;
        }
        policyTable.setItems(
                FXCollections.observableArrayList(
                        allPolicies.stream()
                                .filter(p -> status.equals(
                                        p.getStatus()))
                                .collect(Collectors.toList())));
    }

    @FXML
    private void handlePolicySearch() {
        String q = policySearchField.getText()
                .toLowerCase().trim();
        if (q.isEmpty()) {
            policyTable.setItems(
                    FXCollections.observableArrayList(
                            allPolicies));
            return;
        }
        policyTable.setItems(
                FXCollections.observableArrayList(
                        allPolicies.stream()
                                .filter(p ->
                                        (p.getPolicyNumber() != null &&
                                                p.getPolicyNumber().toLowerCase()
                                                        .contains(q))
                                                || (p.getProviderName() != null &&
                                                p.getProviderName().toLowerCase()
                                                        .contains(q))
                                                || (p.getVehicleReg() != null &&
                                                p.getVehicleReg().toLowerCase()
                                                        .contains(q)))
                                .collect(Collectors.toList())));
    }

    @FXML
    private void handleClearPolicy() {
        policySearchField.clear();
        policyStatusFilter.setValue(null);
        policyTable.setItems(
                FXCollections.observableArrayList(allPolicies));
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
    @FXML private void openPolice() {
        navigateToModule("/fxml/Police.fxml",
                "/styles/police.css", userLabel);
    }

    // ── HELPERS ───────────────────────────────────

    private void showStatus(Label label,
                            String msg, boolean ok) {
        label.setText(msg);
        label.setStyle(ok
                ? "-fx-text-fill: #44cc88;"
                : "-fx-text-fill: #e63946;");
        new Timeline(new KeyFrame(
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
        boolean isAdmin =
                "ADMIN".equals(currentUser.getRole());

        dashboardBtn.setVisible(isAdmin);
        dashboardBtn.setManaged(isAdmin);

        applyRoleBasedMenuBar(insuranceMenuBar);

        if ("INSURANCE".equals(currentUser.getRole())) {
            String provider = currentUser.getIdentifier();
            if (provider != null && !provider.isBlank()) {
                moduleTitleLabel.setText(
                        provider.toUpperCase());
                moduleSubLabel.setText(
                        "Your company's insurance policies");
            }
            deletePolicyBtn.setVisible(false);
            deletePolicyBtn.setManaged(false);
        }
    }


}