package com.mortgage.paystub.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the results of income calculations for a borrower.
 * Contains expected monthly income, YTD pacing, variance analysis,
 * recommended usable income, and variable income breakdowns.
 *
 * @author James Bennett
 * @version 1.0
 */
public class IncomeCalculation {
    private BigDecimal expectedMonthlyIncome;
    private BigDecimal ytdMonthlyPacing;
    private int numberOfPaychecksYtd;
    private BigDecimal variancePercentage;
    private BigDecimal recommendedUsableBaseIncome;
    private String explanation;
    private List<String> warnings;
    private Map<String, BigDecimal> variableIncomeBreakdown;

    /**
     * Default constructor for IncomeCalculation.
     * Initializes collections and default values.
     */
    public IncomeCalculation() {
        this.expectedMonthlyIncome = BigDecimal.ZERO;
        this.ytdMonthlyPacing = BigDecimal.ZERO;
        this.numberOfPaychecksYtd = 0;
        this.variancePercentage = BigDecimal.ZERO;
        this.recommendedUsableBaseIncome = BigDecimal.ZERO;
        this.explanation = "";
        this.warnings = new ArrayList<>();
        this.variableIncomeBreakdown = new HashMap<>();
    }

    /**
     * Gets the expected monthly income based on hourly rate/salary and pay frequency.
     *
     * @return the expected monthly income
     */
    public BigDecimal getExpectedMonthlyIncome() {
        return expectedMonthlyIncome;
    }

    /**
     * Sets the expected monthly income.
     *
     * @param expectedMonthlyIncome the expected monthly income to set
     */
    public void setExpectedMonthlyIncome(BigDecimal expectedMonthlyIncome) {
        this.expectedMonthlyIncome = expectedMonthlyIncome != null ? expectedMonthlyIncome : BigDecimal.ZERO;
    }

    /**
     * Gets the YTD monthly pacing (actual income trend based on YTD totals).
     *
     * @return the YTD monthly pacing
     */
    public BigDecimal getYtdMonthlyPacing() {
        return ytdMonthlyPacing;
    }

    /**
     * Sets the YTD monthly pacing.
     *
     * @param ytdMonthlyPacing the YTD monthly pacing to set
     */
    public void setYtdMonthlyPacing(BigDecimal ytdMonthlyPacing) {
        this.ytdMonthlyPacing = ytdMonthlyPacing != null ? ytdMonthlyPacing : BigDecimal.ZERO;
    }

    /**
     * Gets the number of paychecks received year-to-date.
     *
     * @return the number of paychecks YTD
     */
    public int getNumberOfPaychecksYtd() {
        return numberOfPaychecksYtd;
    }

    /**
     * Sets the number of paychecks received year-to-date.
     *
     * @param numberOfPaychecksYtd the number of paychecks YTD to set
     */
    public void setNumberOfPaychecksYtd(int numberOfPaychecksYtd) {
        this.numberOfPaychecksYtd = numberOfPaychecksYtd;
    }

    /**
     * Gets the variance percentage between YTD pacing and expected income.
     * Positive means exceeding expected, negative means under-earning.
     *
     * @return the variance percentage
     */
    public BigDecimal getVariancePercentage() {
        return variancePercentage;
    }

    /**
     * Sets the variance percentage.
     *
     * @param variancePercentage the variance percentage to set
     */
    public void setVariancePercentage(BigDecimal variancePercentage) {
        this.variancePercentage = variancePercentage != null ? variancePercentage : BigDecimal.ZERO;
    }

    /**
     * Gets the recommended usable base income after applying guardrail rules.
     * This is the income that should be used for loan qualification.
     *
     * @return the recommended usable base income
     */
    public BigDecimal getRecommendedUsableBaseIncome() {
        return recommendedUsableBaseIncome;
    }

    /**
     * Sets the recommended usable base income.
     *
     * @param recommendedUsableBaseIncome the recommended usable base income to set
     */
    public void setRecommendedUsableBaseIncome(BigDecimal recommendedUsableBaseIncome) {
        this.recommendedUsableBaseIncome = recommendedUsableBaseIncome != null ? recommendedUsableBaseIncome : BigDecimal.ZERO;
    }

    /**
     * Gets the explanation or notes about the income calculation.
     * May include reasons for using expected vs YTD, documentation requirements, etc.
     *
     * @return the explanation text
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the explanation or notes.
     *
     * @param explanation the explanation to set
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation != null ? explanation : "";
    }

    /**
     * Gets the list of warnings generated during calculation.
     * Warnings may include data inconsistencies, missing documentation, etc.
     *
     * @return the list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Sets the list of warnings.
     *
     * @param warnings the warnings to set
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    /**
     * Adds a warning to the warnings list.
     *
     * @param warning the warning message to add
     */
    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            this.warnings.add(warning);
        }
    }

    /**
     * Gets the variable income breakdown.
     * Maps pay type names (e.g., "Overtime", "Commission") to recommended monthly amounts.
     *
     * @return the variable income breakdown map
     */
    public Map<String, BigDecimal> getVariableIncomeBreakdown() {
        return variableIncomeBreakdown;
    }

    /**
     * Sets the variable income breakdown.
     *
     * @param variableIncomeBreakdown the variable income breakdown to set
     */
    public void setVariableIncomeBreakdown(Map<String, BigDecimal> variableIncomeBreakdown) {
        this.variableIncomeBreakdown = variableIncomeBreakdown != null ? variableIncomeBreakdown : new HashMap<>();
    }

    /**
     * Adds a variable income entry to the breakdown.
     *
     * @param payTypeName the name of the variable pay type
     * @param monthlyAmount the recommended monthly amount for this pay type
     */
    public void addVariableIncome(String payTypeName, BigDecimal monthlyAmount) {
        if (payTypeName != null && !payTypeName.trim().isEmpty() && monthlyAmount != null) {
            this.variableIncomeBreakdown.put(payTypeName, monthlyAmount);
        }
    }

    /**
     * Calculates the total variable income from all variable pay types.
     *
     * @return the sum of all variable income amounts
     */
    public BigDecimal getTotalVariableIncome() {
        return variableIncomeBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total qualified monthly income (base + variable).
     *
     * @return the total qualified monthly income
     */
    public BigDecimal getTotalQualifiedMonthlyIncome() {
        return recommendedUsableBaseIncome.add(getTotalVariableIncome());
    }

    /**
     * Checks if there are any warnings.
     *
     * @return true if warnings exist, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Checks if the YTD pacing is within the acceptable range (0-5% low).
     *
     * @return true if variance is within 0-5% low, false otherwise
     */
    public boolean isWithinAcceptableRange() {
        return variancePercentage.compareTo(new BigDecimal("-5")) >= 0;
    }

    /**
     * Checks if the variance requires documented explanation (5-10% low).
     *
     * @return true if variance is 5-10% low, false otherwise
     */
    public boolean requiresDocumentedExplanation() {
        return variancePercentage.compareTo(new BigDecimal("-5")) < 0 &&
               variancePercentage.compareTo(new BigDecimal("-10")) >= 0;
    }

    /**
     * Checks if the variance is significant (>10% low).
     *
     * @return true if variance is more than 10% low, false otherwise
     */
    public boolean hasSignificantVariance() {
        return variancePercentage.compareTo(new BigDecimal("-10")) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncomeCalculation that = (IncomeCalculation) o;
        return numberOfPaychecksYtd == that.numberOfPaychecksYtd &&
                Objects.equals(expectedMonthlyIncome, that.expectedMonthlyIncome) &&
                Objects.equals(ytdMonthlyPacing, that.ytdMonthlyPacing) &&
                Objects.equals(variancePercentage, that.variancePercentage) &&
                Objects.equals(recommendedUsableBaseIncome, that.recommendedUsableBaseIncome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedMonthlyIncome, ytdMonthlyPacing, numberOfPaychecksYtd,
                variancePercentage, recommendedUsableBaseIncome);
    }

    /**
     * Returns a string representation of this income calculation for debugging purposes.
     *
     * @return a string containing key calculation results
     */
    @Override
    public String toString() {
        return "IncomeCalculation{" +
                "expectedMonthlyIncome=" + expectedMonthlyIncome +
                ", ytdMonthlyPacing=" + ytdMonthlyPacing +
                ", numberOfPaychecksYtd=" + numberOfPaychecksYtd +
                ", variancePercentage=" + variancePercentage + "%" +
                ", recommendedUsableBaseIncome=" + recommendedUsableBaseIncome +
                ", totalVariableIncome=" + getTotalVariableIncome() +
                ", totalQualifiedIncome=" + getTotalQualifiedMonthlyIncome() +
                ", warnings=" + warnings.size() +
                '}';
    }
}
