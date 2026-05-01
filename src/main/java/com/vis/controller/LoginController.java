package com.vis.controller;

import com.vis.dao.UserDAO;
import com.vis.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupButtonEffects();
        setupFadeTransition();
        // Allow Enter key to trigger login
        passwordField.setOnAction(e -> handleLogin());
    }

    // ── VISUAL EFFECTS ───────────────────────────

    private void setupButtonEffects() {
        // DropShadow effect on login button (7 marks)
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#e63946"));
        shadow.setRadius(20);
        shadow.setSpread(0.3);
        loginButton.setEffect(shadow);
    }

    private void setupFadeTransition() {
        // FadeTransition — button continuously fades in and out (7 marks)
        FadeTransition fade = new FadeTransition(
                Duration.seconds(1.5), loginButton
        );
        fade.setFromValue(1.0);
        fade.setToValue(0.6);
        fade.setAutoReverse(true);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.play();
    }

    // ── LOGIN LOGIC ──────────────────────────────

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.login(username, password);

            if (user != null) {
                navigateToDashboard(user);
            } else {
                showError("Invalid username or password.");
                passwordField.clear();
                shakeError();
            }

        } catch (Exception e) {
            showError("Connection error. Please try again.");
            System.err.println("Login error: " + e.getMessage());
        }
    }

    // ── NAVIGATION ───────────────────────────────

    private void navigateToDashboard(User user) {
        try {
            String fxmlPath;
            String cssPath;

            // Route based on role
            switch (user.getRole()) {
                case "ADMIN" -> {
                    fxmlPath = "/fxml/Dashboard.fxml";
                    cssPath  = "/styles/dashboard.css";
                }
                case "WORKSHOP" -> {
                    fxmlPath = "/fxml/Workshop.fxml";
                    cssPath  = "/styles/workshop.css";
                }
                case "CUSTOMER" -> {
                    fxmlPath = "/fxml/Customer.fxml";
                    cssPath  = "/styles/customer.css";
                }
                case "POLICE" -> {
                    fxmlPath = "/fxml/Police.fxml";
                    cssPath  = "/styles/police.css";
                }
                case "INSURANCE" -> {
                    fxmlPath = "/fxml/Insurance.fxml";
                    cssPath  = "/styles/insurance.css";
                }
                default -> {
                    showError("Unknown role: " + user.getRole());
                    return;
                }
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, cssPath);

            // Pass user to controller
            Object controller = loader.getController();
            if (controller instanceof DashboardController dc) {
                dc.initUser(user);
            } else if (controller instanceof BaseModuleController bmc) {
                bmc.initUser(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            BaseModuleController.applyStageDefaults(stage);

        } catch (Exception e) {
            showError("Failed to load screen.");
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLanding() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Landing.fxml"));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, "/styles/landing.css");
            Stage stage = (Stage) loginButton.getScene().getWindow();

            FadeTransition fade = new FadeTransition(
                    Duration.millis(300),
                    stage.getScene().getRoot());
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                stage.setScene(scene);
                      // ← Opens fullscreen
                stage.setMinWidth(1000);
                stage.setMinHeight(650);
            });
            fade.play();

        } catch (Exception e) {
            System.err.println("Landing error: " + e.getMessage());
        }
    }

    // Add this helper
    private void applyStyles(Scene scene, String pageCSS) {
        URL base = getClass().getResource("/styles/base.css");
        URL page = getClass().getResource(pageCSS);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }

    // ── UI HELPERS ───────────────────────────────

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void shakeError() {
        // Small shake animation on wrong password
        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0),
                        e -> errorLabel.setTranslateX(0)),
                new KeyFrame(Duration.millis(60),
                        e -> errorLabel.setTranslateX(10)),
                new KeyFrame(Duration.millis(120),
                        e -> errorLabel.setTranslateX(-10)),
                new KeyFrame(Duration.millis(180),
                        e -> errorLabel.setTranslateX(6)),
                new KeyFrame(Duration.millis(240),
                        e -> errorLabel.setTranslateX(0))
        );
        shake.play();
    }



}