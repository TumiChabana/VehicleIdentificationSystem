package com.vis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Landing.fxml")
        );
        Scene scene = new Scene(loader.load());

        Font.loadFont(
                getClass().getResourceAsStream("/Fonts/BebasNeue-Regular.ttf"), 14
        );

        System.out.println(
                getClass().getResource("/images/car_bg.jpg")
        );

        URL base    = getClass().getResource("/styles/base.css");
        URL landing = getClass().getResource("/styles/landing.css");
        if (base    != null) scene.getStylesheets()
                .add(base.toExternalForm());
        if (landing != null) scene.getStylesheets()
                .add(landing.toExternalForm());

        URL image = getClass().getResource("/images/logo3.png");

        Image icon= new Image(image.openStream());
        stage.getIcons().add(icon);

        stage.setTitle("NVis");
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