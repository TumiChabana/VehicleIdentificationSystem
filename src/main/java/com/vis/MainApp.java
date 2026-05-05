package com.vis;

import com.vis.dao.*;
import com.vis.util.DataCache;
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
        preloadDataInBackground();
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

    private void preloadDataInBackground() {
        Thread t = new Thread(() -> {
            try {
                DataCache cache = DataCache.getInstance();
                VehicleDAO   vehicleDAO   = new VehicleDAO();
                CustomerDAO  customerDAO  = new CustomerDAO();
                PoliceDAO    policeDAO    = new PoliceDAO();
                InsuranceDAO insuranceDAO = new InsuranceDAO();
                ServiceRecordDAO serviceDAO =
                        new ServiceRecordDAO();

                cache.vehicles   =
                        vehicleDAO.getAllVehiclesWithOwners();
                cache.customers  =
                        customerDAO.getAllCustomers();
                cache.violations =
                        policeDAO.getAllViolations();
                cache.reports    =
                        policeDAO.getAllReports();
                cache.insurance  =
                        insuranceDAO.getAllRecordsWithVehicle();
                cache.services   =
                        serviceDAO.getAllServiceRecords();

                cache.markReady();
                System.out.println(
                        "✅ Data preloaded successfully.");

            } catch (Exception e) {
                System.err.println(
                        "Preload failed: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.setName("preload-thread");
        t.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}