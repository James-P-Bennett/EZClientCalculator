package com.mortgage.paystub.gui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * About dialog showing application information, version, and credits.
 *
 * @author James Bennett
 * @version 1.0
 */
public class AboutDialog extends Dialog<Void> {

    private static final String VERSION = "1.0-SNAPSHOT";
    private static final String APP_NAME = "EZ Client Calculator";
    private static final String AUTHOR = "James Bennett";
    private static final int YEAR = 2026;

    /**
     * Creates a new AboutDialog.
     */
    public AboutDialog() {
        setTitle("About " + APP_NAME);
        setHeaderText(null);
        setResizable(false);

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(450);

        // App icon/logo (using emoji for now)
        Label icon = new Label("\uD83D\uDCCA");
        icon.setFont(Font.font(48));

        // App name
        Label appName = new Label(APP_NAME);
        appName.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Version
        Label version = new Label("Version " + VERSION);
        version.setFont(Font.font(14));
        version.setStyle("-fx-text-fill: #757575;");

        // Separator
        Separator separator1 = new Separator();

        // Description
        Label description = new Label(
            "A paystub income calculator for mortgage lending purposes.\n\n" +
            "Calculates qualified monthly income following USDA, FHA,\n" +
            "and Conventional loan guidelines."
        );
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);
        description.setStyle("-fx-font-size: 13px;");

        // Separator
        Separator separator2 = new Separator();

        // Features
        VBox features = new VBox(5);
        features.setAlignment(Pos.CENTER_LEFT);
        features.setPadding(new Insets(0, 20, 0, 20));

        Label featuresTitle = new Label("Key Features:");
        featuresTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label feature1 = new Label("\u2022 PDF and image paystub parsing");
        Label feature2 = new Label("\u2022 Automated income calculations");
        Label feature3 = new Label("\u2022 Guardrail logic and variance analysis");
        Label feature4 = new Label("\u2022 Click-to-copy results");
        Label feature5 = new Label("\u2022 Variable income tracking");

        features.getChildren().addAll(featuresTitle, feature1, feature2, feature3, feature4, feature5);

        // Separator
        Separator separator3 = new Separator();

        // Credits
        VBox credits = new VBox(3);
        credits.setAlignment(Pos.CENTER);

        Label authorLabel = new Label("Author: " + AUTHOR);
        authorLabel.setStyle("-fx-font-size: 12px;");

        Label copyright = new Label("\u00A9 " + YEAR + " All Rights Reserved");
        copyright.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

        credits.getChildren().addAll(authorLabel, copyright);

        // Technology stack
        Label tech = new Label("Built with JavaFX, Apache PDFBox, and Tesseract OCR");
        tech.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999; -fx-font-style: italic;");

        // Add all components
        content.getChildren().addAll(
            icon,
            appName,
            version,
            separator1,
            description,
            separator2,
            features,
            separator3,
            credits,
            tech
        );

        getDialogPane().setContent(content);

        // Close button
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeButton);

        // Set result converter
        setResultConverter(buttonType -> null);
    }

    /**
     * Shows the About dialog.
     */
    public static void showDialog() {
        AboutDialog dialog = new AboutDialog();
        dialog.showAndWait();
    }
}
