package com.mortgage.paystub;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the EZ Client Calculator.
 * This is a paystub income calculator for mortgage lending purposes.
 *
 * @author James Bennett
 * @version 1.0-SNAPSHOT
 */
public class PaystubCalculatorApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(PaystubCalculatorApp.class);
    private static final String APP_TITLE = "EZ Client Calculator - Paystub Income Analyzer";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting EZ Client Calculator application");

        try {
            // Create a simple layout for initial verification
            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(40));

            Label titleLabel = new Label("EZ Client Calculator");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitleLabel = new Label("Paystub Income Calculator for Mortgage Lending");
            subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

            Label statusLabel = new Label("Application initialized successfully!");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00aa00;");

            Label versionLabel = new Label("Version 1.0-SNAPSHOT");
            versionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999;");

            root.getChildren().addAll(titleLabel, subtitleLabel, statusLabel, versionLabel);

            // Create and set the scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(WINDOW_WIDTH);
            primaryStage.setMinHeight(WINDOW_HEIGHT);
            primaryStage.show();

            logger.info("Application window displayed successfully");

        } catch (Exception e) {
            logger.error("Error starting application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down EZ Client Calculator application");
    }

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Launching EZ Client Calculator...");
        launch(args);
    }
}
