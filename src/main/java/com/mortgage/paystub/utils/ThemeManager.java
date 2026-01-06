package com.mortgage.paystub.utils;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Manages application theme (light/dark mode) with persistent storage.
 * Uses Java Preferences API to save theme selection across application restarts.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ThemeManager {

    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    // Preferences keys
    private static final String PREFS_NODE = "com.mortgage.paystub";
    private static final String THEME_KEY = "theme";

    // Theme identifiers
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    // CSS file paths
    private static final String LIGHT_THEME_CSS = "/styles.css";
    private static final String DARK_THEME_CSS = "/styles-dark.css";

    private static String currentTheme = THEME_LIGHT;
    private static Scene applicationScene;

    /**
     * Initialize the theme manager with the application scene.
     *
     * @param scene the main application scene
     */
    public static void initialize(Scene scene) {
        applicationScene = scene;
        loadSavedTheme();
        applyTheme(currentTheme);
        logger.info("ThemeManager initialized with {} theme", currentTheme);
    }

    /**
     * Get the current theme.
     *
     * @return current theme identifier (THEME_LIGHT or THEME_DARK)
     */
    public static String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Check if dark mode is currently active.
     *
     * @return true if dark mode is active, false otherwise
     */
    public static boolean isDarkMode() {
        return THEME_DARK.equals(currentTheme);
    }

    /**
     * Toggle between light and dark themes.
     */
    public static void toggleTheme() {
        String newTheme = isDarkMode() ? THEME_LIGHT : THEME_DARK;
        setTheme(newTheme);
    }

    /**
     * Set the application theme.
     *
     * @param theme theme identifier (THEME_LIGHT or THEME_DARK)
     */
    public static void setTheme(String theme) {
        if (!THEME_LIGHT.equals(theme) && !THEME_DARK.equals(theme)) {
            logger.warn("Invalid theme: {}, defaulting to light", theme);
            theme = THEME_LIGHT;
        }

        currentTheme = theme;
        applyTheme(theme);
        saveTheme(theme);

        logger.info("Theme changed to: {}", theme);
    }

    /**
     * Apply the specified theme to the application scene.
     *
     * @param theme theme identifier to apply
     */
    private static void applyTheme(String theme) {
        if (applicationScene == null) {
            logger.warn("Application scene not initialized, cannot apply theme");
            return;
        }

        // Remove existing theme stylesheets
        applicationScene.getStylesheets().clear();

        // Add the selected theme stylesheet
        String themeCSS = THEME_DARK.equals(theme) ? DARK_THEME_CSS : LIGHT_THEME_CSS;

        try {
            String stylesheetURL = ThemeManager.class.getResource(themeCSS).toExternalForm();
            applicationScene.getStylesheets().add(stylesheetURL);
            logger.debug("Applied theme stylesheet: {}", themeCSS);
        } catch (Exception e) {
            logger.error("Error loading theme stylesheet: {}", themeCSS, e);
            // Fallback to light theme if there's an error
            if (!THEME_LIGHT.equals(theme)) {
                try {
                    String fallbackURL = ThemeManager.class.getResource(LIGHT_THEME_CSS).toExternalForm();
                    applicationScene.getStylesheets().add(fallbackURL);
                    logger.info("Fell back to light theme");
                } catch (Exception fallbackError) {
                    logger.error("Failed to load fallback theme", fallbackError);
                }
            }
        }
    }

    /**
     * Load the saved theme preference from persistent storage.
     */
    private static void loadSavedTheme() {
        try {
            Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
            currentTheme = prefs.get(THEME_KEY, THEME_LIGHT);
            logger.debug("Loaded saved theme: {}", currentTheme);
        } catch (Exception e) {
            logger.error("Error loading theme preference, using default", e);
            currentTheme = THEME_LIGHT;
        }
    }

    /**
     * Save the current theme preference to persistent storage.
     *
     * @param theme theme identifier to save
     */
    private static void saveTheme(String theme) {
        try {
            Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
            prefs.put(THEME_KEY, theme);
            prefs.flush();
            logger.debug("Saved theme preference: {}", theme);
        } catch (Exception e) {
            logger.error("Error saving theme preference", e);
        }
    }

    /**
     * Get a user-friendly name for the current theme.
     *
     * @return theme display name
     */
    public static String getCurrentThemeName() {
        return isDarkMode() ? "Dark Mode" : "Light Mode";
    }

    /**
     * Get the name of the opposite theme (for toggle menu item text).
     *
     * @return opposite theme display name
     */
    public static String getOppositeThemeName() {
        return isDarkMode() ? "Light Mode" : "Dark Mode";
    }
}
