package com.mortgage.paystub.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for clipboard operations.
 * Provides methods to copy text to the system clipboard.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ClipboardUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClipboardUtil.class);

    /**
     * Copies the specified text to the system clipboard.
     *
     * @param text the text to copy
     * @return true if successful, false otherwise
     */
    public static boolean copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            logger.warn("Attempted to copy null or empty text to clipboard");
            return false;
        }

        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);

            logger.debug("Copied to clipboard: {}",
                text.length() > 50 ? text.substring(0, 50) + "..." : text);
            return true;
        } catch (Exception e) {
            logger.error("Error copying to clipboard", e);
            return false;
        }
    }

    /**
     * Copies formatted currency value to clipboard.
     *
     * @param label the field label
     * @param value the currency value
     * @return true if successful, false otherwise
     */
    public static boolean copyCurrency(String label, String value) {
        return copyToClipboard(label + ": " + value);
    }

    /**
     * Copies formatted percentage value to clipboard.
     *
     * @param label the field label
     * @param value the percentage value
     * @return true if successful, false otherwise
     */
    public static boolean copyPercentage(String label, String value) {
        return copyToClipboard(label + ": " + value);
    }
}
