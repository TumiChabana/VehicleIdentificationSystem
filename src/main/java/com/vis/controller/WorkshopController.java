package com.vis.controller;

import com.vis.dao.CustomerDAO;
import com.vis.dao.VehicleDAO;
import com.vis.dao.ServiceRecordDAO;
import com.vis.model.Customer;
import com.vis.model.ServiceRecord;
import com.vis.model.Vehicle;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WorkshopController
        extends BaseModuleController implements Initializable {

    // ── VEHICLE TAB ──────────────────────────────
    @FXML private TextField vehicleSearchField;
    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> colReg;
    @FXML private TableColumn<Vehicle, String> colMake;
    @FXML private TableColumn<Vehicle, String> colModel;
    @FXML private TableColumn<Vehicle, String> colYear;
    @FXML private TableColumn<Vehicle, String> colColor;
    @FXML private TableColumn<Vehicle, String> colOwner;
    @FXML private Label vehicleStatusLabel;
    @FXML private Label vehicleFormTitle;
    @FXML private Label vehicleFormStatus;
    @FXML private Label userLabel;

    // Vehicle form fields
    @FXML private TextField fieldReg;
    @FXML private TextField fieldMake;
    @FXML private TextField fieldModel;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldColor;
    @FXML private ComboBox<Customer> ownerComboBox;
    @FXML private Button vehicleSaveBtn;

    // ── SERVICE TAB ──────────────────────────────
    @FXML private ComboBox<Vehicle> serviceVehicleFilter;
    @FXML private ComboBox<Vehicle> serviceVehicleCombo;
    @FXML private ComboBox<String> serviceTypeCombo;
    @FXML private DatePicker serviceDatePicker;
    @FXML private TextArea serviceDescField;
    @FXML private TextField serviceCostField;
    @FXML private TableView<ServiceRecord> serviceTable;
    @FXML private TableColumn<ServiceRecord, String> colSVehicle;
    @FXML private TableColumn<ServiceRecord, String> colSDate;
    @FXML private TableColumn<ServiceRecord, String> colSType;
    @FXML private TableColumn<ServiceRecord, String> colSDesc;
    @FXML private TableColumn<ServiceRecord, String> colSCost;
    @FXML private Label serviceStatusLabel;
    @FXML private Label serviceFormStatus;

    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;

    @FXML private Label moduleTitleLabel;
    @FXML private Label moduleSubLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button deleteVehicleBtn;
    @FXML private MenuBar workshopMenuBar;

    // ── DAOs ─────────────────────────────────────
    private final VehicleDAO vehicleDAO           = new VehicleDAO();
    private final CustomerDAO customerDAO         = new CustomerDAO();
    private final ServiceRecordDAO serviceDAO     = new ServiceRecordDAO();

    private List<Vehicle> allVehicles;
    private boolean isEditMode = false;
    private Vehicle selectedVehicle = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupVehicleTableColumns();
        setupServiceTableColumns();
        setupServiceTypeOptions();
    }



    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
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

                // ADMIN sees everything
                // WORKSHOP sees everything (they service all)
                // CUSTOMER sees only their own vehicles
                if ("CUSTOMER".equals(role)) {
                    allVehicles = customerDAO
                            .getVehiclesByCustomerId(
                                    currentUser.getCustomerId());
                } else {
                    allVehicles =
                            vehicleDAO.getAllVehiclesWithOwners();
                }

                List<Customer> customers =
                        customerDAO.getAllCustomers();

                List<ServiceRecord> records;
                if ("CUSTOMER".equals(role)) {
                    records = serviceDAO.getServicesByCustomerId(
                            currentUser.getCustomerId());
                } else {
                    records = serviceDAO.getAllServiceRecords();
                }

                List<Customer> finalCustomers = customers;
                List<ServiceRecord> finalRecords = records;

                Platform.runLater(() -> {
                    vehicleTable.setItems(
                            FXCollections.observableArrayList(
                                    allVehicles));

                    // Customers only for non-customer roles
                    if (!"CUSTOMER".equals(role)) {
                        ownerComboBox.setItems(
                                FXCollections.observableArrayList(
                                        finalCustomers));
                    }

                    serviceVehicleCombo.setItems(
                            FXCollections.observableArrayList(
                                    allVehicles));
                    serviceVehicleFilter.setItems(
                            FXCollections.observableArrayList(
                                    allVehicles));
                    serviceTable.setItems(
                            FXCollections.observableArrayList(
                                    finalRecords));

                    // Hide add/edit/delete for customers
                    // (read-only view)
                    applyRoleRestrictions();
                });
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void applyRoleRestrictions() {
        boolean isCustomer =
                "CUSTOMER".equals(currentUser.getRole());

        // Customer gets read-only workshop view
        vehicleSaveBtn.setVisible(!isCustomer);
        vehicleSaveBtn.setManaged(!isCustomer);
        vehicleFormTitle.setText(isCustomer
                ? "Your Vehicles" : "Add New Vehicle");
    }

    // ── TABLE COLUMN SETUP ────────────────────────

    private void setupVehicleTableColumns() {
        vehicleTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colReg.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.18));
        colMake.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.17));
        colModel.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.17));
        colYear.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.10));
        colColor.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.13));
        colOwner.prefWidthProperty().bind(
                vehicleTable.widthProperty().multiply(0.22));

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
        colOwner.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getOwnerName() != null
                                ? d.getValue().getOwnerName() : "—"));
    }

    private void setupServiceTableColumns() {
        serviceTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colSVehicle.prefWidthProperty().bind(
                serviceTable.widthProperty().multiply(0.18));
        colSDate.prefWidthProperty().bind(
                serviceTable.widthProperty().multiply(0.15));
        colSType.prefWidthProperty().bind(
                serviceTable.widthProperty().multiply(0.20));
        colSDesc.prefWidthProperty().bind(
                serviceTable.widthProperty().multiply(0.30));
        colSCost.prefWidthProperty().bind(
                serviceTable.widthProperty().multiply(0.14));

        colSVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colSDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDate().toString()));
        colSType.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getServiceType()));
        colSDesc.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDescription()));
        colSCost.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "M " + d.getValue().getCost()));
    }

    private void setupServiceTypeOptions() {
        serviceTypeCombo.setItems(
                FXCollections.observableArrayList(
                        "Oil Change", "Full Service",
                        "Brake Service", "Tyre Rotation",
                        "Engine Check", "Transmission Service",
                        "Battery Replacement", "Air Filter",
                        "Wheel Alignment", "Other"
                ));
    }

    // ── VEHICLE CRUD ──────────────────────────────

    @FXML
    private void handleAddVehicle() {
        isEditMode = false;
        selectedVehicle = null;
        vehicleFormTitle.setText("Add New Vehicle");
        vehicleSaveBtn.setText("SAVE VEHICLE");
        clearVehicleForm();
    }

    @FXML
    private void handleEditVehicle() {
        Vehicle v = vehicleTable.getSelectionModel()
                .getSelectedItem();
        if (v == null) {
            showStatus(vehicleStatusLabel,
                    "Please select a vehicle to edit.", false);
            return;
        }
        isEditMode = true;
        selectedVehicle = v;
        vehicleFormTitle.setText("Edit Vehicle");
        vehicleSaveBtn.setText("UPDATE VEHICLE");

        // Populate form
        fieldReg.setText(v.getRegistrationNumber());
        fieldMake.setText(v.getMake());
        fieldModel.setText(v.getModel());
        fieldYear.setText(String.valueOf(v.getYear()));
        fieldColor.setText(v.getColor());
    }

    @FXML
    private void handleSaveVehicle() {
        // Validate
        if (fieldReg.getText().isEmpty()
                || fieldMake.getText().isEmpty()
                || fieldModel.getText().isEmpty()
                || fieldYear.getText().isEmpty()) {
            showStatus(vehicleFormStatus,
                    "Please fill in all required fields.", false);
            return;
        }

        try {
            Vehicle v = new Vehicle();
            v.setRegistrationNumber(
                    fieldReg.getText().trim().toUpperCase());
            v.setMake(fieldMake.getText().trim());
            v.setModel(fieldModel.getText().trim());
            v.setYear(Integer.parseInt(
                    fieldYear.getText().trim()));
            v.setColor(fieldColor.getText().trim());


            Customer owner = ownerComboBox.getValue();
            if (owner != null) {
                v.setOwnerId(owner.getId());
            }

            // Save image path if one was selected
            if (imagePathField.getText() != null
                    && !imagePathField.getText().isBlank()) {
                v.setImagePath(imagePathField.getText());
            }


            boolean success;
            if (isEditMode && selectedVehicle != null) {
                v.setVehicleId(selectedVehicle.getVehicleId());
                success = vehicleDAO.updateVehicle(v);
            } else {
                success = vehicleDAO.addVehicle(v);
            }

            if (success) {
                showStatus(vehicleFormStatus,
                        "Vehicle saved successfully.", true);
                clearVehicleForm();
                loadAllDataAsync();
            } else {
                showStatus(vehicleFormStatus,
                        "Failed to save vehicle.", false);
            }

        } catch (NumberFormatException e) {
            showStatus(vehicleFormStatus,
                    "Year must be a number.", false);
        }


    }

    @FXML
    private void handleDeleteVehicle() {
        Vehicle v = vehicleTable.getSelectionModel()
                .getSelectedItem();
        if (v == null) {
            showStatus(vehicleStatusLabel,
                    "Please select a vehicle to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(
                "Delete " + v.getRegistrationNumber() + "?");
        confirm.setContentText(
                "This will also delete all service records, "
                        + "police reports and violations for this vehicle.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = vehicleDAO.deleteVehicle(
                        v.getVehicleId());
                if (success) {
                    showStatus(vehicleStatusLabel,
                            "Vehicle deleted.", true);
                    loadAllDataAsync();
                } else {
                    showStatus(vehicleStatusLabel,
                            "Delete failed.", false);
                }
            }
        });
    }

    @FXML
    private void handleVehicleSearch() {
        String query = vehicleSearchField.getText()
                .toLowerCase().trim();
        if (query.isEmpty()) {
            vehicleTable.setItems(
                    FXCollections.observableArrayList(allVehicles));
            return;
        }
        List<Vehicle> filtered = allVehicles.stream()
                .filter(v ->
                        v.getRegistrationNumber()
                                .toLowerCase().contains(query)
                                || v.getMake().toLowerCase().contains(query)
                                || v.getModel().toLowerCase().contains(query))
                .collect(Collectors.toList());
        vehicleTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearVehicle() {
        vehicleSearchField.clear();
        vehicleTable.setItems(
                FXCollections.observableArrayList(allVehicles));
        clearVehicleForm();
    }

    // ── SERVICE CRUD ──────────────────────────────

    @FXML
    private void handleAddService() {
        serviceVehicleCombo.setValue(null);
        serviceDatePicker.setValue(LocalDate.now());
        serviceTypeCombo.setValue(null);
        serviceDescField.clear();
        serviceCostField.clear();
    }

    @FXML
    private void handleSaveService() {
        if (serviceVehicleCombo.getValue() == null
                || serviceDatePicker.getValue() == null
                || serviceTypeCombo.getValue() == null
                || serviceCostField.getText().isEmpty()) {
            showStatus(serviceFormStatus,
                    "Please fill in all required fields.", false);
            return;
        }

        try {
            ServiceRecord sr = new ServiceRecord();
            sr.setVehicleId(serviceVehicleCombo.getValue()
                    .getVehicleId());
            sr.setDate(serviceDatePicker.getValue());
            sr.setServiceType(serviceTypeCombo.getValue());
            sr.setDescription(serviceDescField.getText().trim());
            sr.setCost(Double.parseDouble(
                    serviceCostField.getText().trim()));

            boolean success = serviceDAO.addServiceRecord(sr);
            if (success) {
                showStatus(serviceFormStatus,
                        "Service record saved.", true);
                handleAddService(); // reset form
                loadAllDataAsync();
            } else {
                showStatus(serviceFormStatus,
                        "Failed to save record.", false);
            }

        } catch (NumberFormatException e) {
            showStatus(serviceFormStatus,
                    "Cost must be a number.", false);
        }
    }

    @FXML
    private void handleDeleteService() {
        ServiceRecord sr = serviceTable.getSelectionModel()
                .getSelectedItem();
        if (sr == null) {
            showStatus(serviceStatusLabel,
                    "Please select a record to delete.", false);
            return;
        }
        boolean success = serviceDAO.deleteServiceRecord(
                sr.getRecordId());
        if (success) {
            showStatus(serviceStatusLabel,
                    "Record deleted.", true);
            loadAllDataAsync();
        } else {
            showStatus(serviceStatusLabel,
                    "Delete failed.", false);
        }
    }

    @FXML
    private void handleServiceFilter() {
        Vehicle selected = serviceVehicleFilter.getValue();
        if (selected == null) return;

        Task<List<ServiceRecord>> task = new Task<>() {
            @Override
            protected List<ServiceRecord> call() {
                return serviceDAO.getServicesByVehicle(
                        selected.getVehicleId());
            }
        };
        task.setOnSucceeded(e ->
                serviceTable.setItems(
                        FXCollections.observableArrayList(
                                task.getValue())));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleClearService() {
        serviceVehicleFilter.setValue(null);
        loadAllDataAsync();
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML
    private void goToDashboard() {
        goToDashboard(userLabel);
    }

    @FXML private void handleRefresh() { loadAllDataAsync(); }

    @FXML private void handleExit() { Platform.exit(); }

    @FXML
    private void handleLogout() {
        try {
            DBConnection.closeConnection();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, "/styles/login.css");
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(scene);
            BaseModuleController.applyStageDefaults(stage);
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    @FXML private void openCustomer() {
        navigateToModule("/fxml/Customer.fxml",
                "/styles/customer.css", userLabel);
    }
    @FXML private void openPolice() {
        navigateToModule("/fxml/Police.fxml",
                "/styles/police.css", userLabel);
    }
    @FXML private void openInsurance() {
        navigateToModule("/fxml/Insurance.fxml",
                "/styles/insurance.css", userLabel);
    }

    // ── HELPERS ───────────────────────────────────

    private void clearVehicleForm() {
        fieldReg.clear();
        fieldMake.clear();
        fieldModel.clear();
        fieldYear.clear();
        fieldColor.clear();
        ownerComboBox.setValue(null);
        vehicleFormStatus.setText("");
        isEditMode = false;
        selectedVehicle = null;
    }

    private void showStatus(Label label,
                            String message,
                            boolean success) {
        label.setText(message);
        label.setStyle(success
                ? "-fx-text-fill: #44cc88;"
                : "-fx-text-fill: #e63946;");

        // Auto-clear after 4 seconds
        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        Duration.seconds(4),
                        e -> label.setText("")
                )
        ).play();
    }



    private void applyStyles(Scene scene, String pageCSS) {
        java.net.URL base =
                getClass().getResource("/styles/base.css");
        java.net.URL page =
                getClass().getResource(pageCSS);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }



    @FXML
    private void handleImageBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Vehicle Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        File file = chooser.showOpenDialog(
                imagePathField.getScene().getWindow());

        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
            Image preview = new Image("file:" + file.getAbsolutePath(),
                    240, 140, true, true);
            imagePreview.setImage(preview);
            imagePreview.setVisible(true);
        }
    }

    private void setupForRole() {
        boolean isAdmin =
                "ADMIN".equals(currentUser.getRole());
        boolean isWorkshop =
                "WORKSHOP".equals(currentUser.getRole());

        // Dashboard button — admin only
        dashboardBtn.setVisible(isAdmin);
        dashboardBtn.setManaged(isAdmin);

        applyRoleBasedMenuBar(workshopMenuBar);

        if (isWorkshop) {
            // Personalize header
            moduleTitleLabel.setText("WORKSHOP DASHBOARD");
            moduleSubLabel.setText(
                    "Manage vehicle services and registrations");

            // Workshop staff cannot delete vehicles
            // — that is an admin action
            deleteVehicleBtn.setVisible(false);
            deleteVehicleBtn.setManaged(false);

            // But they CAN add vehicles and service records
            // All their core work is still available
        }

        if (isAdmin) {
            moduleTitleLabel.setText("WORKSHOP MODULE");
            moduleSubLabel.setText(
                    "Vehicle Registration & Service Records");

            // Admin sees everything including delete
            deleteVehicleBtn.setVisible(true);
            deleteVehicleBtn.setManaged(true);
        }
    }

}