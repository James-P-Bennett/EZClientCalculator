package com.mortgage.paystub;

import com.mortgage.paystub.gui.StatusBar;
import com.mortgage.paystub.gui.tabs.AnalysisTab;
import com.mortgage.paystub.gui.tabs.CalculationTab;
import com.mortgage.paystub.gui.tabs.ImportTab;
import com.mortgage.paystub.gui.tabs.ResultsTab;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final String VERSION = "1.0-SNAPSHOT";

    private StatusBar statusBar;
    private TabPane tabPane;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting EZ Client Calculator application");

        try {
            // Create the main layout
            BorderPane root = new BorderPane();

            // Create menu bar
            MenuBar menuBar = createMenuBar(primaryStage);
            root.setTop(menuBar);

            // Create tab pane with all tabs
            tabPane = createTabPane();
            root.setCenter(tabPane);

            // Create status bar
            statusBar = new StatusBar();
            root.setBottom(statusBar);

            // Create and configure the scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Apply CSS styling
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            // Update status bar
            statusBar.setStatus("Application initialized - Ready to import paystubs");

            logger.info("Application window displayed successfully");

        } catch (Exception e) {
            logger.error("Error starting application", e);
            showErrorDialog("Startup Error", "Failed to start application: " + e.getMessage());
            throw new RuntimeException("Failed to start application", e);
        }
    }

    /**
     * Creates the menu bar with File, Edit, and Help menus.
     *
     * @param stage the primary stage
     * @return the configured menu bar
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");

        MenuItem openItem = new MenuItem("Open Paystubs...");
        openItem.setOnAction(e -> {
            statusBar.setStatus("Opening file chooser...");
            // Will be implemented in Step 6
        });

        MenuItem saveSessionItem = new MenuItem("Save Session...");
        saveSessionItem.setOnAction(e -> {
            statusBar.setStatus("Saving session...");
            // Will be implemented later
        });

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            logger.info("Exit menu item selected");
            Platform.exit();
        });

        fileMenu.getItems().addAll(openItem, saveSessionItem, separator1, exitItem);

        // Edit Menu
        Menu editMenu = new Menu("Edit");

        MenuItem preferencesItem = new MenuItem("Preferences...");
        preferencesItem.setOnAction(e -> {
            statusBar.setStatus("Opening preferences...");
            showInfoDialog("Preferences", "Preferences will be implemented in a future version.");
        });

        editMenu.getItems().add(preferencesItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");

        MenuItem userGuideItem = new MenuItem("User Guide");
        userGuideItem.setOnAction(e -> {
            statusBar.setStatus("Opening user guide...");
            showUserGuide();
        });

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(userGuideItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        return menuBar;
    }

    /**
     * Creates the tab pane with all application tabs.
     *
     * @return the configured tab pane
     */
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Import Tab - using actual ImportTab component
        Tab importTab = new Tab("1. Import");
        ImportTab importTabContent = new ImportTab(statusBar);
        importTab.setContent(importTabContent);

        // Analysis Tab - using actual AnalysisTab component
        Tab analysisTab = new Tab("2. Analysis");
        AnalysisTab analysisTabContent = new AnalysisTab(statusBar);
        analysisTab.setContent(analysisTabContent);

        // Calculation Tab - using actual CalculationTab component
        Tab calculationTab = new Tab("3. Calculation");
        CalculationTab calculationTabContent = new CalculationTab(statusBar);
        calculationTab.setContent(calculationTabContent);

        // Wire ImportTab to AnalysisTab
        importTabContent.setAnalysisTab(analysisTabContent);
        importTabContent.setOnProcessComplete(() -> {
            // Switch to analysis tab when processing starts
            tabPane.getSelectionModel().select(analysisTab);
        });

        // Wire AnalysisTab to CalculationTab
        analysisTabContent.setCalculationTab(calculationTabContent);
        analysisTabContent.setOnCalculateComplete(() -> {
            // Switch to calculation tab when calculation completes
            tabPane.getSelectionModel().select(calculationTab);
        });

        // Results Tab - using actual ResultsTab component
        Tab resultsTab = new Tab("4. Results");
        ResultsTab resultsTabContent = new ResultsTab(statusBar);
        resultsTab.setContent(resultsTabContent);

        // Wire CalculationTab to ResultsTab
        calculationTabContent.setOnViewResults(() -> {
            // Pass calculation data to results tab
            if (calculationTabContent.getCalculation() != null && calculationTabContent.getBorrower() != null) {
                resultsTabContent.displayResults(
                    calculationTabContent.getBorrower(),
                    calculationTabContent.getCalculation()
                );
            }
            // Switch to results tab when View Results is clicked
            tabPane.getSelectionModel().select(resultsTab);
        });

        tabPane.getTabs().addAll(importTab, analysisTab, calculationTab, resultsTab);

        // Add listener to update status bar when tab changes
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                statusBar.setStatus("Viewing " + newTab.getText() + " tab");
            }
        });

        return tabPane;
    }


    /**
     * Shows the user guide dialog.
     */
    private void showUserGuide() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Guide");
        alert.setHeaderText("EZ Client Calculator - User Guide");

        String guide = """
                How to Use:

                1. IMPORT: Select PDF or image paystub files
                2. ANALYSIS: Review extracted data and make corrections
                3. CALCULATION: View income calculations with guardrails
                4. RESULTS: Copy formatted results to clipboard

                The calculator follows USDA/FHA guidelines for qualifying income.

                For detailed information, please refer to the README.md file.
                """;

        alert.setContentText(guide);
        alert.showAndWait();
    }

    /**
     * Shows the About dialog.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("EZ Client Calculator");

        String about = String.format("""
                Version: %s

                A paystub income calculator for mortgage lending purposes.
                Calculates qualified monthly income following USDA, FHA,
                and Conventional loan guidelines.

                Author: James Bennett

                \u00A9 2026 All Rights Reserved
                """, VERSION);

        alert.setContentText(about);
        alert.showAndWait();
    }

    /**
     * Shows an error dialog.
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information dialog.
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
