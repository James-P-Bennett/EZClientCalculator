package com.mortgage.paystub.model;

/**
 * Enumeration representing the category of a pay type/earning.
 * This categorization is important for determining which income should be included
 * in base income calculations versus variable income calculations.
 *
 * @author James Bennett
 * @version 1.0
 */
public enum PayCategory {
    /**
     * Base wage income - includes regular pay, holiday pay, PTO, vacation, sick pay.
     * This is used for calculating expected monthly income and YTD pacing.
     */
    BASE_WAGE("Base Wage", "Includes regular, holiday, PTO, vacation, and sick pay"),

    /**
     * Variable income - includes overtime, commission, and bonuses.
     * This income is calculated separately using 24-month averaging or conservative figures.
     */
    VARIABLE("Variable Income", "Includes overtime, commission, and bonuses"),

    /**
     * Other income - miscellaneous pay types that don't fit in the other categories.
     * May require special handling or documentation.
     */
    OTHER("Other", "Miscellaneous income requiring special consideration");

    private final String displayName;
    private final String description;

    /**
     * Constructs a PayCategory enum value.
     *
     * @param displayName the human-readable name for this category
     * @param description a description of what types of pay are included in this category
     */
    PayCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for this category.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of what pay types are included in this category.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a string representation of this pay category.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
