package com.vis.controller;

import com.vis.model.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public abstract class BaseModuleController {

    protected User currentUser;

    // Every module receives the logged-in user
    public void initUser(User user) {
        this.currentUser = user;
        onUserLoaded();
    }



    // Each module overrides this to load its data
    protected abstract void onUserLoaded();

    public static void applyStageDefaults(Stage stage) {
        stage.setFullScreen(false);
        stage.setMaximized(false);   // ← reset first

        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        Platform.runLater(() -> stage.setMaximized(true));
    }

    // Shared navigation back to dashboard
    protected void goToDashboard(Label anyLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            applyStylesheet(scene, "/styles/base.css");
            applyStylesheet(scene, "/styles/dashboard.css");

            DashboardController dc = loader.getController();
            dc.initUser(currentUser);

            Stage stage = (Stage) anyLabel.getScene().getWindow();
            smoothTransition(stage, scene);

        } catch (Exception e) {
            System.err.println("Dashboard nav error: " + e.getMessage());
        }
    }


    protected void navigateToModule(String fxmlPath,
                                    String cssPath,
                                    Label anyLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            applyStylesheet(scene, "/styles/base.css");
            applyStylesheet(scene, cssPath);

            Object controller = loader.getController();
            if (controller instanceof BaseModuleController bmc) {
                bmc.initUser(currentUser);
            }

            Stage stage = (Stage) anyLabel.getScene().getWindow();
            smoothTransition(stage, scene);

        } catch (Exception e) {
            System.err.println("Module nav error: " + e.getMessage());
        }
    }

    // ── SHARED TRANSITION — used by every navigation ──
// This is the single method all navigation calls.
// Fade out → swap scene → maximize → fade in.
    public static void smoothTransition(Stage stage, Scene newScene) {
        javafx.animation.FadeTransition fadeOut =
                new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(200),
                        stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            stage.setFullScreen(false);
            stage.setMaximized(false);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            Platform.runLater(() ->
                    Platform.runLater(() -> {
                        stage.setMaximized(true);
                        javafx.animation.FadeTransition fadeIn =
                                new javafx.animation.FadeTransition(
                                        javafx.util.Duration.millis(200),
                                        newScene.getRoot());
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }));
        });
        fadeOut.play();
    }

    private void applyStylesheet(Scene scene, String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null)
            scene.getStylesheets().add(url.toExternalForm());
    }

    protected void applyRoleBasedMenus(MenuBar menuBar,
                                       String currentModule) {
        if (currentUser == null) return;
        String role = currentUser.getRole();

        // Non-admin users only see their own module
        // Remove the Modules menu entirely for non-admins
        // OR filter its items

        if (!"ADMIN".equals(role)) {
            // Find and clear the Modules menu
            menuBar.getMenus().forEach(menu -> {
                if ("Modules".equals(menu.getText())) {
                    menu.getItems().clear();
                    // Only show "Go to my module" which
                    // is wherever they already are
                    MenuItem home = new MenuItem(
                            "My Module - " + currentModule);
                    home.setDisable(true);
                    menu.getItems().add(home);
                }
                // Hide Admin menu for non-admins
                if ("Admin".equals(menu.getText())) {
                    menu.setVisible(false);
                }
            });
        }
    }

    protected void applyRoleBasedMenuBar(MenuBar menuBar) {
        if (currentUser == null) return;
        String role = currentUser.getRole();

        if ("ADMIN".equals(role)) {
            // Admin keeps full menu bar - no changes
            return;
        }

        // Non-admin — strip menu bar down to basics only
        menuBar.getMenus().removeIf(menu ->
                "Modules".equals(menu.getText())
                        || "Admin".equals(menu.getText())
                        || "Reports".equals(menu.getText())
        );

        // What remains: File (Refresh + Exit) and Help only
        // Non-admins don't need anything else
    }


}