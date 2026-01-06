package com.mortgage.paystub.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FormattingUtil class.
 *
 * @author James Bennett
 * @version 1.0
 */
class FormattingUtilTest {

    @Test
    @DisplayName("Should format currency correctly")
    void testFormatCurrency() {
        assertEquals("$1,234.56", FormattingUtil.formatCurrency(new BigDecimal("1234.56")));
        assertEquals("$0.00", FormattingUtil.formatCurrency(new BigDecimal("0")));
        assertEquals("$1,000,000.00", FormattingUtil.formatCurrency(new BigDecimal("1000000")));
        assertEquals("-$500.25", FormattingUtil.formatCurrency(new BigDecimal("-500.25")));
        assertEquals("$0.00", FormattingUtil.formatCurrency(null));
    }

    @Test
    @DisplayName("Should format currency without dollar sign")
    void testFormatCurrencyNoDollar() {
        assertEquals("1,234.56", FormattingUtil.formatCurrencyNoDollar(new BigDecimal("1234.56")));
        assertEquals("0.00", FormattingUtil.formatCurrencyNoDollar(new BigDecimal("0")));
        assertEquals("1,000,000.00", FormattingUtil.formatCurrencyNoDollar(new BigDecimal("1000000")));
        assertEquals("0.00", FormattingUtil.formatCurrencyNoDollar(null));
    }

    @Test
    @DisplayName("Should format dates correctly")
    void testFormatDate() {
        LocalDate date = LocalDate.of(2025, 12, 31);
        assertEquals("12/31/2025", FormattingUtil.formatDate(date));

        LocalDate date2 = LocalDate.of(2025, 1, 1);
        assertEquals("01/01/2025", FormattingUtil.formatDate(date2));

        assertEquals("", FormattingUtil.formatDate(null));
    }

    @Test
    @DisplayName("Should format dates in long format")
    void testFormatDateLong() {
        LocalDate date = LocalDate.of(2025, 12, 31);
        assertEquals("December 31, 2025", FormattingUtil.formatDateLong(date));

        LocalDate date2 = LocalDate.of(2025, 1, 1);
        assertEquals("January 01, 2025", FormattingUtil.formatDateLong(date2));

        assertEquals("", FormattingUtil.formatDateLong(null));
    }

    @Test
    @DisplayName("Should format percentages correctly")
    void testFormatPercentage() {
        assertEquals("85.71%", FormattingUtil.formatPercentage(new BigDecimal("85.71")));
        assertEquals("0.00%", FormattingUtil.formatPercentage(new BigDecimal("0")));
        assertEquals("100.00%", FormattingUtil.formatPercentage(new BigDecimal("100")));
        assertEquals("0.00%", FormattingUtil.formatPercentage(null));

        // Without symbol
        assertEquals("85.71", FormattingUtil.formatPercentage(new BigDecimal("85.71"), false));
        assertEquals("0.00", FormattingUtil.formatPercentage(null, false));
    }

    @Test
    @DisplayName("Should format numbers with commas")
    void testFormatNumber() {
        assertEquals("1,234.56", FormattingUtil.formatNumber(new BigDecimal("1234.56")));
        assertEquals("1,000,000", FormattingUtil.formatNumber(new BigDecimal("1000000")));
        assertEquals("0", FormattingUtil.formatNumber(new BigDecimal("0")));
        assertEquals("0", FormattingUtil.formatNumber(null));
    }

    @Test
    @DisplayName("Should format integers")
    void testFormatInteger() {
        assertEquals("1,234", FormattingUtil.formatInteger(1234));
        assertEquals("1,000,000", FormattingUtil.formatInteger(1000000));
        assertEquals("0", FormattingUtil.formatInteger(0));
        assertEquals("-500", FormattingUtil.formatInteger(-500));
    }

    @Test
    @DisplayName("Should format decimals with specific places")
    void testFormatDecimal() {
        assertEquals("1,234.57", FormattingUtil.formatDecimal(new BigDecimal("1234.5678"), 2));
        assertEquals("1,234.568", FormattingUtil.formatDecimal(new BigDecimal("1234.5678"), 3));
        assertEquals("1,235", FormattingUtil.formatDecimal(new BigDecimal("1234.5678"), 0));
        assertEquals("0", FormattingUtil.formatDecimal(null, 2));
    }

    @Test
    @DisplayName("Should format phone numbers")
    void testFormatPhone() {
        assertEquals("(555) 123-4567", FormattingUtil.formatPhone("5551234567"));
        assertEquals("(555) 123-4567", FormattingUtil.formatPhone("555-123-4567"));
        assertEquals("(555) 123-4567", FormattingUtil.formatPhone("(555) 123-4567"));
        assertEquals("+1 (555) 123-4567", FormattingUtil.formatPhone("15551234567"));

        // Invalid format - returns original
        assertEquals("123", FormattingUtil.formatPhone("123"));
        assertEquals("", FormattingUtil.formatPhone(""));
        assertEquals("", FormattingUtil.formatPhone(null));
    }

    @Test
    @DisplayName("Should format names in title case")
    void testFormatName() {
        assertEquals("John Doe", FormattingUtil.formatName("john doe"));
        assertEquals("John Doe", FormattingUtil.formatName("JOHN DOE"));
        assertEquals("Mary Jane", FormattingUtil.formatName("mary jane"));
        assertEquals("", FormattingUtil.formatName(""));
        assertEquals("", FormattingUtil.formatName(null));
    }

    @Test
    @DisplayName("Should format file sizes")
    void testFormatFileSize() {
        assertEquals("500 B", FormattingUtil.formatFileSize(500));
        assertEquals("1.5 KB", FormattingUtil.formatFileSize(1536)); // 1.5 KB
        assertEquals("2.00 MB", FormattingUtil.formatFileSize(2097152)); // 2 MB
        assertEquals("1.00 GB", FormattingUtil.formatFileSize(1073741824)); // 1 GB
    }

    @Test
    @DisplayName("Should truncate strings")
    void testTruncate() {
        assertEquals("Hello...", FormattingUtil.truncate("Hello World", 8));
        assertEquals("Hello", FormattingUtil.truncate("Hello", 10));
        assertEquals("Hello", FormattingUtil.truncate("Hello", 5));
        assertEquals("", FormattingUtil.truncate(null, 10));
    }

    @Test
    @DisplayName("Should pad strings")
    void testPad() {
        // Left padding (right align)
        assertEquals("  test", FormattingUtil.pad("test", 6, true));
        // Right padding (left align)
        assertEquals("test  ", FormattingUtil.pad("test", 6, false));
        // No padding needed
        assertEquals("testing", FormattingUtil.pad("testing", 5, false));
        // Null string
        assertEquals("      ", FormattingUtil.pad(null, 6, false));
    }

    @Test
    @DisplayName("Should format duration")
    void testFormatDuration() {
        assertEquals("500 ms", FormattingUtil.formatDuration(500));
        assertEquals("2.5 seconds", FormattingUtil.formatDuration(2500));
        assertEquals("2.0 minutes", FormattingUtil.formatDuration(120000));
        assertEquals("1.0 hours", FormattingUtil.formatDuration(3600000));
    }
}
