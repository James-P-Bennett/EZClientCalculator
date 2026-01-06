package com.mortgage.paystub.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an earning/pay type on a paystub.
 * Each earning has a name, category, current period amount, and year-to-date amount.
 *
 * @author James Bennett
 * @version 1.0
 */
public class Earning {
    private String payTypeName;
    private PayCategory category;
    private BigDecimal currentAmount;
    private BigDecimal ytdAmount;

    /**
     * Default constructor for Earning.
     */
    public Earning() {
        this.currentAmount = BigDecimal.ZERO;
        this.ytdAmount = BigDecimal.ZERO;
    }

    /**
     * Constructs an Earning with all fields.
     *
     * @param payTypeName the name of the pay type (e.g., "Regular", "Overtime", "Bonus")
     * @param category the category of this earning (BASE_WAGE, VARIABLE, or OTHER)
     * @param currentAmount the amount for the current pay period
     * @param ytdAmount the year-to-date total amount
     */
    public Earning(String payTypeName, PayCategory category, BigDecimal currentAmount, BigDecimal ytdAmount) {
        this.payTypeName = payTypeName;
        this.category = category;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.ytdAmount = ytdAmount != null ? ytdAmount : BigDecimal.ZERO;
    }

    /**
     * Gets the name of the pay type.
     *
     * @return the pay type name
     */
    public String getPayTypeName() {
        return payTypeName;
    }

    /**
     * Sets the name of the pay type.
     *
     * @param payTypeName the pay type name to set
     */
    public void setPayTypeName(String payTypeName) {
        this.payTypeName = payTypeName;
    }

    /**
     * Gets the category of this earning.
     *
     * @return the pay category
     */
    public PayCategory getCategory() {
        return category;
    }

    /**
     * Sets the category of this earning.
     *
     * @param category the pay category to set
     */
    public void setCategory(PayCategory category) {
        this.category = category;
    }

    /**
     * Gets the amount for the current pay period.
     *
     * @return the current period amount
     */
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    /**
     * Sets the amount for the current pay period.
     *
     * @param currentAmount the current period amount to set
     */
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
    }

    /**
     * Gets the year-to-date total amount.
     *
     * @return the YTD amount
     */
    public BigDecimal getYtdAmount() {
        return ytdAmount;
    }

    /**
     * Sets the year-to-date total amount.
     *
     * @param ytdAmount the YTD amount to set
     */
    public void setYtdAmount(BigDecimal ytdAmount) {
        this.ytdAmount = ytdAmount != null ? ytdAmount : BigDecimal.ZERO;
    }

    /**
     * Checks if this earning is a base wage earning (used in expected monthly income calculations).
     *
     * @return true if this is a base wage earning, false otherwise
     */
    public boolean isBaseWage() {
        return category == PayCategory.BASE_WAGE;
    }

    /**
     * Checks if this earning is variable income (overtime, commission, bonus).
     *
     * @return true if this is variable income, false otherwise
     */
    public boolean isVariableIncome() {
        return category == PayCategory.VARIABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Earning earning = (Earning) o;
        return Objects.equals(payTypeName, earning.payTypeName) &&
                category == earning.category &&
                Objects.equals(currentAmount, earning.currentAmount) &&
                Objects.equals(ytdAmount, earning.ytdAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payTypeName, category, currentAmount, ytdAmount);
    }

    /**
     * Returns a string representation of this earning for debugging purposes.
     *
     * @return a string containing all field values
     */
    @Override
    public String toString() {
        return "Earning{" +
                "payTypeName='" + payTypeName + '\'' +
                ", category=" + category +
                ", currentAmount=" + currentAmount +
                ", ytdAmount=" + ytdAmount +
                '}';
    }
}
