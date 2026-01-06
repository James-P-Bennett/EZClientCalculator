package com.mortgage.paystub.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a borrower (loan applicant) with their employment and income information.
 * A borrower has one employer and can have multiple paystubs for analysis.
 *
 * @author James Bennett
 * @version 1.0
 */
public class Borrower {
    private String name;
    private String employerName;
    private EmploymentType employmentType;
    private PayFrequency payFrequency;
    private BigDecimal hourlyRate;
    private BigDecimal salary;
    private List<Paystub> paystubs;

    /**
     * Default constructor for Borrower.
     * Initializes paystubs list.
     */
    public Borrower() {
        this.paystubs = new ArrayList<>();
        this.hourlyRate = BigDecimal.ZERO;
        this.salary = BigDecimal.ZERO;
    }

    /**
     * Constructs a Borrower with basic information.
     *
     * @param name the borrower's name
     * @param employerName the employer's name
     * @param employmentType the type of employment (HOURLY or SALARY)
     * @param payFrequency the frequency of payment
     */
    public Borrower(String name, String employerName, EmploymentType employmentType, PayFrequency payFrequency) {
        this.name = name;
        this.employerName = employerName;
        this.employmentType = employmentType;
        this.payFrequency = payFrequency;
        this.paystubs = new ArrayList<>();
        this.hourlyRate = BigDecimal.ZERO;
        this.salary = BigDecimal.ZERO;
    }

    /**
     * Gets the borrower's name.
     *
     * @return the borrower's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the borrower's name.
     *
     * @param name the borrower's name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the employer's name.
     *
     * @return the employer's name
     */
    public String getEmployerName() {
        return employerName;
    }

    /**
     * Sets the employer's name.
     *
     * @param employerName the employer's name to set
     */
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    /**
     * Gets the employment type.
     *
     * @return the employment type
     */
    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    /**
     * Sets the employment type.
     *
     * @param employmentType the employment type to set
     */
    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
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
     * Gets the hourly rate (applicable for hourly employees).
     *
     * @return the hourly rate
     */
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Sets the hourly rate (applicable for hourly employees).
     *
     * @param hourlyRate the hourly rate to set
     */
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
    }

    /**
     * Gets the salary per pay period (applicable for salaried employees).
     *
     * @return the salary per pay period
     */
    public BigDecimal getSalary() {
        return salary;
    }

    /**
     * Sets the salary per pay period (applicable for salaried employees).
     *
     * @param salary the salary per pay period to set
     */
    public void setSalary(BigDecimal salary) {
        this.salary = salary != null ? salary : BigDecimal.ZERO;
    }

    /**
     * Gets the list of paystubs for this borrower.
     *
     * @return the list of paystubs
     */
    public List<Paystub> getPaystubs() {
        return paystubs;
    }

    /**
     * Sets the list of paystubs for this borrower.
     *
     * @param paystubs the list of paystubs to set
     */
    public void setPaystubs(List<Paystub> paystubs) {
        this.paystubs = paystubs != null ? paystubs : new ArrayList<>();
    }

    /**
     * Adds a paystub to this borrower's collection.
     *
     * @param paystub the paystub to add
     */
    public void addPaystub(Paystub paystub) {
        if (paystub != null) {
            this.paystubs.add(paystub);
        }
    }

    /**
     * Gets the most recent paystub based on pay date.
     *
     * @return the most recent paystub, or null if no paystubs exist
     */
    public Paystub getMostRecentPaystub() {
        return paystubs.stream()
                .max((p1, p2) -> p1.getPayDate().compareTo(p2.getPayDate()))
                .orElse(null);
    }

    /**
     * Checks if this borrower is an hourly employee.
     *
     * @return true if employment type is HOURLY, false otherwise
     */
    public boolean isHourly() {
        return employmentType == EmploymentType.HOURLY;
    }

    /**
     * Checks if this borrower is a salaried employee.
     *
     * @return true if employment type is SALARY, false otherwise
     */
    public boolean isSalaried() {
        return employmentType == EmploymentType.SALARY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Borrower borrower = (Borrower) o;
        return Objects.equals(name, borrower.name) &&
                Objects.equals(employerName, borrower.employerName) &&
                employmentType == borrower.employmentType &&
                payFrequency == borrower.payFrequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, employerName, employmentType, payFrequency);
    }

    /**
     * Returns a string representation of this borrower for debugging purposes.
     *
     * @return a string containing key field values
     */
    @Override
    public String toString() {
        return "Borrower{" +
                "name='" + name + '\'' +
                ", employerName='" + employerName + '\'' +
                ", employmentType=" + employmentType +
                ", payFrequency=" + payFrequency +
                ", hourlyRate=" + hourlyRate +
                ", salary=" + salary +
                ", paystubs=" + paystubs.size() +
                '}';
    }
}
