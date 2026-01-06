package com.mortgage.paystub.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class for formatting various data types for display.
 * Provides consistent formatting across the application.
 *
 * @author James Bennett
 * @version 1.0
 */
public class FormattingUtil {

    // Standard formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_LONG = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("0.00");
    private static final DecimalFormat NUMBER_FORMATTER = new DecimalFormat("#,##0.##");

    static {
        CURRENCY_FORMATTER.setRoundingMode(RoundingMode.HALF_UP);
        PERCENTAGE_FORMATTER.setRoundingMode(RoundingMode.HALF_UP);
        NUMBER_FORMATTER.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Formats a BigDecimal as currency.
     *
     * @param amount the amount to format
     * @return formatted currency string (e.g., "$1,234.56")
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return CURRENCY_FORMATTER.format(amount);
    }

    /**
     * Formats a BigDecimal as currency without the dollar sign.
     *
     * @param amount the amount to format
     * @return formatted number string (e.g., "1,234.56")
     */
    public static String formatCurrencyNoDollar(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(amount);
    }

    /**
     * Formats a date using the standard format (MM/dd/yyyy).
     *
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a date using long format (e.g., "January 15, 2026").
     *
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDateLong(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER_LONG);
    }

    /**
     * Formats a BigDecimal as a percentage.
     *
     * @param value the value to format (e.g., 0.8571 for 85.71%)
     * @param includeSymbol whether to include the % symbol
     * @return formatted percentage string
     */
    public static String formatPercentage(BigDecimal value, boolean includeSymbol) {
        if (value == null) {
            return includeSymbol ? "0.00%" : "0.00";
        }
        String formatted = PERCENTAGE_FORMATTER.format(value);
        return includeSymbol ? formatted + "%" : formatted;
    }

    /**
     * Formats a BigDecimal as a percentage with symbol.
     *
     * @param value the value to format
     * @return formatted percentage string with % symbol
     */
    public static String formatPercentage(BigDecimal value) {
        return formatPercentage(value, true);
    }

    /**
     * Formats a number with comma separators.
     *
     * @param number the number to format
     * @return formatted number string (e.g., "1,234.56")
     */
    public static String formatNumber(BigDecimal number) {
        if (number == null) {
            return "0";
        }
        return NUMBER_FORMATTER.format(number);
    }

    /**
     * Formats an integer with comma separators.
     *
     * @param number the number to format
     * @return formatted number string (e.g., "1,234")
     */
    public static String formatInteger(int number) {
        return NumberFormat.getIntegerInstance(Locale.US).format(number);
    }

    /**
     * Formats a BigDecimal with a specific number of decimal places.
     *
     * @param number the number to format
     * @param decimalPlaces the number of decimal places
     * @return formatted number string
     */
    public static String formatDecimal(BigDecimal number, int decimalPlaces) {
        if (number == null) {
            return "0";
        }

        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }

        DecimalFormat formatter = new DecimalFormat(pattern.toString());
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(number);
    }

    /**
     * Formats a phone number in standard US format.
     *
     * @param phone the phone number (10 digits)
     * @return formatted phone string (e.g., "(555) 123-4567")
     */
    public static String formatPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }

        // Remove all non-digit characters
        String digits = phone.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 10));
        } else if (digits.length() == 11 && digits.startsWith("1")) {
            return String.format("+1 (%s) %s-%s",
                digits.substring(1, 4),
                digits.substring(4, 7),
                digits.substring(7, 11));
        }

        // Return original if can't format
        return phone;
    }

    /**
     * Formats a name in title case.
     *
     * @param name the name to format
     * @return formatted name in title case
     */
    public static String formatName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                // Capitalize first letter
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1));
                }
            }

            if (i < words.length - 1) {
                formatted.append(" ");
            }
        }

        return formatted.toString();
    }

    /**
     * Formats file size in human-readable format.
     *
     * @param bytes the size in bytes
     * @return formatted size string (e.g., "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }

        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        }

        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }

    /**
     * Truncates a string to a maximum length with ellipsis.
     *
     * @param str the string to truncate
     * @param maxLength the maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }

        if (str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Pads a string with spaces to reach a minimum length.
     *
     * @param str the string to pad
     * @param length the minimum length
     * @param rightAlign whether to right-align (pad left)
     * @return padded string
     */
    public static String pad(String str, int length, boolean rightAlign) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        int padding = length - str.length();
        StringBuilder sb = new StringBuilder();

        if (rightAlign) {
            for (int i = 0; i < padding; i++) {
                sb.append(" ");
            }
            sb.append(str);
        } else {
            sb.append(str);
            for (int i = 0; i < padding; i++) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Formats a duration in milliseconds to a human-readable format.
     *
     * @param millis the duration in milliseconds
     * @return formatted duration (e.g., "2.5 seconds")
     */
    public static String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + " ms";
        }

        double seconds = millis / 1000.0;
        if (seconds < 60) {
            return String.format("%.1f seconds", seconds);
        }

        double minutes = seconds / 60.0;
        if (minutes < 60) {
            return String.format("%.1f minutes", minutes);
        }

        double hours = minutes / 60.0;
        return String.format("%.1f hours", hours);
    }
}
