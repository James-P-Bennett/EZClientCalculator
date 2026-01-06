package com.mortgage.paystub.model;

/**
 * Enumeration representing the type of employment for a borrower.
 * This determines how income calculations are performed.
 *
 * @author James Bennett
 * @version 1.0
 */
public enum EmploymentType {
    /**
     * Hourly employment - income calculated based on hourly rate and hours worked
     */
    HOURLY("Hourly"),

    /**
     * Salaried employment - income calculated based on fixed salary per pay period
     */
    SALARY("Salary");

    private final String displayName;

    /**
     * Constructs an EmploymentType enum value.
     *
     * @param displayName the human-readable name for this employment type
     */
    EmploymentType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name for this employment type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a string representation of this employment type.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
