package com.mortgage.paystub.model;

/**
 * Enumeration representing the frequency at which an employee is paid.
 * Each frequency has an associated number of pay periods per year, which is used
 * for income calculations and conversions to monthly income.
 *
 * @author James Bennett
 * @version 1.0
 */
public enum PayFrequency {
    /**
     * Weekly pay frequency - 52 pay periods per year
     */
    WEEKLY(52, "Weekly"),

    /**
     * Bi-weekly pay frequency - 26 pay periods per year (every two weeks)
     */
    BI_WEEKLY(26, "Bi-Weekly"),

    /**
     * Semi-monthly pay frequency - 24 pay periods per year (twice per month)
     */
    SEMI_MONTHLY(24, "Semi-Monthly"),

    /**
     * Monthly pay frequency - 12 pay periods per year
     */
    MONTHLY(12, "Monthly");

    private final int periodsPerYear;
    private final String displayName;

    /**
     * Constructs a PayFrequency enum value.
     *
     * @param periodsPerYear the number of pay periods in a year for this frequency
     * @param displayName the human-readable name for this frequency
     */
    PayFrequency(int periodsPerYear, String displayName) {
        this.periodsPerYear = periodsPerYear;
        this.displayName = displayName;
    }

    /**
     * Gets the number of pay periods per year for this frequency.
     *
     * @return the number of pay periods per year
     */
    public int getPeriodsPerYear() {
        return periodsPerYear;
    }

    /**
     * Gets the human-readable display name for this frequency.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a string representation of this pay frequency.
     *
     * @return the display name followed by the number of periods per year
     */
    @Override
    public String toString() {
        return displayName + " (" + periodsPerYear + " pay periods/year)";
    }
}
