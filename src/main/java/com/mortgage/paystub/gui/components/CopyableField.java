package com.mortgage.paystub.gui.components;

import com.mortgage.paystub.gui.StatusBar;
import com.mortgage.paystub.utils.ClipboardUtil;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * A custom component that displays a label and value, and copies the value to clipboard on click.
 * Provides visual feedback when clicked.
 *
 * @author James Bennett
 * @version 1.0
 */
public class CopyableField extends HBox {

    private final Label labelField;
    private final Label valueField;
    private final Label copiedIndicator;
    private final StatusBar statusBar;
    private String copyText;

    /**
     * Creates a new CopyableField.
     *
     * @param label the field label
     * @param value the field value
     * @param statusBar the status bar for showing copy feedback
     */
    public CopyableField(String label, String value, StatusBar statusBar) {
        this(label, value, statusBar, false);
    }

    /**
     * Creates a new CopyableField.
     *
     * @param label the field label
     * @param value the field value
     * @param statusBar the status bar for showing copy feedback
     * @param isProminent whether this field should have prominent styling
     */
    public CopyableField(String label, String value, StatusBar statusBar, boolean isProminent) {
        super(10);
        this.statusBar = statusBar;
        this.copyText = value;

        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(8, 12, 8, 12));
        this.setCursor(Cursor.HAND);

        // Base styling
        this.getStyleClass().add("copyable-field");

        // Label
        labelField = new Label(label);
        labelField.getStyleClass().add(isProminent ? "copyable-field-label-prominent" : "copyable-field-label");

        // Value
        valueField = new Label(value);
        valueField.getStyleClass().add(isProminent ? "copyable-field-value-prominent" : "copyable-field-value");

        // Copied indicator
        copiedIndicator = new Label("✓ Copied!");
        copiedIndicator.getStyleClass().add("copyable-field-copied");
        copiedIndicator.setOpacity(0);

        this.getChildren().addAll(labelField, valueField, copiedIndicator);

        // Tooltip
        Tooltip tooltip = new Tooltip("Click to copy");
        Tooltip.install(this, tooltip);

        // Click to copy
        this.setOnMouseClicked(e -> copyToClipboard());
    }

    /**
     * Copies the field value to clipboard and shows visual feedback.
     */
    private void copyToClipboard() {
        boolean success = ClipboardUtil.copyToClipboard(copyText);

        if (success) {
            // Show copied indicator with fade animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), copiedIndicator);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), copiedIndicator);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(1000));

            fadeIn.setOnFinished(e -> fadeOut.play());
            fadeIn.play();

            // Scale animation for value field
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), valueField);
            scale.setFromX(1);
            scale.setFromY(1);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();

            // Update status bar
            if (statusBar != null) {
                String label = labelField.getText();
                String value = valueField.getText();
                statusBar.setStatus("Copied: " + label + " - " + value);
            }
        }
    }

    /**
     * Sets the value to be copied (may differ from displayed value).
     *
     * @param copyText the text to copy
     */
    public void setCopyText(String copyText) {
        this.copyText = copyText;
    }

    /**
     * Updates the displayed value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.valueField.setText(value);
        if (this.copyText.equals(this.valueField.getText())) {
            this.copyText = value;
        }
    }

    /**
     * Updates the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        this.labelField.setText(label);
    }
}
