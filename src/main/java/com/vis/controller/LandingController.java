package com.vis.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import java.net.URL;
import java.util.ResourceBundle;

public class LandingController implements Initializable {

    @FXML private Button getStartedBtn;
    @FXML private Button loginNavBtn;
    @FXML private HBox modulesSection;
    @FXML private ImageView bgImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playEntryAnimations();
        setupResponsiveBackground();
        playEntryAnimations();
    }

    // ── ANIMATIONS ────────────────────────────────

    private void playEntryAnimations() {
        // Fade in the whole page
        FadeTransition pageFade = new FadeTransition(
                Duration.millis(800), modulesSection);
        pageFade.setFromValue(0);
        pageFade.setToValue(1);
        pageFade.setDelay(Duration.millis(400));
        pageFade.play();

        // Slide up module cards
        TranslateTransition slideUp = new TranslateTransition(
                Duration.millis(700), modulesSection);
        slideUp.setFromY(40);
        slideUp.setToY(0);
        slideUp.setDelay(Duration.millis(400));
        slideUp.play();

        // Pulse the Access System button
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(getStartedBtn.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(getStartedBtn.opacityProperty(), 0.75))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setDelay(Duration.seconds(1));
        pulse.play();
    }

    // ── NAVIGATION ────────────────────────────────

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            applyStyles(scene, "/styles/login.css");

            Stage stage = (Stage) loginNavBtn.getScene().getWindow();

            // Fade transition between screens
            FadeTransition fade = new FadeTransition(
                    Duration.millis(400),
                    stage.getScene().getRoot());
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setMaximized(true);

                // Fade in new scene
                FadeTransition fadeIn = new FadeTransition(
                        Duration.millis(400), scene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fade.play();

        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    // Scroll placeholders — JavaFX desktop doesn't scroll
    // but we keep these for future enhancement
    @FXML private void scrollToTop()     {}
    @FXML private void scrollToModules() {}
    @FXML private void scrollToAbout()   {}
    @FXML private void scrollToContact() {}

    // ── STYLE HELPER ─────────────────────────────

    private void applyStyles(Scene scene, String pageCSS) {
        URL base = getClass().getResource("/styles/base.css");
        URL page = getClass().getResource(pageCSS);
        if (base != null)
            scene.getStylesheets().add(base.toExternalForm());
        if (page != null)
            scene.getStylesheets().add(page.toExternalForm());
    }


    private void setupResponsiveBackground() {
        // Make image always fill the screen
        bgImage.setPreserveRatio(false);

        // Bind image size to scene size when scene is available
        bgImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                bgImage.fitWidthProperty()
                        .bind(newScene.widthProperty());
                bgImage.fitHeightProperty()
                        .bind(newScene.heightProperty());
            }
        });
    }
}