package com.mortgage.paystub.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a paystub document containing employee payment information.
 * This includes pay dates, employee/employer information, earnings, and deductions.
 *
 * @author James Bennett
 * @version 1.0
 */
public class Paystub {
    private LocalDate payPeriodStartDate;
    private LocalDate payPeriodEndDate;
    private LocalDate payDate;
    private PayFrequency payFrequency;
    private String employeeName;
    private String employerName;
    private List<Earning> earnings;
    private List<Deduction> deductions;
    private BigDecimal hoursWorked;

    /**
     * Default constructor for Paystub.
     * Initializes earnings and deductions lists.
     */
    public Paystub() {
        this.earnings = new ArrayList<>();
        this.deductions = new ArrayList<>();
        this.hoursWorked = BigDecimal.ZERO;
    }

    /**
     * Constructs a Paystub with basic information.
     *
     * @param payPeriodStartDate the start date of the pay period
     * @param payPeriodEndDate the end date of the pay period
     * @param payDate the date of payment
     * @param payFrequency the frequency of payment
     * @param employeeName the name of the employee
     * @param employerName the name of the employer
     */
    public Paystub(LocalDate payPeriodStartDate, LocalDate payPeriodEndDate, LocalDate payDate,
                   PayFrequency payFrequency, String employeeName, String employerName) {
        this.payPeriodStartDate = payPeriodStartDate;
        this.payPeriodEndDate = payPeriodEndDate;
        this.payDate = payDate;
        this.payFrequency = payFrequency;
        this.employeeName = employeeName;
        this.employerName = employerName;
        this.earnings = new ArrayList<>();
        this.deductions = new ArrayList<>();
        this.hoursWorked = BigDecimal.ZERO;
    }

    /**
     * Gets the start date of the pay period.
     *
     * @return the pay period start date
     */
    public LocalDate getPayPeriodStartDate() {
        return payPeriodStartDate;
    }

    /**
     * Sets the start date of the pay period.
     *
     * @param payPeriodStartDate the pay period start date to set
     */
    public void setPayPeriodStartDate(LocalDate payPeriodStartDate) {
        this.payPeriodStartDate = payPeriodStartDate;
    }

    /**
     * Gets the end date of the pay period.
     *
     * @return the pay period end date
     */
    public LocalDate getPayPeriodEndDate() {
        return payPeriodEndDate;
    }

    /**
     * Sets the end date of the pay period.
     *
     * @param payPeriodEndDate the pay period end date to set
     */
    public void setPayPeriodEndDate(LocalDate payPeriodEndDate) {
        this.payPeriodEndDate = payPeriodEndDate;
    }

    /**
     * Gets the payment date.
     *
     * @return the pay date
     */
    public LocalDate getPayDate() {
        return payDate;
    }

    /**
     * Sets the payment date.
     *
     * @param payDate the pay date to set
     */
    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    /**
     * Gets the pay frequency.
     *
     * @return the pay frequency
     */
    public PayFrequency getPayFrequency() {
        return payFrequency;
    }

    /**
     * Sets the pay frequency.
     *
     * @param payFrequency the pay frequency to set
     */
    public void setPayFrequency(PayFrequency payFrequency) {
        this.payFrequency = payFrequency;
    }

    /**
     * Gets the employee name.
     *
     * @return the employee name
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * Sets the employee name.
     *
     * @param employeeName the employee name to set
     */
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * Gets the employer name.
     *
     * @return the employer name
     */
    public String getEmployerName() {
        return employerName;
    }

    /**
     * Sets the employer name.
     *
     * @param employerName the employer name to set
     */
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    /**
     * Gets the list of earnings on this paystub.
     *
     * @return the list of earnings
     */
    public List<Earning> getEarnings() {
        return earnings;
    }

    /**
     * Sets the list of earnings on this paystub.
     *
     * @param earnings the list of earnings to set
     */
    public void setEarnings(List<Earning> earnings) {
        this.earnings = earnings != null ? earnings : new ArrayList<>();
    }

    /**
     * Adds an earning to this paystub.
     *
     * @param earning the earning to add
     */
    public void addEarning(Earning earning) {
        if (earning != null) {
            this.earnings.add(earning);
        }
    }

    /**
     * Gets the list of deductions on this paystub.
     *
     * @return the list of deductions
     */
    public List<Deduction> getDeductions() {
        return deductions;
    }

    /**
     * Sets the list of deductions on this paystub.
     *
     * @param deductions the list of deductions to set
     */
    public void setDeductions(List<Deduction> deductions) {
        this.deductions = deductions != null ? deductions : new ArrayList<>();
    }

    /**
     * Adds a deduction to this paystub.
     *
     * @param deduction the deduction to add
     */
    public void addDeduction(Deduction deduction) {
        if (deduction != null) {
            this.deductions.add(deduction);
        }
    }

    /**
     * Gets the number of hours worked in this pay period (for hourly employees).
     *
     * @return the hours worked
     */
    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    /**
     * Sets the number of hours worked in this pay period (for hourly employees).
     *
     * @param hoursWorked the hours worked to set
     */
    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked != null ? hoursWorked : BigDecimal.ZERO;
    }

    /**
     * Calculates the total current earnings for this paystub.
     *
     * @return the sum of all current earnings
     */
    public BigDecimal getTotalCurrentEarnings() {
        return earnings.stream()
                .map(Earning::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total YTD earnings for this paystub.
     *
     * @return the sum of all YTD earnings
     */
    public BigDecimal getTotalYtdEarnings() {
        return earnings.stream()
                .map(Earning::getYtdAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total current deductions for this paystub.
     *
     * @return the sum of all current deductions
     */
    public BigDecimal getTotalCurrentDeductions() {
        return deductions.stream()
                .map(Deduction::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total YTD deductions for this paystub.
     *
     * @return the sum of all YTD deductions
     */
    public BigDecimal getTotalYtdDeductions() {
        return deductions.stream()
                .map(Deduction::getYtdAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Gets all base wage earnings from this paystub.
     *
     * @return list of base wage earnings
     */
    public List<Earning> getBaseWageEarnings() {
        return earnings.stream()
                .filter(Earning::isBaseWage)
                .toList();
    }

    /**
     * Gets all variable income earnings from this paystub.
     *
     * @return list of variable income earnings
     */
    public List<Earning> getVariableEarnings() {
        return earnings.stream()
                .filter(Earning::isVariableIncome)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paystub paystub = (Paystub) o;
        return Objects.equals(payPeriodStartDate, paystub.payPeriodStartDate) &&
                Objects.equals(payPeriodEndDate, paystub.payPeriodEndDate) &&
                Objects.equals(payDate, paystub.payDate) &&
                payFrequency == paystub.payFrequency &&
                Objects.equals(employeeName, paystub.employeeName) &&
                Objects.equals(employerName, paystub.employerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payPeriodStartDate, payPeriodEndDate, payDate, payFrequency, employeeName, employerName);
    }

    /**
     * Returns a string representation of this paystub for debugging purposes.
     *
     * @return a string containing key field values
     */
    @Override
    public String toString() {
        return "Paystub{" +
                "payDate=" + payDate +
                ", payPeriod=" + payPeriodStartDate + " to " + payPeriodEndDate +
                ", payFrequency=" + payFrequency +
                ", employeeName='" + employeeName + '\'' +
                ", employerName='" + employerName + '\'' +
                ", earnings=" + earnings.size() +
                ", deductions=" + deductions.size() +
                ", hoursWorked=" + hoursWorked +
                '}';
    }
}
