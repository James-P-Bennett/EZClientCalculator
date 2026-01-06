package com.mortgage.paystub.calculator;

import com.mortgage.paystub.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Core income calculation engine for mortgage lending purposes.
 * Implements all calculation formulas following USDA, FHA, and Conventional loan guidelines.
 *
 * <p>This class calculates:
 * <ul>
 *   <li>Expected monthly income (based on hourly rate or salary)</li>
 *   <li>YTD monthly pacing (actual income trend)</li>
 *   <li>Variance analysis and guardrail logic</li>
 *   <li>Variable income calculations (overtime, commission, bonus)</li>
 *   <li>Recommended usable income for qualification</li>
 * </ul>
 *
 * @author James Bennett
 * @version 1.0
 */
public class
IncomeCalculator {
    private static final Logger logger = LoggerFactory.getLogger(IncomeCalculator.class);

    // Constants for guardrail thresholds
    private static final BigDecimal ACCEPTABLE_VARIANCE_THRESHOLD = new BigDecimal("-5.0");
    private static final BigDecimal DOCUMENTED_EXPLANATION_THRESHOLD = new BigDecimal("-10.0");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final int SCALE = 2;
    private static final int CALCULATION_SCALE = 6; // Higher precision for intermediate calculations

    /**
     * Calculates the complete income analysis for a borrower.
     *
     * @param borrower the borrower with paystubs to analyze
     * @return IncomeCalculation object containing all calculation results
     * @throws IllegalArgumentException if borrower or required data is missing
     */
    public IncomeCalculation calculateIncome(Borrower borrower) {
        validateBorrower(borrower);

        logger.info("Starting income calculation for borrower: {}", borrower.getName());

        IncomeCalculation calculation = new IncomeCalculation();

        // Get the most recent paystub for YTD data
        Paystub mostRecentStub = borrower.getMostRecentPaystub();
        if (mostRecentStub == null) {
            throw new IllegalArgumentException("Borrower must have at least one paystub");
        }

        // Calculate expected monthly income
        BigDecimal expectedMonthlyIncome = calculateExpectedMonthlyIncome(borrower);
        calculation.setExpectedMonthlyIncome(expectedMonthlyIncome);
        logger.debug("Expected monthly income: {}", expectedMonthlyIncome);

        // Calculate YTD monthly pacing
        BigDecimal ytdPacing = calculateYtdMonthlyPacing(borrower, mostRecentStub);
        calculation.setYtdMonthlyPacing(ytdPacing);

        // Determine number of paychecks YTD
        int paychecksYtd = determinePaychecksYtd(mostRecentStub, borrower.getPayFrequency());
        calculation.setNumberOfPaychecksYtd(paychecksYtd);
        logger.debug("YTD monthly pacing: {}, Paychecks YTD: {}", ytdPacing, paychecksYtd);

        // Calculate variance
        BigDecimal variance = calculateVariancePercentage(expectedMonthlyIncome, ytdPacing);
        calculation.setVariancePercentage(variance);
        logger.debug("Variance percentage: {}%", variance);

        // Apply guardrail logic to determine recommended income
        applyGuardrailLogic(calculation, expectedMonthlyIncome, ytdPacing, variance);

        // Calculate variable income
        calculateVariableIncome(calculation, mostRecentStub);

        // Perform validations and add warnings
        validateCalculation(calculation, borrower, mostRecentStub);

        logger.info("Income calculation completed. Total qualified income: {}",
                    calculation.getTotalQualifiedMonthlyIncome());

        return calculation;
    }

    /**
     * Calculates the expected monthly income based on employment type.
     *
     * <p>Formula for hourly employees:
     * <pre>
     * Hours per Pay Period × Pay Periods per Year × Hourly Rate ÷ 12
     * </pre>
     *
     * <p>Formula for salaried employees:
     * <pre>
     * Pay per Period × Pay Periods per Year ÷ 12
     * </pre>
     *
     * @param borrower the borrower with employment information
     * @return the expected monthly income
     */
    public BigDecimal calculateExpectedMonthlyIncome(Borrower borrower) {
        if (borrower.getPayFrequency() == null) {
            throw new IllegalArgumentException("Pay frequency is required");
        }

        int periodsPerYear = borrower.getPayFrequency().getPeriodsPerYear();

        if (borrower.isHourly()) {
            return calculateHourlyMonthlyIncome(borrower, periodsPerYear);
        } else if (borrower.isSalaried()) {
            return calculateSalariedMonthlyIncome(borrower, periodsPerYear);
        } else {
            throw new IllegalArgumentException("Employment type must be HOURLY or SALARY");
        }
    }

    /**
     * Calculates monthly income for hourly employees.
     *
     * @param borrower the borrower
     * @param periodsPerYear pay periods per year
     * @return monthly income
     */
    private BigDecimal calculateHourlyMonthlyIncome(Borrower borrower, int periodsPerYear) {
        if (borrower.getHourlyRate() == null || borrower.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hourly rate is required for hourly employees");
        }

        // Get hours from most recent paystub, or use standard hours
        Paystub mostRecent = borrower.getMostRecentPaystub();
        BigDecimal hoursPerPeriod;

        if (mostRecent != null && mostRecent.getHoursWorked() != null &&
            mostRecent.getHoursWorked().compareTo(BigDecimal.ZERO) > 0) {
            hoursPerPeriod = mostRecent.getHoursWorked();
        } else {
            // Default to 40 hours/week for standard calculations
            hoursPerPeriod = getStandardHoursForFrequency(borrower.getPayFrequency());
        }

        // Hours per Period × Pay Periods per Year × Hourly Rate ÷ 12
        BigDecimal annualIncome = hoursPerPeriod
                .multiply(new BigDecimal(periodsPerYear))
                .multiply(borrower.getHourlyRate());

        return annualIncome.divide(MONTHS_PER_YEAR, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculates monthly income for salaried employees.
     *
     * @param borrower the borrower
     * @param periodsPerYear pay periods per year
     * @return monthly income
     */
    private BigDecimal calculateSalariedMonthlyIncome(Borrower borrower, int periodsPerYear) {
        if (borrower.getSalary() == null || borrower.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salary per period is required for salaried employees");
        }

        // Pay per Period × Pay Periods per Year ÷ 12
        BigDecimal annualSalary = borrower.getSalary()
                .multiply(new BigDecimal(periodsPerYear));

        return annualSalary.divide(MONTHS_PER_YEAR, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Gets standard hours for a pay frequency (40-hour work week).
     *
     * @param frequency the pay frequency
     * @return standard hours for the period
     */
    private BigDecimal getStandardHoursForFrequency(PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> new BigDecimal("40");
            case BI_WEEKLY -> new BigDecimal("80");
            case SEMI_MONTHLY -> new BigDecimal("86.67"); // ~40 hours/week
            case MONTHLY -> new BigDecimal("173.33"); // ~40 hours/week
        };
    }

    /**
     * Calculates the YTD monthly pacing based on actual year-to-date earnings.
     *
     * <p>Formula:
     * <pre>
     * YTD Base Wages ÷ Number of Stubs YTD × Pay Periods per Year ÷ 12
     * </pre>
     *
     * @param borrower the borrower
     * @param mostRecentStub the most recent paystub with YTD data
     * @return the YTD monthly pacing
     */
    public BigDecimal calculateYtdMonthlyPacing(Borrower borrower, Paystub mostRecentStub) {
        if (mostRecentStub == null) {
            throw new IllegalArgumentException("Most recent paystub is required");
        }

        // Sum all base wage YTD earnings from the most recent stub
        BigDecimal ytdBaseWages = mostRecentStub.getBaseWageEarnings().stream()
                .map(Earning::getYtdAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (ytdBaseWages.compareTo(BigDecimal.ZERO) == 0) {
            logger.warn("No base wage earnings found in YTD totals");
            return BigDecimal.ZERO;
        }

        int paychecksYtd = determinePaychecksYtd(mostRecentStub, borrower.getPayFrequency());
        if (paychecksYtd == 0) {
            throw new IllegalArgumentException("Cannot determine number of paychecks YTD");
        }

        int periodsPerYear = borrower.getPayFrequency().getPeriodsPerYear();

        // YTD Base Wages ÷ Number of Stubs YTD × Pay Periods per Year ÷ 12
        BigDecimal averagePerPaycheck = ytdBaseWages.divide(
                new BigDecimal(paychecksYtd), CALCULATION_SCALE, RoundingMode.HALF_UP);
        BigDecimal annualPacing = averagePerPaycheck.multiply(new BigDecimal(periodsPerYear));

        return annualPacing.divide(MONTHS_PER_YEAR, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Determines the number of paychecks received year-to-date based on the pay date
     * and pay frequency.
     *
     * @param mostRecentStub the most recent paystub
     * @param payFrequency the pay frequency
     * @return the number of paychecks YTD
     */
    public int determinePaychecksYtd(Paystub mostRecentStub, PayFrequency payFrequency) {
        LocalDate payDate = mostRecentStub.getPayDate();
        if (payDate == null) {
            throw new IllegalArgumentException("Pay date is required");
        }

        // Calculate from January 1st of the pay date's year to the pay date
        LocalDate startOfYear = LocalDate.of(payDate.getYear(), 1, 1);

        // Estimate based on frequency
        long daysSinceStartOfYear = ChronoUnit.DAYS.between(startOfYear, payDate);

        int estimatedPaychecks = switch (payFrequency) {
            case WEEKLY -> (int) (daysSinceStartOfYear / 7) + 1;
            case BI_WEEKLY -> (int) (daysSinceStartOfYear / 14) + 1;
            case SEMI_MONTHLY -> (int) (payDate.getMonthValue() * 2);
            case MONTHLY -> payDate.getMonthValue();
        };

        return estimatedPaychecks;
    }

    /**
     * Calculates the variance percentage between YTD pacing and expected income.
     *
     * <p>Formula:
     * <pre>
     * (YTD Pacing - Expected) ÷ Expected × 100
     * </pre>
     *
     * <p>Positive percentage means exceeding expected, negative means under-earning.
     *
     * @param expectedIncome the expected monthly income
     * @param ytdPacing the YTD monthly pacing
     * @return the variance percentage
     */
    public BigDecimal calculateVariancePercentage(BigDecimal expectedIncome, BigDecimal ytdPacing) {
        if (expectedIncome == null || expectedIncome.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Expected income must be greater than zero");
        }

        // (YTD - Expected) / Expected * 100
        BigDecimal difference = ytdPacing.subtract(expectedIncome);
        BigDecimal variance = difference.divide(expectedIncome, CALCULATION_SCALE, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return variance.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Applies guardrail logic to determine the recommended usable base income.
     *
     * <p>Rules:
     * <ul>
     *   <li>If YTD exceeds expected → use Expected</li>
     *   <li>0-5% low → use Expected (verbal explanation acceptable)</li>
     *   <li>5-10% low → use Expected only with documented explanation, else use YTD</li>
     *   <li>>10% low → use YTD unless clearly documented reason exists</li>
     * </ul>
     *
     * @param calculation the calculation object to update
     * @param expectedIncome the expected monthly income
     * @param ytdPacing the YTD monthly pacing
     * @param variance the variance percentage
     */
    public void applyGuardrailLogic(IncomeCalculation calculation, BigDecimal expectedIncome,
                                    BigDecimal ytdPacing, BigDecimal variance) {

        if (variance.compareTo(BigDecimal.ZERO) >= 0) {
            // YTD exceeds expected - use Expected
            calculation.setRecommendedUsableBaseIncome(expectedIncome);
            calculation.setExplanation("YTD pacing exceeds expected income. Using expected monthly income per guidelines.");
            logger.info("Guardrail: YTD exceeds expected, using expected income");

        } else if (variance.compareTo(ACCEPTABLE_VARIANCE_THRESHOLD) >= 0) {
            // 0-5% low - use Expected (within acceptable range)
            calculation.setRecommendedUsableBaseIncome(expectedIncome);
            calculation.setExplanation("YTD pacing is within acceptable range (0-5% low). " +
                    "Using expected monthly income. Verbal explanation is acceptable.");
            logger.info("Guardrail: Within acceptable range ({}%), using expected income", variance);

        } else if (variance.compareTo(DOCUMENTED_EXPLANATION_THRESHOLD) >= 0) {
            // 5-10% low - requires documented explanation
            // Default to YTD unless explanation is provided
            calculation.setRecommendedUsableBaseIncome(ytdPacing);
            calculation.setExplanation("YTD pacing is 5-10% below expected. " +
                    "Using YTD pacing. If documented explanation exists for lower pacing, " +
                    "expected income may be used instead.");
            calculation.addWarning("Income is 5-10% below expected. Documented explanation required to use expected income.");
            logger.warn("Guardrail: 5-10% low ({}%), using YTD, documented explanation needed for expected", variance);

        } else {
            // >10% low - use YTD
            calculation.setRecommendedUsableBaseIncome(ytdPacing);
            calculation.setExplanation("YTD pacing is more than 10% below expected. " +
                    "Using YTD pacing per guidelines. Clearly documented reason required to use expected income.");
            calculation.addWarning("Income is significantly below expected (>10%). Using YTD pacing.");
            logger.warn("Guardrail: Significant variance ({}%), using YTD income", variance);
        }
    }

    /**
     * Calculates variable income (overtime, commission, bonus) using appropriate methods.
     *
     * <p>Currently uses YTD pacing for variable income. Future enhancement: 24-month averaging.
     *
     * @param calculation the calculation object to update
     * @param mostRecentStub the most recent paystub
     */
    public void calculateVariableIncome(IncomeCalculation calculation, Paystub mostRecentStub) {
        List<Earning> variableEarnings = mostRecentStub.getVariableEarnings();

        if (variableEarnings.isEmpty()) {
            logger.debug("No variable income earnings found");
            return;
        }

        for (Earning earning : variableEarnings) {
            BigDecimal ytdAmount = earning.getYtdAmount();

            if (ytdAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Simple YTD-based calculation (conservative approach)
                // For production: implement 24-month averaging with trend analysis
                BigDecimal monthlyAmount = calculateVariableIncomeMonthly(ytdAmount, mostRecentStub.getPayDate());

                calculation.addVariableIncome(earning.getPayTypeName(), monthlyAmount);
                logger.debug("Variable income {}: YTD ${}, Monthly ${}",
                           earning.getPayTypeName(), ytdAmount, monthlyAmount);
            }
        }
    }

    /**
     * Calculates monthly variable income from YTD amount.
     * Uses conservative YTD pacing approach.
     *
     * @param ytdAmount the YTD amount for the variable income type
     * @param asOfDate the date of the paystub
     * @return the monthly amount
     */
    private BigDecimal calculateVariableIncomeMonthly(BigDecimal ytdAmount, LocalDate asOfDate) {
        if (asOfDate == null) {
            throw new IllegalArgumentException("As-of date is required");
        }

        // Calculate months elapsed in the year
        int monthsElapsed = asOfDate.getMonthValue();

        if (monthsElapsed == 0) {
            return BigDecimal.ZERO;
        }

        // YTD Amount ÷ Months Elapsed (conservative approach)
        return ytdAmount.divide(new BigDecimal(monthsElapsed), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Validates the borrower has all required information.
     *
     * @param borrower the borrower to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBorrower(Borrower borrower) {
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null");
        }
        if (borrower.getName() == null || borrower.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Borrower name is required");
        }
        if (borrower.getEmploymentType() == null) {
            throw new IllegalArgumentException("Employment type is required");
        }
        if (borrower.getPayFrequency() == null) {
            throw new IllegalArgumentException("Pay frequency is required");
        }
        if (borrower.getPaystubs() == null || borrower.getPaystubs().isEmpty()) {
            throw new IllegalArgumentException("At least one paystub is required");
        }
    }

    /**
     * Validates the calculation and adds warnings for potential issues.
     *
     * @param calculation the calculation to validate
     * @param borrower the borrower
     * @param mostRecentStub the most recent paystub
     */
    private void validateCalculation(IncomeCalculation calculation, Borrower borrower,
                                     Paystub mostRecentStub) {

        // Check for name consistency
        if (mostRecentStub.getEmployeeName() != null &&
            !mostRecentStub.getEmployeeName().trim().isEmpty() &&
            !mostRecentStub.getEmployeeName().equalsIgnoreCase(borrower.getName())) {
            calculation.addWarning("Employee name on paystub does not match borrower name");
        }

        // Check for employer consistency
        if (mostRecentStub.getEmployerName() != null &&
            !mostRecentStub.getEmployerName().trim().isEmpty() &&
            !mostRecentStub.getEmployerName().equalsIgnoreCase(borrower.getEmployerName())) {
            calculation.addWarning("Employer name on paystub does not match borrower employer");
        }

        // Check for pay frequency consistency
        if (mostRecentStub.getPayFrequency() != null &&
            mostRecentStub.getPayFrequency() != borrower.getPayFrequency()) {
            calculation.addWarning("Pay frequency on paystub does not match borrower pay frequency");
        }

        // Validate YTD totals are reasonable
        BigDecimal totalYtdEarnings = mostRecentStub.getTotalYtdEarnings();
        BigDecimal expectedYtdTotal = calculation.getExpectedMonthlyIncome()
                .multiply(new BigDecimal(mostRecentStub.getPayDate().getMonthValue()));

        // If YTD is less than 50% of expected, flag it
        BigDecimal fiftyPercent = expectedYtdTotal.multiply(new BigDecimal("0.5"));
        if (totalYtdEarnings.compareTo(fiftyPercent) < 0) {
            calculation.addWarning("YTD earnings appear unusually low compared to expected totals");
        }

        logger.debug("Validation completed. Total warnings: {}", calculation.getWarnings().size());
    }
}
