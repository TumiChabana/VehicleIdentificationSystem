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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml")
            );
            Scene scene = new Scene(loader.load());
            URL cssUrl = getClass().getResource("/styles/dashboard.css");
            if (cssUrl != null) {
                applyStyles(scene, "/styles/dashboard.css");
                System.out.println("✅ CSS loaded: " + cssUrl);
            } else {
                System.err.println("❌ CSS not found — check file location");
            }

            // Pass the logged-in user to the dashboard
            DashboardController controller = loader.getController();
            controller.initUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true); // ← Fullscreen on dashboard too

        } catch (Exception e) {
            showError("Failed to load dashboard.");
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void applyStyles(Scene scene, String pageCSS) {
        URL base = getClass().getResource("/styles/base.css");
        URL page = getClass().getResource(pageCSS);
        if (base != null) scene.getStylesheets().add(base.toExternalForm());
        if (page != null) scene.getStylesheets().add(page.toExternalForm());
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