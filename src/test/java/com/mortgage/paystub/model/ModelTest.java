package com.mortgage.paystub.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify all data models compile and work correctly.
 *
 * @author James Bennett
 * @version 1.0
 */
class ModelTest {

    @Test
    void testPayFrequencyEnum() {
        assertEquals(52, PayFrequency.WEEKLY.getPeriodsPerYear());
        assertEquals(26, PayFrequency.BI_WEEKLY.getPeriodsPerYear());
        assertEquals(24, PayFrequency.SEMI_MONTHLY.getPeriodsPerYear());
        assertEquals(12, PayFrequency.MONTHLY.getPeriodsPerYear());

        assertEquals("Bi-Weekly", PayFrequency.BI_WEEKLY.getDisplayName());
        assertTrue(PayFrequency.BI_WEEKLY.toString().contains("26"));
    }

    @Test
    void testEmploymentTypeEnum() {
        assertEquals("Hourly", EmploymentType.HOURLY.getDisplayName());
        assertEquals("Salary", EmploymentType.SALARY.getDisplayName());
    }

    @Test
    void testPayCategoryEnum() {
        assertEquals("Base Wage", PayCategory.BASE_WAGE.getDisplayName());
        assertEquals("Variable Income", PayCategory.VARIABLE.getDisplayName());
        assertEquals("Other", PayCategory.OTHER.getDisplayName());
        assertNotNull(PayCategory.BASE_WAGE.getDescription());
    }

    @Test
    void testEarningModel() {
        Earning earning = new Earning("Regular", PayCategory.BASE_WAGE,
                new BigDecimal("2000.00"), new BigDecimal("10000.00"));

        assertEquals("Regular", earning.getPayTypeName());
        assertEquals(PayCategory.BASE_WAGE, earning.getCategory());
        assertEquals(new BigDecimal("2000.00"), earning.getCurrentAmount());
        assertEquals(new BigDecimal("10000.00"), earning.getYtdAmount());
        assertTrue(earning.isBaseWage());
        assertFalse(earning.isVariableIncome());
    }

    @Test
    void testDeductionModel() {
        Deduction deduction = new Deduction("Federal Tax",
                new BigDecimal("300.00"), new BigDecimal("1500.00"));

        assertEquals("Federal Tax", deduction.getDeductionName());
        assertEquals(new BigDecimal("300.00"), deduction.getCurrentAmount());
        assertEquals(new BigDecimal("1500.00"), deduction.getYtdAmount());
    }

    @Test
    void testPaystubModel() {
        Paystub paystub = new Paystub();
        paystub.setEmployeeName("John Doe");
        paystub.setEmployerName("ABC Company");
        paystub.setPayFrequency(PayFrequency.BI_WEEKLY);
        paystub.setPayDate(LocalDate.of(2026, 1, 5));
        paystub.setPayPeriodStartDate(LocalDate.of(2025, 12, 22));
        paystub.setPayPeriodEndDate(LocalDate.of(2026, 1, 4));

        Earning earning1 = new Earning("Regular", PayCategory.BASE_WAGE,
                new BigDecimal("1600.00"), new BigDecimal("8000.00"));
        Earning earning2 = new Earning("Overtime", PayCategory.VARIABLE,
                new BigDecimal("200.00"), new BigDecimal("800.00"));

        paystub.addEarning(earning1);
        paystub.addEarning(earning2);

        assertEquals(2, paystub.getEarnings().size());
        assertEquals(new BigDecimal("1800.00"), paystub.getTotalCurrentEarnings());
        assertEquals(new BigDecimal("8800.00"), paystub.getTotalYtdEarnings());
        assertEquals(1, paystub.getBaseWageEarnings().size());
        assertEquals(1, paystub.getVariableEarnings().size());
    }

    @Test
    void testBorrowerModel() {
        Borrower borrower = new Borrower("Jane Smith", "XYZ Corp",
                EmploymentType.HOURLY, PayFrequency.BI_WEEKLY);
        borrower.setHourlyRate(new BigDecimal("25.00"));

        assertEquals("Jane Smith", borrower.getName());
        assertEquals("XYZ Corp", borrower.getEmployerName());
        assertEquals(EmploymentType.HOURLY, borrower.getEmploymentType());
        assertTrue(borrower.isHourly());
        assertFalse(borrower.isSalaried());
        assertEquals(new BigDecimal("25.00"), borrower.getHourlyRate());

        Paystub paystub = new Paystub();
        paystub.setPayDate(LocalDate.of(2026, 1, 5));
        borrower.addPaystub(paystub);

        assertEquals(1, borrower.getPaystubs().size());
        assertNotNull(borrower.getMostRecentPaystub());
    }

    @Test
    void testIncomeCalculationModel() {
        IncomeCalculation calc = new IncomeCalculation();
        calc.setExpectedMonthlyIncome(new BigDecimal("4333.33"));
        calc.setYtdMonthlyPacing(new BigDecimal("4200.00"));
        calc.setNumberOfPaychecksYtd(5);
        calc.setVariancePercentage(new BigDecimal("-3.08"));
        calc.setRecommendedUsableBaseIncome(new BigDecimal("4333.33"));
        calc.setExplanation("Within acceptable range (0-5% low)");
        calc.addWarning("YTD pacing is slightly below expected");
        calc.addVariableIncome("Overtime", new BigDecimal("200.00"));

        assertEquals(new BigDecimal("4333.33"), calc.getExpectedMonthlyIncome());
        assertEquals(new BigDecimal("4200.00"), calc.getYtdMonthlyPacing());
        assertEquals(5, calc.getNumberOfPaychecksYtd());
        assertEquals(new BigDecimal("4333.33"), calc.getRecommendedUsableBaseIncome());
        assertEquals(new BigDecimal("200.00"), calc.getTotalVariableIncome());
        assertEquals(new BigDecimal("4533.33"), calc.getTotalQualifiedMonthlyIncome());
        assertTrue(calc.hasWarnings());
        assertTrue(calc.isWithinAcceptableRange());
        assertFalse(calc.requiresDocumentedExplanation());
        assertFalse(calc.hasSignificantVariance());
    }

    @Test
    void testIncomeCalculationVarianceCategories() {
        IncomeCalculation calc1 = new IncomeCalculation();
        calc1.setVariancePercentage(new BigDecimal("-2.0"));
        assertTrue(calc1.isWithinAcceptableRange());
        assertFalse(calc1.requiresDocumentedExplanation());
        assertFalse(calc1.hasSignificantVariance());

        IncomeCalculation calc2 = new IncomeCalculation();
        calc2.setVariancePercentage(new BigDecimal("-7.0"));
        assertFalse(calc2.isWithinAcceptableRange());
        assertTrue(calc2.requiresDocumentedExplanation());
        assertFalse(calc2.hasSignificantVariance());

        IncomeCalculation calc3 = new IncomeCalculation();
        calc3.setVariancePercentage(new BigDecimal("-12.0"));
        assertFalse(calc3.isWithinAcceptableRange());
        assertFalse(calc3.requiresDocumentedExplanation());
        assertTrue(calc3.hasSignificantVariance());
    }
}
