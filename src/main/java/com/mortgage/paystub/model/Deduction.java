package com.mortgage.paystub.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a deduction on a paystub.
 * Deductions include taxes, insurance, retirement contributions, and other withholdings.
 *
 * @author James Bennett
 * @version 1.0
 */
public class Deduction {
    private String deductionName;
    private BigDecimal currentAmount;
    private BigDecimal ytdAmount;

    /**
     * Default constructor for Deduction.
     */
    public Deduction() {
        this.currentAmount = BigDecimal.ZERO;
        this.ytdAmount = BigDecimal.ZERO;
    }

    /**
     * Constructs a Deduction with all fields.
     *
     * @param deductionName the name of the deduction (e.g., "Federal Tax", "Health Insurance", "401k")
     * @param currentAmount the deduction amount for the current pay period
     * @param ytdAmount the year-to-date total deduction amount
     */
    public Deduction(String deductionName, BigDecimal currentAmount, BigDecimal ytdAmount) {
        this.deductionName = deductionName;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.ytdAmount = ytdAmount != null ? ytdAmount : BigDecimal.ZERO;
    }

    /**
     * Gets the name of the deduction.
     *
     * @return the deduction name
     */
    public String getDeductionName() {
        return deductionName;
    }

    /**
     * Sets the name of the deduction.
     *
     * @param deductionName the deduction name to set
     */
    public void setDeductionName(String deductionName) {
        this.deductionName = deductionName;
    }

    /**
     * Gets the deduction amount for the current pay period.
     *
     * @return the current period deduction amount
     */
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    /**
     * Sets the deduction amount for the current pay period.
     *
     * @param currentAmount the current period deduction amount to set
     */
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
    }

    /**
     * Gets the year-to-date total deduction amount.
     *
     * @return the YTD deduction amount
     */
    public BigDecimal getYtdAmount() {
        return ytdAmount;
    }

    /**
     * Sets the year-to-date total deduction amount.
     *
     * @param ytdAmount the YTD deduction amount to set
     */
    public void setYtdAmount(BigDecimal ytdAmount) {
        this.ytdAmount = ytdAmount != null ? ytdAmount : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deduction deduction = (Deduction) o;
        return Objects.equals(deductionName, deduction.deductionName) &&
                Objects.equals(currentAmount, deduction.currentAmount) &&
                Objects.equals(ytdAmount, deduction.ytdAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deductionName, currentAmount, ytdAmount);
    }

    /**
     * Returns a string representation of this deduction for debugging purposes.
     *
     * @return a string containing all field values
     */
    @Override
    public String toString() {
        return "Deduction{" +
                "deductionName='" + deductionName + '\'' +
                ", currentAmount=" + currentAmount +
                ", ytdAmount=" + ytdAmount +
                '}';
    }
}
