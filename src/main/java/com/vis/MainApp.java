package com.vis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Login.fxml")
        );
        Scene scene = new Scene(loader.load());
        URL cssUrl = getClass().getResource("/styles/login.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("✅ CSS loaded: " + cssUrl);
        } else {
            System.err.println("❌ CSS not found — check file location");
        }

        Font.loadFont(
                getClass().getResourceAsStream("/Fonts/BebasNeue-Regular.ttf"), 14
        );

        System.out.println(
                getClass().getResource("/images/car_bg.jpg")
        );

        stage.setTitle("Vehicle Identification System");
        stage.setScene(scene);
        stage.setMaximized(true);      // ← Opens fullscreen
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}