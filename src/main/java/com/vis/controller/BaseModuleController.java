package com.vis.controller;

import com.vis.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

    // Shared navigation back to dashboard
    protected void goToDashboard(Label anyLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/styles/theme.css")
                            .toExternalForm());

            DashboardController dc = loader.getController();
            dc.initUser(currentUser);

            Stage stage = (Stage) anyLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}