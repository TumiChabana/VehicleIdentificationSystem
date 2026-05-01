package com.vis.controller;

import com.vis.dao.CustomerDAO;
import com.vis.dao.UserDAO;
import com.vis.model.Customer;
import com.vis.model.User;
import com.vis.util.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController
        extends BaseModuleController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colCreated;

    @FXML private TextField fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label formStatus;
    @FXML private Label userLabel;
    @FXML private Button dashboardBtn;
    @FXML private Label customerLinkLabel;
    @FXML private ComboBox<Customer> customerLinkCombo;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupRoleOptions();
    }

    @Override
    protected void onUserLoaded() {
        userLabel.setText(currentUser.getRole());

        // Only ADMIN should ever reach this screen
        boolean isAdmin = "ADMIN".equals(
                currentUser.getRole());
        if (dashboardBtn != null) {
            dashboardBtn.setVisible(isAdmin);
            dashboardBtn.setManaged(isAdmin);
        }
        loadUsersAsync();
    }

    // ── ASYNC LOADING ─────────────────────────────

    private void loadUsersAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return userDAO.getAllUsers();
            }
        };
        task.setOnSucceeded(e ->
                userTable.setItems(
                        FXCollections.observableArrayList(
                                task.getValue())));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── TABLE COLUMNS ─────────────────────────────

    private void setupTableColumns() {
        userTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        colUsername.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.35));
        colRole.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.30));
        colCreated.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.32));

        colUsername.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getUsername()));
        colRole.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getRole()));
        colCreated.setCellValueFactory(d ->
                new SimpleStringProperty("—"));

        // Color code roles
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item,
                                      boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "ADMIN" ->
                                "-fx-text-fill: #e63946; " +
                                        "-fx-font-weight: bold;";
                        case "POLICE" ->
                                "-fx-text-fill: #4488ff; " +
                                        "-fx-font-weight: bold;";
                        case "WORKSHOP" ->
                                "-fx-text-fill: #ffaa00; " +
                                        "-fx-font-weight: bold;";
                        case "INSURANCE" ->
                                "-fx-text-fill: #aa88ff; " +
                                        "-fx-font-weight: bold;";
                        case "CUSTOMER" ->
                                "-fx-text-fill: #44cc88; " +
                                        "-fx-font-weight: bold;";
                        default ->
                                "-fx-text-fill: #aaaacc;";
                    });
                }
            }
        });
    }

    private void setupRoleOptions() {
        if (roleCombo != null) {
            roleCombo.setItems(
                    FXCollections.observableArrayList(
                            "ADMIN", "WORKSHOP", "CUSTOMER",
                            "POLICE", "INSURANCE"));
        }
    }

    // ── USER MANAGEMENT ───────────────────────────

    @FXML
    private void handleAddUser() {
        if (fieldUsername.getText().isEmpty()
                || fieldPassword.getText().isEmpty()
                || roleCombo.getValue() == null) {
            showStatus("All fields are required.", false);
            return;
        }

        boolean success = UserDAO.register(
                fieldUsername.getText().trim(),
                fieldPassword.getText().trim(),
                roleCombo.getValue());

        // If customer role, link to customer record
        if (success && "CUSTOMER".equals(roleCombo.getValue())
                && customerLinkCombo.getValue() != null) {
            UserDAO.linkCustomer(
                    fieldUsername.getText().trim(),
                    customerLinkCombo.getValue().getId());
        }

        showStatus(
                success ? "User created successfully."
                        : "Failed - username may exist.",
                success);

        if (success) {
            fieldUsername.clear();
            fieldPassword.clear();
            roleCombo.setValue(null);
            customerLinkCombo.setValue(null);
            loadUsersAsync();
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            showStatus("Select a user to delete.", false);
            return;
        }
        if ("ADMIN".equals(selected.getRole())) {
            showStatus("Cannot delete admin account.",
                    false);
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(
                "Delete user: " + selected.getUsername() + "?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean ok = userDAO.deleteUser(
                        selected.getUserId());
                showStatus(
                        ok ? "User deleted."
                                : "Delete failed.", ok);
                if (ok) loadUsersAsync();
            }
        });
    }

    @FXML
    private void handleRefresh() { loadUsersAsync(); }

    @FXML
    private void goToDashboard() {
        goToDashboard(userLabel);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
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

    private void showStatus(String msg, boolean ok) {
        formStatus.setText(msg);
        formStatus.setStyle(ok
                ? "-fx-text-fill: #44cc88;"
                : "-fx-text-fill: #e63946;");
        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        Duration.seconds(4),
                        e -> formStatus.setText("")
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

    @FXML
    private void handleRoleChange() {
        boolean isCustomer = "CUSTOMER".equals(
                roleCombo.getValue());
        customerLinkLabel.setVisible(isCustomer);
        customerLinkLabel.setManaged(isCustomer);
        customerLinkCombo.setVisible(isCustomer);
        customerLinkCombo.setManaged(isCustomer);

        if (isCustomer && customerLinkCombo.getItems().isEmpty()) {
            customerLinkCombo.setItems(
                    FXCollections.observableArrayList(
                            customerDAO.getAllCustomers()));
        }
    }
}