package com.vis.controller;

import com.vis.dao.*;
import com.vis.model.*;
import com.vis.util.DBConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CustomerController
        extends BaseModuleController implements Initializable {

    // ── TOP BAR ──────────────────────────────────
    @FXML private Label moduleTitleLabel;
    @FXML private Label moduleSubLabel;
    @FXML private Label userLabel;
    @FXML private Button dashboardBtn;

    // ── TAB 1 — CUSTOMER/PROFILE ──────────────────
    @FXML private Tab tab1;
    @FXML private HBox adminSearchBar;
    @FXML private HBox adminCustomerActions;
    @FXML private VBox adminCustomerForm;
    @FXML private VBox customerProfileCard;
    @FXML private VBox myVehiclesPanel;
    @FXML private VBox myVehiclesList;

    // Profile labels
    @FXML private Label profileName;
    @FXML private Label profilePhone;
    @FXML private Label profileEmail;
    @FXML private Label profileAddress;

    // Admin form fields
    @FXML private TextField customerSearchField;
    @FXML private TextField fieldName;
    @FXML private TextField fieldPhone;
    @FXML private TextField fieldEmail;
    @FXML private TextArea fieldAddress;
    @FXML private Button customerSaveBtn;
    @FXML private Label customerFormTitle;
    @FXML private Label customerFormStatus;
    @FXML private Label customerStatusLabel;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colAddress;

    // ── TAB 2 — QUERIES ──────────────────────────
    @FXML private ComboBox<Customer> queryCustomerFilter;
    @FXML private ComboBox<Customer> queryCustomerCombo;
    @FXML private ComboBox<Vehicle> queryVehicleCombo;
    @FXML private DatePicker queryDatePicker;
    @FXML private TextArea queryTextField;
    @FXML private TextArea responseTextField;
    @FXML private VBox responseArea;
    @FXML private TableView<CustomerQuery> queryTable;
    @FXML private TableColumn<CustomerQuery, String> colQCustomer;
    @FXML private TableColumn<CustomerQuery, String> colQVehicle;
    @FXML private TableColumn<CustomerQuery, String> colQDate;
    @FXML private TableColumn<CustomerQuery, String> colQStatus;
    @FXML private TableColumn<CustomerQuery, String> colQText;
    @FXML private Pagination queryPagination;
    @FXML private Label queryStatusLabel;
    @FXML private Label queryFormStatus;
    @FXML private Label queryFormTitle;
    @FXML private Button saveQueryBtn;
    @FXML private Button respondBtn;
    @FXML private Button deleteQueryBtn;

    // ── TAB 3 — MY SERVICES (Customer only) ──────
    @FXML private Tab myServicesTab;
    @FXML private TabPane customerTabs;
    @FXML private TableView<ServiceRecord> myServicesTable;
    @FXML private TableColumn<ServiceRecord, String> colSVehicle;
    @FXML private TableColumn<ServiceRecord, String> colSDate;
    @FXML private TableColumn<ServiceRecord, String> colSType;
    @FXML private TableColumn<ServiceRecord, String> colSDesc;
    @FXML private TableColumn<ServiceRecord, String> colSCost;

    // ── TAB 4 — MY INSURANCE (Customer only) ─────
    @FXML private Tab myInsuranceTab;
    @FXML private TableView<InsuranceRecord> myInsuranceTable;
    @FXML private TableColumn<InsuranceRecord, String> colIVehicle;
    @FXML private TableColumn<InsuranceRecord, String> colIProvider;
    @FXML private TableColumn<InsuranceRecord, String> colIPolicy;
    @FXML private TableColumn<InsuranceRecord, String> colIStart;
    @FXML private TableColumn<InsuranceRecord, String> colIEnd;
    @FXML private TableColumn<InsuranceRecord, String> colIStatus;

    @FXML private MenuBar customerMenuBar;
    @FXML private SplitPane customerSplitPane;

    @FXML private Tab myViolationsTab;
    @FXML private Tab myPoliceReportsTab;
    @FXML private TableView<Violation> myViolationsTable;
    @FXML private TableColumn<Violation, String> colVVehicle;
    @FXML private TableColumn<Violation, String> colVDate;
    @FXML private TableColumn<Violation, String> colVType;
    @FXML private TableColumn<Violation, String> colVFine;
    @FXML private TableColumn<Violation, String> colVStatus;
    @FXML private TableView<PoliceReport> myPoliceReportsTable;
    @FXML private TableColumn<PoliceReport, String> colPVehicle;
    @FXML private TableColumn<PoliceReport, String> colPDate;
    @FXML private TableColumn<PoliceReport, String> colPType;
    @FXML private TableColumn<PoliceReport, String> colPOfficer;
    @FXML private TableColumn<PoliceReport, String> colPDesc;

    private final PoliceDAO policeDAO = new PoliceDAO();

    // ── DAOs ─────────────────────────────────────
    private final CustomerDAO customerDAO       =
            new CustomerDAO();
    private final CustomerQueryDAO queryDAO     =
            new CustomerQueryDAO();
    private final VehicleDAO vehicleDAO         =
            new VehicleDAO();
    private final ServiceRecordDAO serviceDAO   =
            new ServiceRecordDAO();
    private final InsuranceDAO insuranceDAO     =
            new InsuranceDAO();

    private List<Customer> allCustomers;
    private List<CustomerQuery> allQueries;
    private boolean isEditMode = false;
    private Customer selectedCustomer = null;
    private static final int QUERIES_PER_PAGE = 8;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCustomerTableColumns();
        setupQueryTableColumns();
        setupServiceTableColumns();
        setupInsuranceTableColumns();
        setupViolationTableColumns();
        setupPoliceReportTableColumns();
    }

    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());
        setupForRole();
        loadAllDataAsync();
    }

    // ── ROLE SETUP ────────────────────────────────

    private void setupForRole() {
        boolean isCustomer =
                "CUSTOMER".equals(currentUser.getRole());
        boolean isAdmin =
                "ADMIN".equals(currentUser.getRole());

        // Dashboard button — admin only
        dashboardBtn.setVisible(isAdmin);
        dashboardBtn.setManaged(isAdmin);

        applyRoleBasedMenuBar(customerMenuBar);

        if (isCustomer) {
            setupCustomerPersonalView();
        } else {
            setupAdminView();
        }
    }

    private void setupCustomerPersonalView() {
        // Update header
        moduleTitleLabel.setText("MY VEHICLE PORTAL");
        moduleSubLabel.setText(
                "Your vehicles, services, insurance and queries");

        // Tab 1 — switch to profile view
        tab1.setText("My Profile");

        // Hide admin controls
        adminSearchBar.setVisible(false);
        adminSearchBar.setManaged(false);
        adminCustomerActions.setVisible(false);
        adminCustomerActions.setManaged(false);
        adminCustomerForm.setVisible(false);
        adminCustomerForm.setManaged(false);
        customerTable.setVisible(false);
        customerTable.setManaged(false);

        // Show customer profile card + vehicles
        customerProfileCard.setVisible(true);
        customerProfileCard.setManaged(true);
        myVehiclesPanel.setVisible(true);
        myVehiclesPanel.setManaged(true);

        // Show extra tabs
        myServicesTab.setDisable(false);
        myServicesTab.getStyleClass().remove("hidden-tab");

        myInsuranceTab.setDisable(false);
        myInsuranceTab.getStyleClass().remove("hidden-tab");

        // Query form — customer can submit, not respond
        queryFormTitle.setText("Submit a Query");
        respondBtn.setVisible(false);
        respondBtn.setManaged(false);
        saveQueryBtn.setText("SUBMIT QUERY");

        // Hide filter for customers
        // (they only see their own queries)
        queryCustomerFilter.setVisible(false);
        queryCustomerFilter.setManaged(false);
        deleteQueryBtn.setVisible(false);
        deleteQueryBtn.setManaged(false);

        // Show all customer tabs
        myServicesTab.setDisable(false);
        myInsuranceTab.setDisable(false);
        myViolationsTab.setDisable(false);
        myPoliceReportsTab.setDisable(false);

        if (myServicesTab != null) myServicesTab.setDisable(false);
        if (myInsuranceTab != null) myInsuranceTab.setDisable(false);
        if (myViolationsTab != null) myViolationsTab.setDisable(false);
        if (myPoliceReportsTab != null) myPoliceReportsTab.setDisable(false);
    }

    private void setupAdminView() {
        // Admin sees full management view
        moduleTitleLabel.setText("CUSTOMER MODULE");
        moduleSubLabel.setText(
                "Owner Information & Vehicle Queries");
        tab1.setText("Customers");

        // Safely remove customer-only tabs
        if (customerTabs != null) {
            customerTabs.getTabs().removeAll(
                    myServicesTab, myInsuranceTab,
                    myViolationsTab, myPoliceReportsTab);
        }


        // Show respond button for admin
        respondBtn.setVisible(true);
        respondBtn.setManaged(true);

        Platform.runLater(() -> {
            if (customerSplitPane != null) {
                customerSplitPane.setDividerPositions(0.55);
            }
        });

        // Make sure admin form is visible
        adminCustomerForm.setVisible(true);
        adminCustomerForm.setManaged(true);

        // Make sure customer-only panels are hidden
        customerProfileCard.setVisible(false);
        customerProfileCard.setManaged(false);
        myVehiclesPanel.setVisible(false);
        myVehiclesPanel.setManaged(false);
    }

    // ── ASYNC LOADING ─────────────────────────────

    private void loadAllDataAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String role = currentUser.getRole();

                if ("CUSTOMER".equals(role)) {
                    loadCustomerPersonalData();
                } else {
                    loadAdminData();
                }
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void loadCustomerPersonalData() {
        int cid = currentUser.getCustomerId();

        // Load customer's own record
        Customer me = customerDAO.getCustomerById(cid);

        // Load their vehicles
        List<Vehicle> myVehicles =
                customerDAO.getVehiclesByCustomerId(cid);

        // Load their queries
        List<CustomerQuery> myQueries =
                queryDAO.getQueriesByCustomer(cid);

        // Load their service records
        List<ServiceRecord> myServices =
                serviceDAO.getServicesByCustomerId(cid);

        // Load their insurance
        List<InsuranceRecord> myInsurance =
                insuranceDAO.getRecordsByCustomerId(cid);

        // NEW — violations and police reports
        List<Violation> myViolations =
                policeDAO.getViolationsByCustomerId(cid);
        List<PoliceReport> myReports =
                policeDAO.getReportsByCustomerId(cid);

        Platform.runLater(() -> {
            // Populate profile card
            if (me != null) {
                profileName.setText(me.getName());
                profilePhone.setText(me.getPhone() != null
                        ? me.getPhone() : "—");
                profileEmail.setText(me.getEmail() != null
                        ? me.getEmail() : "—");
                profileAddress.setText(
                        me.getAddress() != null
                                ? me.getAddress() : "—");


            }

            // NEW
            if (myViolationsTable != null)
                myViolationsTable.setItems(
                        FXCollections.observableArrayList(myViolations));
            if (myPoliceReportsTable != null)
                myPoliceReportsTable.setItems(
                        FXCollections.observableArrayList(myReports));

            // Populate vehicle cards
            buildVehicleCards(myVehicles);

            // Set query combos
            List<Customer> meList = new ArrayList<>();
            if (me != null) meList.add(me);
            queryCustomerCombo.setItems(
                    FXCollections.observableArrayList(meList));
            queryCustomerCombo.setValue(me);
            queryVehicleCombo.setItems(
                    FXCollections.observableArrayList(myVehicles));


            // Load queries
            allQueries = myQueries;
            setupQueryPagination();

            // Load services table
            myServicesTable.setItems(
                    FXCollections.observableArrayList(myServices));

            // Load insurance table
            myInsuranceTable.setItems(
                    FXCollections.observableArrayList(myInsurance));


        });
    }

    private void loadAdminData() {
        allCustomers = customerDAO.getAllCustomers();
        allQueries   = queryDAO.getAllQueries();
        List<Vehicle> vehicles =
                vehicleDAO.getAllVehiclesWithOwners();

        Platform.runLater(() -> {
            customerTable.setItems(
                    FXCollections.observableArrayList(
                            allCustomers));
            queryCustomerCombo.setItems(
                    FXCollections.observableArrayList(
                            allCustomers));
            queryCustomerFilter.setItems(
                    FXCollections.observableArrayList(
                            allCustomers));
            queryVehicleCombo.setItems(
                    FXCollections.observableArrayList(vehicles));
            setupQueryPagination();
        });
    }

    // Build visual vehicle cards for customer view
    private void buildVehicleCards(List<Vehicle> vehicles) {
        myVehiclesList.getChildren().clear();

        if (vehicles.isEmpty()) {
            Label none = new Label(
                    "No vehicles registered under your name.");
            none.setStyle("-fx-text-fill: #555577; " +
                    "-fx-font-size: 13px;");
            myVehiclesList.getChildren().add(none);
            return;
        }

        for (Vehicle v : vehicles) {
            VBox card = new VBox(8);
            card.getStyleClass().add("vehicle-mini-card");
            card.setStyle(
                    "-fx-background-color: #1a1a35;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #2a2a4a;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 14;");

            Label reg = new Label(
                    v.getRegistrationNumber());
            reg.setStyle(
                    "-fx-text-fill: #e63946;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;");

            Label details = new Label(
                    v.getYear() + " " + v.getMake()
                            + " " + v.getModel()
                            + "  |  " + v.getColor());
            details.setStyle(
                    "-fx-text-fill: #aaaacc;" +
                            "-fx-font-size: 13px;");

            card.getChildren().addAll(reg, details);
            myVehiclesList.getChildren().add(card);
        }
    }

    // ── TABLE COLUMN SETUP ────────────────────────

    private void setupCustomerTableColumns() {
        customerTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        colName.prefWidthProperty().bind(
                customerTable.widthProperty().multiply(0.25));
        colPhone.prefWidthProperty().bind(
                customerTable.widthProperty().multiply(0.20));
        colEmail.prefWidthProperty().bind(
                customerTable.widthProperty().multiply(0.28));
        colAddress.prefWidthProperty().bind(
                customerTable.widthProperty().multiply(0.24));

        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));
        colPhone.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPhone()));
        colEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        colAddress.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getAddress()));
    }

    private void setupQueryTableColumns() {
        queryTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        colQCustomer.prefWidthProperty().bind(
                queryTable.widthProperty().multiply(0.18));
        colQVehicle.prefWidthProperty().bind(
                queryTable.widthProperty().multiply(0.15));
        colQDate.prefWidthProperty().bind(
                queryTable.widthProperty().multiply(0.13));
        colQStatus.prefWidthProperty().bind(
                queryTable.widthProperty().multiply(0.13));
        colQText.prefWidthProperty().bind(
                queryTable.widthProperty().multiply(0.38));

        colQCustomer.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getCustomerName()));
        colQVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colQDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDate().toString()));
        colQText.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getQueryText()));
        colQStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle("Answered".equals(item)
                            ? "-fx-text-fill: #44cc88;" +
                            "-fx-font-weight: bold;"
                            : "-fx-text-fill: #e63946;" +
                            "-fx-font-weight: bold;");
                }
            }
        });
        colQStatus.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getResponseText() != null
                                && !d.getValue().getResponseText().isEmpty()
                                ? "Answered" : "Pending"));
    }

    private void setupServiceTableColumns() {
        if (myServicesTable == null) return;
        myServicesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
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

    private void setupInsuranceTableColumns() {
        if (myInsuranceTable == null) return;
        myInsuranceTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
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
        colIStatus.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getStatus()));

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
                                "-fx-text-fill: #44cc88;" +
                                        "-fx-font-weight: bold;";
                        case "Expired" ->
                                "-fx-text-fill: #ffaa00;" +
                                        "-fx-font-weight: bold;";
                        default ->
                                "-fx-text-fill: #e63946;" +
                                        "-fx-font-weight: bold;";
                    });
                }
            }
        });
    }

    // ── PAGINATION ────────────────────────────────

    private void setupQueryPagination() {
        if (allQueries == null || allQueries.isEmpty()) {
            queryPagination.setPageCount(1);
            queryTable.setItems(
                    FXCollections.emptyObservableList());
            return;
        }
        int pages = (int) Math.ceil(
                (double) allQueries.size() / QUERIES_PER_PAGE);
        queryPagination.setPageCount(Math.max(pages, 1));
        queryPagination.currentPageIndexProperty()
                .addListener((obs, o, n) ->
                        loadQueryPage(n.intValue()));
        loadQueryPage(0);
    }

    private void loadQueryPage(int page) {
        int from = page * QUERIES_PER_PAGE;
        int to   = Math.min(from + QUERIES_PER_PAGE,
                allQueries.size());
        queryTable.setItems(
                FXCollections.observableArrayList(
                        allQueries.subList(from, to)));
    }

    // ── CUSTOMER CRUD ─────────────────────────────

    @FXML private void handleAddCustomer() {
        isEditMode = false;
        selectedCustomer = null;
        customerFormTitle.setText("Add New Customer");
        customerSaveBtn.setText("SAVE CUSTOMER");
        clearCustomerForm();
    }

    @FXML private void handleEditCustomer() {
        Customer c = customerTable.getSelectionModel()
                .getSelectedItem();
        if (c == null) {
            showStatus(customerStatusLabel,
                    "⚠ Select a customer to edit.", false);
            return;
        }
        isEditMode = true;
        selectedCustomer = c;
        customerFormTitle.setText("Edit Customer");
        customerSaveBtn.setText("UPDATE CUSTOMER");
        fieldName.setText(c.getName());
        fieldPhone.setText(c.getPhone());
        fieldEmail.setText(c.getEmail());
        fieldAddress.setText(c.getAddress());
    }

    @FXML private void handleSaveCustomer() {
        if (fieldName.getText().isEmpty()
                || fieldPhone.getText().isEmpty()) {
            showStatus(customerFormStatus,
                    "⚠ Name and phone are required.", false);
            return;
        }
        Customer c = new Customer();
        c.setName(fieldName.getText().trim());
        c.setPhone(fieldPhone.getText().trim());
        c.setEmail(fieldEmail.getText().trim());
        c.setAddress(fieldAddress.getText().trim());

        boolean success;
        if (isEditMode && selectedCustomer != null) {
            c.setId(selectedCustomer.getId());
            success = customerDAO.updateCustomer(c);
        } else {
            success = customerDAO.addCustomer(c);
        }
        showStatus(customerFormStatus,
                success ? "✅ Customer saved."
                        : "❌ Failed to save.", success);
        if (success) {
            clearCustomerForm();
            loadAllDataAsync();
        }
    }

    @FXML private void handleDeleteCustomer() {
        Customer c = customerTable.getSelectionModel()
                .getSelectedItem();
        if (c == null) {
            showStatus(customerStatusLabel,
                    "⚠ Select a customer.", false);
            return;
        }
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + c.getName() + "?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = customerDAO.deleteCustomer(
                        c.getId());
                showStatus(customerStatusLabel,
                        ok ? "✅ Deleted." : "❌ Failed.", ok);
                if (ok) loadAllDataAsync();
            }
        });
    }

    @FXML private void handleCustomerSearch() {
        String q = customerSearchField.getText()
                .toLowerCase().trim();
        if (q.isEmpty()) {
            customerTable.setItems(
                    FXCollections.observableArrayList(
                            allCustomers));
            return;
        }
        customerTable.setItems(
                FXCollections.observableArrayList(
                        allCustomers.stream()
                                .filter(c ->
                                        c.getName().toLowerCase().contains(q)
                                                || (c.getPhone() != null &&
                                                c.getPhone().toLowerCase()
                                                        .contains(q))
                                                || (c.getEmail() != null &&
                                                c.getEmail().toLowerCase()
                                                        .contains(q)))
                                .collect(Collectors.toList())));
    }

    @FXML private void handleClearCustomer() {
        customerSearchField.clear();
        customerTable.setItems(
                FXCollections.observableArrayList(allCustomers));
        clearCustomerForm();
    }

    // ── QUERY CRUD ────────────────────────────────

    @FXML private void handleNewQuery() {
        queryTextField.clear();
        responseTextField.clear();
        queryDatePicker.setValue(LocalDate.now());
        if ("CUSTOMER".equals(currentUser.getRole())) {
            // Customer field already pre-set
        } else {
            queryCustomerCombo.setValue(null);
            queryVehicleCombo.setValue(null);
        }
    }

    @FXML private void handleQuerySelected() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) return;
        queryTextField.setText(q.getQueryText());
        responseTextField.setText(
                q.getResponseText() != null
                        ? q.getResponseText() : "");
        queryDatePicker.setValue(q.getDate());
    }

    @FXML private void handleSaveQuery() {
        if (queryVehicleCombo.getValue() == null
                || queryTextField.getText().isEmpty()) {
            showStatus(queryFormStatus,
                    "⚠ Vehicle and query text are required.",
                    false);
            return;
        }
        CustomerQuery q = new CustomerQuery();
        q.setCustomerId(
                queryCustomerCombo.getValue() != null
                        ? queryCustomerCombo.getValue().getId()
                        : currentUser.getCustomerId());
        q.setVehicleId(
                queryVehicleCombo.getValue().getVehicleId());
        q.setDate(queryDatePicker.getValue() != null
                ? queryDatePicker.getValue() : LocalDate.now());
        q.setQueryText(queryTextField.getText().trim());

        boolean success = queryDAO.addQuery(q);
        showStatus(queryFormStatus,
                success ? "✅ Query submitted successfully."
                        : "❌ Failed to submit.", success);
        if (success) {
            handleNewQuery();
            loadAllDataAsync();
        }
    }

    @FXML private void handleRespondQuery() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) {
            showStatus(queryFormStatus,
                    "⚠ Select a query to respond to.", false);
            return;
        }
        if (responseTextField.getText().isEmpty()) {
            showStatus(queryFormStatus,
                    "⚠ Enter a response.", false);
            return;
        }
        boolean success = queryDAO.respondToQuery(
                q.getQueryId(),
                responseTextField.getText().trim());
        showStatus(queryFormStatus,
                success ? "✅ Response saved."
                        : "❌ Failed.", success);
        if (success) loadAllDataAsync();
    }

    @FXML private void handleQueryFilter() {
        Customer selected = queryCustomerFilter.getValue();
        if (selected == null) return;
        allQueries = queryDAO.getQueriesByCustomer(
                selected.getId());
        setupQueryPagination();
    }

    @FXML private void handleClearQuery() {
        queryCustomerFilter.setValue(null);
        loadAllDataAsync();
    }

    @FXML private void handleDeleteQuery() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) {
            showStatus(queryStatusLabel,
                    "⚠ Select a query.", false);
            return;
        }
        boolean ok = queryDAO.deleteQuery(q.getQueryId());
        showStatus(queryStatusLabel,
                ok ? "✅ Deleted." : "❌ Failed.", ok);
        if (ok) loadAllDataAsync();
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML private void goToDashboard() {
        goToDashboard(userLabel);
    }
    @FXML private void handleRefresh() {
        loadAllDataAsync();
    }
    @FXML private void handleExit() {
        javafx.application.Platform.exit();
    }

    @FXML private void handleLogout() {
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
    @FXML private void openPolice() {
        navigateToModule("/fxml/Police.fxml",
                "/styles/police.css", userLabel);
    }
    @FXML private void openInsurance() {
        navigateToModule("/fxml/Insurance.fxml",
                "/styles/insurance.css", userLabel);
    }

    // ── HELPERS ───────────────────────────────────

    private void clearCustomerForm() {
        fieldName.clear();
        fieldPhone.clear();
        fieldEmail.clear();
        fieldAddress.clear();
        customerFormStatus.setText("");
        isEditMode = false;
        selectedCustomer = null;
    }

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
        java.net.URL page = getClass().getResource(css);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }

    private void setupViolationTableColumns() {
        if (myViolationsTable == null) return;
        myViolationsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
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
        colVStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle("Paid".equals(item)
                            ? "-fx-text-fill: #44cc88; -fx-font-weight: bold;"
                            : "-fx-text-fill: #e63946; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupPoliceReportTableColumns() {
        if (myPoliceReportsTable == null) return;
        myPoliceReportsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        colPVehicle.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getVehicleReg()));
        colPDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDate().toString()));
        colPType.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getReportType()));
        colPOfficer.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getOfficerName()));
        colPDesc.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDescription()));
        colPType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "Theft" ->
                                "-fx-text-fill: #e63946; -fx-font-weight: bold;";
                        case "Accident" ->
                                "-fx-text-fill: #ffaa00; -fx-font-weight: bold;";
                        default ->
                                "-fx-text-fill: #aaaacc;";
                    });
                }
            }
        });
    }
}