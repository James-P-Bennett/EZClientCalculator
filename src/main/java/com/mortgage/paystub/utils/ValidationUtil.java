package com.mortgage.paystub.utils;

import com.mortgage.paystub.model.PayFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for validating various data types used in the application.
 * Provides validation methods for dates, currency, names, and other field types.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

    // Common date formats
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("M-d-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    // Currency pattern: optional $ or -, digits with optional commas and decimals
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^-?\\$?[0-9,]+(\\.[0-9]{1,2})?$");

    // Name pattern: letters, spaces, hyphens, apostrophes
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$");

    /**
     * Validates a date string and attempts to parse it.
     *
     * @param dateStr the date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                LocalDate.parse(dateStr.trim(), formatter);
                return true;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        logger.debug("Invalid date format: {}", dateStr);
        return false;
    }

    /**
     * Parses a date string using common formats.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        logger.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    /**
     * Validates that a date is within a reasonable range for paystubs.
     *
     * @param date the date to validate
     * @return true if date is reasonable, false otherwise
     */
    public static boolean isReasonablePaystubDate(LocalDate date) {
        if (date == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        LocalDate twoYearsAgo = now.minusYears(2);
        LocalDate oneMonthFuture = now.plusMonths(1);

        boolean isValid = date.isAfter(twoYearsAgo) && date.isBefore(oneMonthFuture);

        if (!isValid) {
            logger.debug("Date {} is outside reasonable range ({} to {})", date, twoYearsAgo, oneMonthFuture);
        }

        return isValid;
    }

    /**
     * Validates a currency string format.
     *
     * @param currencyStr the currency string to validate
     * @return true if valid currency format, false otherwise
     */
    public static boolean isValidCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return false;
        }

        return CURRENCY_PATTERN.matcher(currencyStr.trim()).matches();
    }

    /**
     * Parses a currency string to BigDecimal.
     *
     * @param currencyStr the currency string to parse
     * @return the parsed BigDecimal, or null if parsing fails
     */
    public static BigDecimal parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove $ and commas
            String cleaned = currencyStr.trim()
                                       .replace("$", "")
                                       .replace(",", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse currency: {}", currencyStr);
            return null;
        }
    }

    /**
     * Validates that a currency amount is reasonable.
     *
     * @param amount the amount to validate
     * @return true if reasonable, false otherwise
     */
    public static boolean isReasonableCurrencyAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        // Reasonable range: -$100,000 to $1,000,000 per field
        BigDecimal minAmount = new BigDecimal("-100000");
        BigDecimal maxAmount = new BigDecimal("1000000");

        boolean isValid = amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;

        if (!isValid) {
            logger.debug("Amount {} is outside reasonable range", amount);
        }

        return isValid;
    }

    /**
     * Validates a person's name.
     *
     * @param name the name to validate
     * @return true if valid name format, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String trimmed = name.trim();

        // Check length
        if (trimmed.length() < 2 || trimmed.length() > 100) {
            logger.debug("Name length {} is outside valid range (2-100)", trimmed.length());
            return false;
        }

        // Check pattern
        return NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Checks if two names are similar enough to be the same person.
     * Useful for detecting name inconsistencies on paystubs.
     *
     * @param name1 first name
     * @param name2 second name
     * @return true if names are similar, false otherwise
     */
    public static boolean namesAreSimilar(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }

        String normalized1 = normalizeName(name1);
        String normalized2 = normalizeName(name2);

        // Exact match after normalization
        if (normalized1.equals(normalized2)) {
            return true;
        }

        // Check if one contains the other (e.g., "John Smith" vs "Smith, John")
        return normalized1.contains(normalized2) || normalized2.contains(normalized1);
    }

    /**
     * Normalizes a name for comparison.
     *
     * @param name the name to normalize
     * @return normalized name (lowercase, no extra spaces)
     */
    private static String normalizeName(String name) {
        return name.trim()
                   .toLowerCase()
                   .replaceAll("\\s+", " ")
                   .replaceAll("[,.]", "");
    }

    /**
     * Validates that YTD total is greater than or equal to current amount.
     *
     * @param current current period amount
     * @param ytd year-to-date total
     * @return true if valid relationship, false otherwise
     */
    public static boolean isValidYtdRelationship(BigDecimal current, BigDecimal ytd) {
        if (current == null || ytd == null) {
            return false;
        }

        boolean isValid = ytd.compareTo(current) >= 0;

        if (!isValid) {
            logger.debug("Invalid YTD relationship: current={}, ytd={}", current, ytd);
        }

        return isValid;
    }

    /**
     * Validates a pay frequency value.
     *
     * @param frequency the pay frequency to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPayFrequency(PayFrequency frequency) {
        if (frequency == null) {
            return false;
        }

        // Check if it's one of the standard frequencies
        for (PayFrequency pf : PayFrequency.values()) {
            if (pf == frequency) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param str the string to validate
     * @return true if not null or empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates an email address format.
     *
     * @param email the email to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return emailPattern.matcher(email.trim()).matches();
    }

    /**
     * Validates a phone number format.
     *
     * @param phone the phone number to validate
     * @return true if valid phone format, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Remove common formatting characters
        String cleaned = phone.replaceAll("[\\s().-]", "");

        // Check if it's 10 or 11 digits (with optional country code)
        return cleaned.matches("^\\d{10,11}$");
    }

    /**
     * Validates that a number is positive.
     *
     * @param number the number to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Validates that a number is non-negative.
     *
     * @param number the number to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) >= 0;
    }
}
