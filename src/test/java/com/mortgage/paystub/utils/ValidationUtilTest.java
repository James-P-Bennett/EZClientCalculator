package com.mortgage.paystub.utils;

import com.mortgage.paystub.model.PayFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil class.
 *
 * @author James Bennett
 * @version 1.0
 */
class ValidationUtilTest {

    @Test
    @DisplayName("Should validate date formats correctly")
    void testIsValidDate() {
        // Valid dates in different formats
        assertTrue(ValidationUtil.isValidDate("12/31/2025"));
        assertTrue(ValidationUtil.isValidDate("1/1/2025"));
        assertTrue(ValidationUtil.isValidDate("01/01/2025"));
        assertTrue(ValidationUtil.isValidDate("12-31-2025"));
        assertTrue(ValidationUtil.isValidDate("2025-12-31"));

        // Invalid dates
        assertFalse(ValidationUtil.isValidDate("13/31/2025")); // Invalid month
        assertFalse(ValidationUtil.isValidDate("12/32/2025")); // Invalid day
        assertFalse(ValidationUtil.isValidDate("not a date"));
        assertFalse(ValidationUtil.isValidDate(""));
        assertFalse(ValidationUtil.isValidDate(null));
    }

    @Test
    @DisplayName("Should parse dates correctly")
    void testParseDate() {
        LocalDate expected = LocalDate.of(2025, 12, 31);

        assertEquals(expected, ValidationUtil.parseDate("12/31/2025"));
        assertEquals(expected, ValidationUtil.parseDate("12-31-2025"));
        assertEquals(expected, ValidationUtil.parseDate("2025-12-31"));

        assertNull(ValidationUtil.parseDate("invalid"));
        assertNull(ValidationUtil.parseDate(null));
        assertNull(ValidationUtil.parseDate(""));
    }

    @Test
    @DisplayName("Should validate reasonable paystub dates")
    void testIsReasonablePaystubDate() {
        LocalDate today = LocalDate.now();
        LocalDate recentPast = today.minusMonths(6);
        LocalDate tooOld = today.minusYears(3);
        LocalDate future = today.plusMonths(2);

        assertTrue(ValidationUtil.isReasonablePaystubDate(today));
        assertTrue(ValidationUtil.isReasonablePaystubDate(recentPast));
        assertFalse(ValidationUtil.isReasonablePaystubDate(tooOld));
        assertFalse(ValidationUtil.isReasonablePaystubDate(future));
        assertFalse(ValidationUtil.isReasonablePaystubDate(null));
    }

    @Test
    @DisplayName("Should validate currency format")
    void testIsValidCurrency() {
        // Valid formats
        assertTrue(ValidationUtil.isValidCurrency("1234.56"));
        assertTrue(ValidationUtil.isValidCurrency("$1234.56"));
        assertTrue(ValidationUtil.isValidCurrency("$1,234.56"));
        assertTrue(ValidationUtil.isValidCurrency("1,234"));
        assertTrue(ValidationUtil.isValidCurrency("-1234.56"));
        assertTrue(ValidationUtil.isValidCurrency("-$1,234.56"));

        // Invalid formats
        assertFalse(ValidationUtil.isValidCurrency(""));
        assertFalse(ValidationUtil.isValidCurrency(null));
        assertFalse(ValidationUtil.isValidCurrency("abc"));
        assertFalse(ValidationUtil.isValidCurrency("12.345")); // Too many decimal places
    }

    @Test
    @DisplayName("Should parse currency correctly")
    void testParseCurrency() {
        assertEquals(new BigDecimal("1234.56"), ValidationUtil.parseCurrency("1234.56"));
        assertEquals(new BigDecimal("1234.56"), ValidationUtil.parseCurrency("$1234.56"));
        assertEquals(new BigDecimal("1234.56"), ValidationUtil.parseCurrency("$1,234.56"));
        assertEquals(new BigDecimal("-1234.56"), ValidationUtil.parseCurrency("-$1,234.56"));

        assertNull(ValidationUtil.parseCurrency("invalid"));
        assertNull(ValidationUtil.parseCurrency(null));
        assertNull(ValidationUtil.parseCurrency(""));
    }

    @Test
    @DisplayName("Should validate reasonable currency amounts")
    void testIsReasonableCurrencyAmount() {
        assertTrue(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("1000")));
        assertTrue(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("50000")));
        assertTrue(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("0")));
        assertTrue(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("-1000")));

        assertFalse(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("2000000"))); // Too large
        assertFalse(ValidationUtil.isReasonableCurrencyAmount(new BigDecimal("-200000"))); // Too negative
        assertFalse(ValidationUtil.isReasonableCurrencyAmount(null));
    }

    @Test
    @DisplayName("Should validate name format")
    void testIsValidName() {
        // Valid names
        assertTrue(ValidationUtil.isValidName("John Doe"));
        assertTrue(ValidationUtil.isValidName("Mary-Jane Watson"));
        assertTrue(ValidationUtil.isValidName("O'Brien"));
        assertTrue(ValidationUtil.isValidName("Jean-Claude Van Damme"));

        // Invalid names
        assertFalse(ValidationUtil.isValidName("J")); // Too short
        assertFalse(ValidationUtil.isValidName("John123")); // Contains numbers
        assertFalse(ValidationUtil.isValidName("John@Doe")); // Special chars
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName(null));
    }

    @Test
    @DisplayName("Should detect similar names")
    void testNamesAreSimilar() {
        // Similar names (exact match after normalization)
        assertTrue(ValidationUtil.namesAreSimilar("John Doe", "john doe"));
        assertTrue(ValidationUtil.namesAreSimilar("John Doe", "JOHN DOE"));
        assertTrue(ValidationUtil.namesAreSimilar("John Doe", "John  Doe")); // Extra spaces
        assertTrue(ValidationUtil.namesAreSimilar("Mary Smith", "mary smith"));

        // Names that contain each other
        assertTrue(ValidationUtil.namesAreSimilar("John", "John Doe")); // One contains the other
        assertTrue(ValidationUtil.namesAreSimilar("Doe", "John Doe")); // One contains the other

        // Different names
        assertFalse(ValidationUtil.namesAreSimilar("John Doe", "Jane Doe"));
        assertFalse(ValidationUtil.namesAreSimilar("John Smith", "Mary Jones"));

        // Null cases
        assertFalse(ValidationUtil.namesAreSimilar(null, "John Doe"));
        assertFalse(ValidationUtil.namesAreSimilar("John Doe", null));
        assertFalse(ValidationUtil.namesAreSimilar(null, null));
    }

    @Test
    @DisplayName("Should validate YTD relationship")
    void testIsValidYtdRelationship() {
        assertTrue(ValidationUtil.isValidYtdRelationship(
            new BigDecimal("1000"),
            new BigDecimal("5000")
        )); // YTD > current

        assertTrue(ValidationUtil.isValidYtdRelationship(
            new BigDecimal("1000"),
            new BigDecimal("1000")
        )); // YTD = current

        assertFalse(ValidationUtil.isValidYtdRelationship(
            new BigDecimal("5000"),
            new BigDecimal("1000")
        )); // YTD < current (invalid)

        assertFalse(ValidationUtil.isValidYtdRelationship(null, new BigDecimal("1000")));
        assertFalse(ValidationUtil.isValidYtdRelationship(new BigDecimal("1000"), null));
    }

    @Test
    @DisplayName("Should validate pay frequency")
    void testIsValidPayFrequency() {
        assertTrue(ValidationUtil.isValidPayFrequency(PayFrequency.WEEKLY));
        assertTrue(ValidationUtil.isValidPayFrequency(PayFrequency.BI_WEEKLY));
        assertTrue(ValidationUtil.isValidPayFrequency(PayFrequency.SEMI_MONTHLY));
        assertTrue(ValidationUtil.isValidPayFrequency(PayFrequency.MONTHLY));

        assertFalse(ValidationUtil.isValidPayFrequency(null));
    }

    @Test
    @DisplayName("Should validate string is not empty")
    void testIsNotEmpty() {
        assertTrue(ValidationUtil.isNotEmpty("hello"));
        assertTrue(ValidationUtil.isNotEmpty("   hello   "));

        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
        assertFalse(ValidationUtil.isNotEmpty(null));
    }

    @Test
    @DisplayName("Should validate email format")
    void testIsValidEmail() {
        // Valid emails
        assertTrue(ValidationUtil.isValidEmail("john@example.com"));
        assertTrue(ValidationUtil.isValidEmail("john.doe@example.com"));
        assertTrue(ValidationUtil.isValidEmail("john+tag@example.co.uk"));

        // Invalid emails
        assertFalse(ValidationUtil.isValidEmail("invalid"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail("john@"));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Should validate phone format")
    void testIsValidPhone() {
        // Valid phones
        assertTrue(ValidationUtil.isValidPhone("5551234567")); // 10 digits
        assertTrue(ValidationUtil.isValidPhone("15551234567")); // 11 digits with country code
        assertTrue(ValidationUtil.isValidPhone("(555) 123-4567"));
        assertTrue(ValidationUtil.isValidPhone("555-123-4567"));
        assertTrue(ValidationUtil.isValidPhone("555.123.4567"));

        // Invalid phones
        assertFalse(ValidationUtil.isValidPhone("123")); // Too short
        assertFalse(ValidationUtil.isValidPhone("abc"));
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone(null));
    }

    @Test
    @DisplayName("Should validate positive numbers")
    void testIsPositive() {
        assertTrue(ValidationUtil.isPositive(new BigDecimal("1")));
        assertTrue(ValidationUtil.isPositive(new BigDecimal("100.50")));

        assertFalse(ValidationUtil.isPositive(new BigDecimal("0")));
        assertFalse(ValidationUtil.isPositive(new BigDecimal("-1")));
        assertFalse(ValidationUtil.isPositive(null));
    }

    @Test
    @DisplayName("Should validate non-negative numbers")
    void testIsNonNegative() {
        assertTrue(ValidationUtil.isNonNegative(new BigDecimal("0")));
        assertTrue(ValidationUtil.isNonNegative(new BigDecimal("1")));
        assertTrue(ValidationUtil.isNonNegative(new BigDecimal("100.50")));

        assertFalse(ValidationUtil.isNonNegative(new BigDecimal("-1")));
        assertFalse(ValidationUtil.isNonNegative(null));
    }
}
