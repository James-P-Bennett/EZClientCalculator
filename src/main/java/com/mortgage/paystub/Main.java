package com.mortgage.paystub;

/**
 * Main launcher class for the EZ Client Calculator JAR file.
 * This class serves as the entry point for the executable JAR.
 *
 * JavaFX applications require a separate launcher class when packaged as a JAR
 * because the Application class cannot be directly used as the main class.
 *
 * @author James Bennett
 * @version 1.0-SNAPSHOT
 */
public class Main {

    /**
     * Main entry point for the JAR file.
     * Launches the JavaFX application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        PaystubCalculatorApp.main(args);
    }
}
