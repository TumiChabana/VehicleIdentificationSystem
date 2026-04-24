package com.vis.controller;

import com.vis.dao.CustomerDAO;
import com.vis.dao.CustomerQueryDAO;
import com.vis.dao.VehicleDAO;
import com.vis.model.Customer;
import com.vis.model.CustomerQuery;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CustomerController
        extends BaseModuleController implements Initializable {

    // ── CUSTOMER TAB ─────────────────────────────
    @FXML private TextField customerSearchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private Label customerStatusLabel;
    @FXML private Label customerFormTitle;
    @FXML private Label customerFormStatus;
    @FXML private Label userLabel;

    @FXML private TextField fieldName;
    @FXML private TextField fieldPhone;
    @FXML private TextField fieldEmail;
    @FXML private TextArea fieldAddress;
    @FXML private Button customerSaveBtn;

    // ── QUERY TAB ────────────────────────────────
    @FXML private ComboBox<Customer> queryCustomerFilter;
    @FXML private ComboBox<Customer> queryCustomerCombo;
    @FXML private ComboBox<Vehicle> queryVehicleCombo;
    @FXML private DatePicker queryDatePicker;
    @FXML private TextArea queryTextField;
    @FXML private TextArea responseTextField;
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

    // ── DAOs ─────────────────────────────────────
    private final CustomerDAO customerDAO         =
            new CustomerDAO();
    private final CustomerQueryDAO queryDAO       =
            new CustomerQueryDAO();
    private final VehicleDAO vehicleDAO           =
            new VehicleDAO();

    private List<Customer> allCustomers;
    private List<CustomerQuery> allQueries;
    private boolean isEditMode = false;
    private Customer selectedCustomer = null;

    private static final int QUERIES_PER_PAGE = 8;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCustomerTableColumns();
        setupQueryTableColumns();
    }

    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());
        loadAllDataAsync();
    }

    // ── ASYNC LOADING ─────────────────────────────

    private void loadAllDataAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                allCustomers = customerDAO.getAllCustomers();
                allQueries   = queryDAO.getAllQueries();
                List<Vehicle> vehicles =
                        vehicleDAO.getAllVehiclesWithOwners();

                Platform.runLater(() -> {
                    // Customer tab
                    customerTable.setItems(
                            FXCollections.observableArrayList(
                                    allCustomers));

                    // Query combos
                    queryCustomerCombo.setItems(
                            FXCollections.observableArrayList(
                                    allCustomers));
                    queryCustomerFilter.setItems(
                            FXCollections.observableArrayList(
                                    allCustomers));
                    queryVehicleCombo.setItems(
                            FXCollections.observableArrayList(
                                    vehicles));

                    // Query table with pagination
                    setupQueryPagination();
                });
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── TABLE COLUMNS ─────────────────────────────

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
                new SimpleStringProperty(d.getValue().getAddress()));
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

        // Color code status
        colQStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Answered".equals(item)
                            ? "-fx-text-fill: #44cc88; " +
                            "-fx-font-weight: bold;"
                            : "-fx-text-fill: #e63946; " +
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

    // ── PAGINATION ────────────────────────────────

    private void setupQueryPagination() {
        if (allQueries == null || allQueries.isEmpty()) {
            queryPagination.setPageCount(1);
            queryTable.setItems(FXCollections.emptyObservableList());
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
        queryTable.setItems(FXCollections.observableArrayList(
                allQueries.subList(from, to)));
    }

    // ── CUSTOMER CRUD ─────────────────────────────

    @FXML
    private void handleAddCustomer() {
        isEditMode = false;
        selectedCustomer = null;
        customerFormTitle.setText("Add New Customer");
        customerSaveBtn.setText("SAVE CUSTOMER");
        clearCustomerForm();
    }

    @FXML
    private void handleEditCustomer() {
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

    @FXML
    private void handleSaveCustomer() {
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

        if (success) {
            showStatus(customerFormStatus,
                    "✅ Customer saved successfully.", true);
            clearCustomerForm();
            loadAllDataAsync();
        } else {
            showStatus(customerFormStatus,
                    "❌ Failed to save customer.", false);
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer c = customerTable.getSelectionModel()
                .getSelectedItem();
        if (c == null) {
            showStatus(customerStatusLabel,
                    "⚠ Select a customer to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + c.getName() + "?");
        confirm.setContentText(
                "This will also remove their vehicles and queries.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = customerDAO.deleteCustomer(
                        c.getId());
                showStatus(customerStatusLabel,
                        ok ? "✅ Customer deleted."
                                : "❌ Delete failed.", ok);
                if (ok) loadAllDataAsync();
            }
        });
    }

    @FXML
    private void handleCustomerSearch() {
        String q = customerSearchField.getText()
                .toLowerCase().trim();
        if (q.isEmpty()) {
            customerTable.setItems(
                    FXCollections.observableArrayList(allCustomers));
            return;
        }
        List<Customer> filtered = allCustomers.stream()
                .filter(c ->
                        c.getName().toLowerCase().contains(q)
                                || (c.getPhone() != null
                                && c.getPhone().toLowerCase().contains(q))
                                || (c.getEmail() != null
                                && c.getEmail().toLowerCase().contains(q)))
                .collect(Collectors.toList());
        customerTable.setItems(
                FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearCustomer() {
        customerSearchField.clear();
        customerTable.setItems(
                FXCollections.observableArrayList(allCustomers));
        clearCustomerForm();
    }

    // ── QUERY CRUD ────────────────────────────────

    @FXML
    private void handleNewQuery() {
        queryFormTitle.setText("New Query");
        queryCustomerCombo.setValue(null);
        queryVehicleCombo.setValue(null);
        queryDatePicker.setValue(LocalDate.now());
        queryTextField.clear();
        responseTextField.clear();
    }

    @FXML
    private void handleQuerySelected() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) return;

        queryTextField.setText(q.getQueryText());
        responseTextField.setText(
                q.getResponseText() != null
                        ? q.getResponseText() : "");
        queryDatePicker.setValue(q.getDate());
        queryFormTitle.setText("Query #" + q.getQueryId());
    }

    @FXML
    private void handleSaveQuery() {
        if (queryCustomerCombo.getValue() == null
                || queryVehicleCombo.getValue() == null
                || queryTextField.getText().isEmpty()) {
            showStatus(queryFormStatus,
                    "⚠ Customer, vehicle and query are required.",
                    false);
            return;
        }

        CustomerQuery q = new CustomerQuery();
        q.setCustomerId(
                queryCustomerCombo.getValue().getId());
        q.setVehicleId(
                queryVehicleCombo.getValue().getVehicleId());
        q.setDate(queryDatePicker.getValue() != null
                ? queryDatePicker.getValue() : LocalDate.now());
        q.setQueryText(queryTextField.getText().trim());

        boolean success = queryDAO.addQuery(q);
        showStatus(queryFormStatus,
                success ? "✅ Query saved." : "❌ Failed.",
                success);
        if (success) {
            handleNewQuery();
            loadAllDataAsync();
        }
    }

    @FXML
    private void handleRespondQuery() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) {
            showStatus(queryFormStatus,
                    "⚠ Select a query to respond to.", false);
            return;
        }
        if (responseTextField.getText().isEmpty()) {
            showStatus(queryFormStatus,
                    "⚠ Enter a response first.", false);
            return;
        }

        boolean success = queryDAO.respondToQuery(
                q.getQueryId(),
                responseTextField.getText().trim());
        showStatus(queryFormStatus,
                success ? "✅ Response saved."
                        : "❌ Failed to save response.",
                success);
        if (success) loadAllDataAsync();
    }

    @FXML
    private void handleQueryFilter() {
        Customer selected = queryCustomerFilter.getValue();
        if (selected == null) return;

        allQueries = queryDAO.getQueriesByCustomer(
                selected.getId());
        setupQueryPagination();
    }

    @FXML
    private void handleClearQuery() {
        queryCustomerFilter.setValue(null);
        loadAllDataAsync();
    }

    @FXML
    private void handleDeleteQuery() {
        CustomerQuery q = queryTable.getSelectionModel()
                .getSelectedItem();
        if (q == null) {
            showStatus(queryStatusLabel,
                    "⚠ Select a query to delete.", false);
            return;
        }
        boolean ok = queryDAO.deleteQuery(q.getQueryId());
        showStatus(queryStatusLabel,
                ok ? "✅ Query deleted." : "❌ Delete failed.", ok);
        if (ok) loadAllDataAsync();
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML private void goToDashboard() {
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
            Stage stage =
                    (Stage) userLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
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
        java.net.URL page =
                getClass().getResource(css);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }
}