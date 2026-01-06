package com.mortgage.paystub.calculator;

import com.mortgage.paystub.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for IncomeCalculator.
 * Tests all calculation methods with various scenarios from the Income Worksheet.
 *
 * @author James Bennett
 * @version 1.0
 */
class IncomeCalculatorTest {

    private IncomeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IncomeCalculator();
    }

    // ==================== EXPECTED MONTHLY INCOME TESTS ====================

    @Test
    void testCalculateExpectedMonthlyIncome_Hourly_BiWeekly() {
        // Scenario 1: Hourly employee, bi-weekly, $20/hr, 80 hours per period
        // Expected: 80 × 26 × $20 ÷ 12 = $3,466.67
        Borrower borrower = createHourlyBorrower(new BigDecimal("20.00"), PayFrequency.BI_WEEKLY);

        // Create paystub with 80 hours
        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 1, 5));
        paystub.setHoursWorked(new BigDecimal("80.00"));
        borrower.addPaystub(paystub);

        BigDecimal expectedIncome = calculator.calculateExpectedMonthlyIncome(borrower);

        assertEquals(new BigDecimal("3466.67"), expectedIncome);
    }

    @Test
    void testCalculateExpectedMonthlyIncome_Hourly_Weekly() {
        // Weekly employee, $15/hr, 40 hours per week
        // Expected: 40 × 52 × $15 ÷ 12 = $2,600.00
        Borrower borrower = createHourlyBorrower(new BigDecimal("15.00"), PayFrequency.WEEKLY);

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 1, 5));
        paystub.setHoursWorked(new BigDecimal("40.00"));
        borrower.addPaystub(paystub);

        BigDecimal expectedIncome = calculator.calculateExpectedMonthlyIncome(borrower);

        assertEquals(new BigDecimal("2600.00"), expectedIncome);
    }

    @Test
    void testCalculateExpectedMonthlyIncome_Salary_SemiMonthly() {
        // Scenario 2: Salary employee, semi-monthly, $1,750 per period
        // Expected: $1,750 × 24 ÷ 12 = $3,500.00
        Borrower borrower = createSalariedBorrower(new BigDecimal("1750.00"), PayFrequency.SEMI_MONTHLY);
        borrower.addPaystub(createPaystub(borrower, LocalDate.of(2026, 1, 5)));

        BigDecimal expectedIncome = calculator.calculateExpectedMonthlyIncome(borrower);

        assertEquals(new BigDecimal("3500.00"), expectedIncome);
    }

    @Test
    void testCalculateExpectedMonthlyIncome_Salary_Monthly() {
        // Monthly salary, $4,000 per month
        // Expected: $4,000 × 12 ÷ 12 = $4,000.00
        Borrower borrower = createSalariedBorrower(new BigDecimal("4000.00"), PayFrequency.MONTHLY);
        borrower.addPaystub(createPaystub(borrower, LocalDate.of(2026, 1, 5)));

        BigDecimal expectedIncome = calculator.calculateExpectedMonthlyIncome(borrower);

        assertEquals(new BigDecimal("4000.00"), expectedIncome);
    }

    // ==================== YTD MONTHLY PACING TESTS ====================

    @Test
    void testCalculateYtdMonthlyPacing_OnTrack() {
        // Hourly employee with 13 paychecks YTD, bi-weekly
        // YTD: $18,000, Expected monthly: $3,466.67
        // YTD Pacing: $18,000 ÷ 13 × 26 ÷ 12 = $3,000.00
        Borrower borrower = createHourlyBorrower(new BigDecimal("20.00"), PayFrequency.BI_WEEKLY);

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 7, 1)); // Mid-year
        paystub.setHoursWorked(new BigDecimal("80.00"));

        // Add base wage earning with YTD
        Earning regularPay = new Earning("Regular", PayCategory.BASE_WAGE,
                new BigDecimal("1600.00"), new BigDecimal("18000.00"));
        paystub.addEarning(regularPay);

        borrower.addPaystub(paystub);

        BigDecimal ytdPacing = calculator.calculateYtdMonthlyPacing(borrower, paystub);

        // $18,000 / 13 = $1,384.615... per check
        // $1,384.615... × 26 = $36,000 annual
        // $36,000 / 12 = $3,000.00 monthly
        assertEquals(new BigDecimal("3000.00"), ytdPacing);
    }

    @Test
    void testCalculateYtdMonthlyPacing_UnderEarning() {
        // Salary employee with 10 paychecks YTD, semi-monthly
        // YTD: $16,000 (expected $17,500), variance should be -8.6%
        Borrower borrower = createSalariedBorrower(new BigDecimal("1750.00"), PayFrequency.SEMI_MONTHLY);

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 5, 15)); // 5 months in
        Earning salaryPay = new Earning("Salary", PayCategory.BASE_WAGE,
                new BigDecimal("1750.00"), new BigDecimal("16000.00"));
        paystub.addEarning(salaryPay);

        borrower.addPaystub(paystub);

        BigDecimal ytdPacing = calculator.calculateYtdMonthlyPacing(borrower, paystub);

        // $16,000 / 10 = $1,600 per check
        // $1,600 × 24 = $38,400 annual
        // $38,400 / 12 = $3,200.00 monthly
        assertEquals(new BigDecimal("3200.00"), ytdPacing);
    }

    // ==================== VARIANCE CALCULATION TESTS ====================

    @Test
    void testCalculateVariancePercentage_Positive() {
        // YTD exceeds expected
        BigDecimal expected = new BigDecimal("3500.00");
        BigDecimal ytd = new BigDecimal("3700.00");

        BigDecimal variance = calculator.calculateVariancePercentage(expected, ytd);

        // (3700 - 3500) / 3500 * 100 = 5.71%
        assertEquals(new BigDecimal("5.71"), variance);
    }

    @Test
    void testCalculateVariancePercentage_WithinAcceptableRange() {
        // 3% below expected
        BigDecimal expected = new BigDecimal("3466.67");
        BigDecimal ytd = new BigDecimal("3362.67");

        BigDecimal variance = calculator.calculateVariancePercentage(expected, ytd);

        // Should be approximately -3%
        assertTrue(variance.compareTo(new BigDecimal("-3.1")) > 0);
        assertTrue(variance.compareTo(new BigDecimal("-2.9")) < 0);
    }

    @Test
    void testCalculateVariancePercentage_RequiresDocumentation() {
        // 8.6% below expected (Scenario 2 from requirements)
        BigDecimal expected = new BigDecimal("3500.00");
        BigDecimal ytd = new BigDecimal("3200.00");

        BigDecimal variance = calculator.calculateVariancePercentage(expected, ytd);

        // (3200 - 3500) / 3500 * 100 = -8.57%
        assertEquals(new BigDecimal("-8.57"), variance);
    }

    @Test
    void testCalculateVariancePercentage_SignificantVariance() {
        // More than 10% below
        BigDecimal expected = new BigDecimal("4000.00");
        BigDecimal ytd = new BigDecimal("3500.00");

        BigDecimal variance = calculator.calculateVariancePercentage(expected, ytd);

        // (3500 - 4000) / 4000 * 100 = -12.5%
        assertEquals(new BigDecimal("-12.50"), variance);
    }

    // ==================== GUARDRAIL LOGIC TESTS ====================

    @Test
    void testGuardrailLogic_YtdExceedsExpected() {
        IncomeCalculation calc = new IncomeCalculation();
        BigDecimal expected = new BigDecimal("3000.00");
        BigDecimal ytd = new BigDecimal("3200.00");
        BigDecimal variance = new BigDecimal("6.67");

        calculator.applyGuardrailLogic(calc, expected, ytd, variance);

        assertEquals(expected, calc.getRecommendedUsableBaseIncome());
        assertTrue(calc.getExplanation().contains("exceeds expected"));
    }

    @Test
    void testGuardrailLogic_WithinAcceptableRange() {
        // 0-5% low: use Expected
        IncomeCalculation calc = new IncomeCalculation();
        BigDecimal expected = new BigDecimal("3466.67");
        BigDecimal ytd = new BigDecimal("3362.67");
        BigDecimal variance = new BigDecimal("-3.00");

        calculator.applyGuardrailLogic(calc, expected, ytd, variance);

        assertEquals(expected, calc.getRecommendedUsableBaseIncome());
        assertTrue(calc.getExplanation().contains("acceptable range"));
        assertTrue(calc.getExplanation().contains("0-5%"));
    }

    @Test
    void testGuardrailLogic_RequiresDocumentation() {
        // 5-10% low: use YTD (default), but can use Expected with documentation
        IncomeCalculation calc = new IncomeCalculation();
        BigDecimal expected = new BigDecimal("3500.00");
        BigDecimal ytd = new BigDecimal("3200.00");
        BigDecimal variance = new BigDecimal("-8.57");

        calculator.applyGuardrailLogic(calc, expected, ytd, variance);

        // Should use YTD by default
        assertEquals(ytd, calc.getRecommendedUsableBaseIncome());
        assertTrue(calc.getExplanation().contains("5-10%"));
        assertTrue(calc.hasWarnings());
    }

    @Test
    void testGuardrailLogic_SignificantVariance() {
        // >10% low: use YTD
        IncomeCalculation calc = new IncomeCalculation();
        BigDecimal expected = new BigDecimal("4000.00");
        BigDecimal ytd = new BigDecimal("3500.00");
        BigDecimal variance = new BigDecimal("-12.50");

        calculator.applyGuardrailLogic(calc, expected, ytd, variance);

        assertEquals(ytd, calc.getRecommendedUsableBaseIncome());
        assertTrue(calc.getExplanation().contains("more than 10%"));
        assertTrue(calc.hasWarnings());
    }

    // ==================== PAYCHECKS YTD TESTS ====================

    @Test
    void testDeterminePaychecksYtd_BiWeekly() {
        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 7, 1)); // July 1st

        int paychecks = calculator.determinePaychecksYtd(paystub, PayFrequency.BI_WEEKLY);

        // Approximately 26 weeks / 2 = 13 paychecks
        assertTrue(paychecks >= 12 && paychecks <= 14);
    }

    @Test
    void testDeterminePaychecksYtd_SemiMonthly() {
        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 5, 15)); // May 15th

        int paychecks = calculator.determinePaychecksYtd(paystub, PayFrequency.SEMI_MONTHLY);

        // 5 months × 2 = 10 paychecks
        assertEquals(10, paychecks);
    }

    @Test
    void testDeterminePaychecksYtd_Monthly() {
        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 3, 31)); // March 31st

        int paychecks = calculator.determinePaychecksYtd(paystub, PayFrequency.MONTHLY);

        // 3 paychecks
        assertEquals(3, paychecks);
    }

    // ==================== VARIABLE INCOME TESTS ====================

    @Test
    void testCalculateVariableIncome() {
        IncomeCalculation calc = new IncomeCalculation();

        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 6, 1)); // June 1st (6 months in)

        // Add overtime earning
        Earning overtime = new Earning("Overtime", PayCategory.VARIABLE,
                new BigDecimal("150.00"), new BigDecimal("900.00"));
        paystub.addEarning(overtime);

        // Add commission earning
        Earning commission = new Earning("Commission", PayCategory.VARIABLE,
                new BigDecimal("500.00"), new BigDecimal("3000.00"));
        paystub.addEarning(commission);

        calculator.calculateVariableIncome(calc, paystub);

        // Overtime: $900 / 6 months = $150/month
        // Commission: $3000 / 6 months = $500/month
        assertEquals(new BigDecimal("150.00"), calc.getVariableIncomeBreakdown().get("Overtime"));
        assertEquals(new BigDecimal("500.00"), calc.getVariableIncomeBreakdown().get("Commission"));
        assertEquals(new BigDecimal("650.00"), calc.getTotalVariableIncome());
    }

    @Test
    void testCalculateVariableIncome_NoVariableEarnings() {
        IncomeCalculation calc = new IncomeCalculation();

        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 6, 1));

        // Only base wage, no variable
        Earning regular = new Earning("Regular", PayCategory.BASE_WAGE,
                new BigDecimal("2000.00"), new BigDecimal("12000.00"));
        paystub.addEarning(regular);

        calculator.calculateVariableIncome(calc, paystub);

        assertEquals(BigDecimal.ZERO, calc.getTotalVariableIncome());
    }

    // ==================== FULL INTEGRATION TESTS ====================

    @Test
    void testCalculateIncome_CompleteScenario_OnTrack() {
        // Scenario 1: Hourly employee, on track
        Borrower borrower = createHourlyBorrower(new BigDecimal("20.00"), PayFrequency.BI_WEEKLY);
        borrower.setName("John Doe");
        borrower.setEmployerName("ABC Company");

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 7, 1));
        paystub.setHoursWorked(new BigDecimal("80.00"));
        paystub.setEmployeeName("John Doe");
        paystub.setEmployerName("ABC Company");

        // Regular pay: Current $1600, YTD $18,000 (13 checks)
        Earning regular = new Earning("Regular", PayCategory.BASE_WAGE,
                new BigDecimal("1600.00"), new BigDecimal("18000.00"));
        paystub.addEarning(regular);

        // Some overtime
        Earning overtime = new Earning("Overtime", PayCategory.VARIABLE,
                new BigDecimal("100.00"), new BigDecimal("700.00"));
        paystub.addEarning(overtime);

        borrower.addPaystub(paystub);

        IncomeCalculation result = calculator.calculateIncome(borrower);

        assertNotNull(result);
        assertEquals(new BigDecimal("3466.67"), result.getExpectedMonthlyIncome());
        assertEquals(new BigDecimal("3000.00"), result.getYtdMonthlyPacing());
        // Variance should be approximately -13.46% (negative)
        assertTrue(result.getVariancePercentage().compareTo(new BigDecimal("-14")) > 0); // Greater than -14 (less negative)
        assertTrue(result.getVariancePercentage().compareTo(new BigDecimal("-13")) < 0); // Less than -13 (more negative)
        // Should use YTD due to >10% variance
        assertEquals(new BigDecimal("3000.00"), result.getRecommendedUsableBaseIncome());
        assertTrue(result.getTotalVariableIncome().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateIncome_CompleteScenario_UnderEarning() {
        // Scenario 2: Salary employee, under-earning (8.6% low)
        Borrower borrower = createSalariedBorrower(new BigDecimal("1750.00"), PayFrequency.SEMI_MONTHLY);
        borrower.setName("Jane Smith");
        borrower.setEmployerName("XYZ Corp");

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 5, 15));
        paystub.setEmployeeName("Jane Smith");
        paystub.setEmployerName("XYZ Corp");

        Earning salary = new Earning("Salary", PayCategory.BASE_WAGE,
                new BigDecimal("1750.00"), new BigDecimal("16000.00"));
        paystub.addEarning(salary);

        borrower.addPaystub(paystub);

        IncomeCalculation result = calculator.calculateIncome(borrower);

        assertNotNull(result);
        assertEquals(new BigDecimal("3500.00"), result.getExpectedMonthlyIncome());
        assertEquals(new BigDecimal("3200.00"), result.getYtdMonthlyPacing());
        assertEquals(new BigDecimal("-8.57"), result.getVariancePercentage());
        // Should use YTD (5-10% variance)
        assertEquals(new BigDecimal("3200.00"), result.getRecommendedUsableBaseIncome());
        assertTrue(result.hasWarnings());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void testValidation_MissingBorrower() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateIncome(null));
    }

    @Test
    void testValidation_MissingPaystubs() {
        Borrower borrower = createHourlyBorrower(new BigDecimal("20.00"), PayFrequency.BI_WEEKLY);
        // No paystubs added

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateIncome(borrower));
    }

    @Test
    void testValidation_MissingHourlyRate() {
        Borrower borrower = new Borrower("Test", "Company", EmploymentType.HOURLY, PayFrequency.BI_WEEKLY);
        // No hourly rate set
        borrower.addPaystub(createPaystub(borrower, LocalDate.now()));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateExpectedMonthlyIncome(borrower));
    }

    @Test
    void testValidation_MissingSalary() {
        Borrower borrower = new Borrower("Test", "Company", EmploymentType.SALARY, PayFrequency.MONTHLY);
        // No salary set
        borrower.addPaystub(createPaystub(borrower, LocalDate.now()));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateExpectedMonthlyIncome(borrower));
    }

    @Test
    void testValidation_NameMismatch() {
        Borrower borrower = createSalariedBorrower(new BigDecimal("2000.00"), PayFrequency.MONTHLY);
        borrower.setName("John Doe");

        Paystub paystub = createPaystub(borrower, LocalDate.of(2026, 1, 31));
        paystub.setEmployeeName("Jane Smith"); // Different name

        Earning salary = new Earning("Salary", PayCategory.BASE_WAGE,
                new BigDecimal("2000.00"), new BigDecimal("2000.00"));
        paystub.addEarning(salary);

        borrower.addPaystub(paystub);

        IncomeCalculation result = calculator.calculateIncome(borrower);

        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(w -> w.contains("name") && w.contains("does not match")));
    }

    // ==================== HELPER METHODS ====================

    private Borrower createHourlyBorrower(BigDecimal hourlyRate, PayFrequency frequency) {
        Borrower borrower = new Borrower();
        borrower.setName("Test Employee");
        borrower.setEmployerName("Test Company");
        borrower.setEmploymentType(EmploymentType.HOURLY);
        borrower.setPayFrequency(frequency);
        borrower.setHourlyRate(hourlyRate);
        return borrower;
    }

    private Borrower createSalariedBorrower(BigDecimal salary, PayFrequency frequency) {
        Borrower borrower = new Borrower();
        borrower.setName("Test Employee");
        borrower.setEmployerName("Test Company");
        borrower.setEmploymentType(EmploymentType.SALARY);
        borrower.setPayFrequency(frequency);
        borrower.setSalary(salary);
        return borrower;
    }

    private Paystub createPaystub(Borrower borrower, LocalDate payDate) {
        Paystub paystub = new Paystub();
        paystub.setPayDate(payDate);
        paystub.setPayFrequency(borrower.getPayFrequency());
        paystub.setEmployeeName(borrower.getName());
        paystub.setEmployerName(borrower.getEmployerName());
        return paystub;
    }
}
