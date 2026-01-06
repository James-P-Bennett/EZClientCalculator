package com.mortgage.paystub.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Status bar component for displaying application status and feedback.
 * Shows the current operation status, warnings, and errors.
 *
 * @author James Bennett
 * @version 1.0
 */
public class StatusBar extends HBox {

    private final Label statusLabel;
    private final Label warningCountLabel;
    private final Label errorCountLabel;

    private int warningCount = 0;
    private int errorCount = 0;

    /**
     * Creates a new status bar.
     */
    public StatusBar() {
        super(10);
        this.setPadding(new Insets(5, 10, 5, 10));
        this.getStyleClass().add("status-bar");

        // Status label (left side)
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        // Spacer to push warning/error counts to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Warning count label
        warningCountLabel = new Label();
        warningCountLabel.getStyleClass().add("warning-label");
        warningCountLabel.setVisible(false);

        // Error count label
        errorCountLabel = new Label();
        errorCountLabel.getStyleClass().add("error-label");
        errorCountLabel.setVisible(false);

        this.getChildren().addAll(statusLabel, spacer, warningCountLabel, errorCountLabel);
        this.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Sets the status message.
     *
     * @param message the status message to display
     */
    public void setStatus(String message) {
        if (message != null && !message.trim().isEmpty()) {
            statusLabel.setText(message);
        } else {
            statusLabel.setText("Ready");
        }
    }

    /**
     * Sets the warning count.
     *
     * @param count the number of warnings
     */
    public void setWarningCount(int count) {
        this.warningCount = count;
        updateWarningLabel();
    }

    /**
     * Increments the warning count.
     */
    public void incrementWarningCount() {
        this.warningCount++;
        updateWarningLabel();
    }

    /**
     * Sets the error count.
     *
     * @param count the number of errors
     */
    public void setErrorCount(int count) {
        this.errorCount = count;
        updateErrorLabel();
    }

    /**
     * Increments the error count.
     */
    public void incrementErrorCount() {
        this.errorCount++;
        updateErrorLabel();
    }

    /**
     * Resets all counts to zero.
     */
    public void resetCounts() {
        this.warningCount = 0;
        this.errorCount = 0;
        updateWarningLabel();
        updateErrorLabel();
    }

    /**
     * Gets the current warning count.
     *
     * @return the warning count
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * Gets the current error count.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Updates the warning label display.
     */
    private void updateWarningLabel() {
        if (warningCount > 0) {
            warningCountLabel.setText(String.format("\u26A0 %d Warning%s",
                    warningCount, warningCount == 1 ? "" : "s"));
            warningCountLabel.setVisible(true);
        } else {
            warningCountLabel.setVisible(false);
        }
    }

    /**
     * Updates the error label display.
     */
    private void updateErrorLabel() {
        if (errorCount > 0) {
            errorCountLabel.setText(String.format("\u274C %d Error%s",
                    errorCount, errorCount == 1 ? "" : "s"));
            errorCountLabel.setVisible(true);
        } else {
            errorCountLabel.setVisible(false);
        }
    }

    /**
     * Shows a temporary status message that reverts to "Ready" after a delay.
     *
     * @param message the temporary message
     * @param durationMs the duration in milliseconds
     */
    public void showTemporaryStatus(String message, int durationMs) {
        setStatus(message);

        // Use JavaFX Timeline or Thread to revert after delay
        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
                javafx.application.Platform.runLater(() -> setStatus("Ready"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
