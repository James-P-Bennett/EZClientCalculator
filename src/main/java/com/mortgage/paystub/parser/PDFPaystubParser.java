package com.mortgage.paystub.parser;

import com.mortgage.paystub.model.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF-based paystub parser using Apache PDFBox.
 * Extracts paystub data from PDF documents using pattern recognition and text analysis.
 *
 * <p>Supports common paystub formats from providers like ADP, Paychex, and others.
 * Uses regex patterns to identify and extract:
 * <ul>
 *   <li>Employee and employer names</li>
 *   <li>Pay dates and periods</li>
 *   <li>Earnings (regular, overtime, bonuses, etc.)</li>
 *   <li>Deductions</li>
 *   <li>YTD totals</li>
 * </ul>
 *
 * @author James Bennett
 * @version 1.0
 */
public class PDFPaystubParser implements PaystubParser {

    private static final Logger logger = LoggerFactory.getLogger(PDFPaystubParser.class);

    // Pattern for currency: matches $1,234.56 or 1234.56
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
            "\\$?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"
    );

    // Patterns for dates - supporting multiple formats
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yy"),
            DateTimeFormatter.ofPattern("M/d/yy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b"
    );

    // Common pay type keywords for categorization
    private static final Map<String, PayCategory> PAY_TYPE_CATEGORIES = new HashMap<>();

    static {
        // Base wage keywords
        PAY_TYPE_CATEGORIES.put("regular", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("salary", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("hourly", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("holiday", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("pto", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("vacation", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("sick", PayCategory.BASE_WAGE);
        PAY_TYPE_CATEGORIES.put("personal", PayCategory.BASE_WAGE);

        // Variable income keywords
        PAY_TYPE_CATEGORIES.put("overtime", PayCategory.VARIABLE);
        PAY_TYPE_CATEGORIES.put("ot", PayCategory.VARIABLE);
        PAY_TYPE_CATEGORIES.put("commission", PayCategory.VARIABLE);
        PAY_TYPE_CATEGORIES.put("bonus", PayCategory.VARIABLE);
        PAY_TYPE_CATEGORIES.put("incentive", PayCategory.VARIABLE);
    }

    // Keywords that indicate YTD columns
    private static final List<String> YTD_INDICATORS = Arrays.asList(
            "ytd", "year to date", "yr to dt", "year-to-date"
    );

    @Override
    public ParsingResult parse(File file) throws IOException {
        logger.info("Starting PDF parsing for file: {}", file.getName());

        ParsingResult result = new ParsingResult();

        try {
            // Extract text from PDF
            String text = extractText(file);
            result.setExtractedText(text);

            if (text == null || text.trim().isEmpty()) {
                result.addError("No text could be extracted from PDF");
                result.setConfidenceLevel(ParsingResult.ConfidenceLevel.FAILED);
                return result;
            }

            Paystub paystub = result.getPaystub();

            // Parse different sections
            int fieldsExtracted = 0;

            // Extract employee name
            if (extractEmployeeName(text, paystub)) {
                fieldsExtracted++;
            } else {
                result.addFieldNeedingVerification("Employee Name");
            }

            // Extract employer name
            if (extractEmployerName(text, paystub)) {
                fieldsExtracted++;
            } else {
                result.addFieldNeedingVerification("Employer Name");
            }

            // Extract dates
            if (extractDates(text, paystub, result)) {
                fieldsExtracted++;
            } else {
                result.addFieldNeedingVerification("Pay Dates");
            }

            // Extract pay frequency
            if (extractPayFrequency(text, paystub)) {
                fieldsExtracted++;
            } else {
                result.addFieldNeedingVerification("Pay Frequency");
            }

            // Extract earnings
            int earningsExtracted = extractEarnings(text, paystub, result);
            if (earningsExtracted > 0) {
                fieldsExtracted++;
            } else {
                result.addFieldNeedingVerification("Earnings");
            }

            // Extract deductions
            int deductionsExtracted = extractDeductions(text, paystub, result);
            if (deductionsExtracted > 0) {
                fieldsExtracted++;
            }

            // Determine confidence level based on fields extracted
            determineConfidenceLevel(result, fieldsExtracted);

            logger.info("PDF parsing completed. Confidence: {}, Fields extracted: {}",
                        result.getConfidenceLevel(), fieldsExtracted);

        } catch (Exception e) {
            logger.error("Error parsing PDF: {}", e.getMessage(), e);
            result.addError("Parsing error: " + e.getMessage());
            result.setConfidenceLevel(ParsingResult.ConfidenceLevel.FAILED);
        }

        return result;
    }

    @Override
    public String extractText(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist");
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    @Override
    public boolean supports(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
    }

    @Override
    public String getParserName() {
        return "PDF Paystub Parser";
    }

    /**
     * Extracts employee name from the text.
     * Looks for common patterns like "Employee:", "Name:", etc.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @return true if name was extracted, false otherwise
     */
    private boolean extractEmployeeName(String text, Paystub paystub) {
        // Common patterns for employee name
        String[] patterns = {
                "Employee\\s*:?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)",
                "Employee\\s+Name\\s*:?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)",
                "Name\\s*:?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String name = matcher.group(1).trim();
                paystub.setEmployeeName(name);
                logger.debug("Extracted employee name: {}", name);
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts employer name from the text.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @return true if employer was extracted, false otherwise
     */
    private boolean extractEmployerName(String text, Paystub paystub) {
        // Common patterns for employer name
        String[] patterns = {
                "Employer\\s*:?\\s*([A-Z][A-Za-z0-9\\s&,\\.]+?)(?:\\n|Pay)",
                "Company\\s*:?\\s*([A-Z][A-Za-z0-9\\s&,\\.]+?)(?:\\n|Pay)"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String employer = matcher.group(1).trim();
                // Limit to reasonable length
                if (employer.length() > 50) {
                    employer = employer.substring(0, 50);
                }
                paystub.setEmployerName(employer);
                logger.debug("Extracted employer name: {}", employer);
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts dates (pay date, period start, period end) from the text.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @param result the parsing result for warnings
     * @return true if at least pay date was extracted, false otherwise
     */
    private boolean extractDates(String text, Paystub paystub, ParsingResult result) {
        List<LocalDate> dates = new ArrayList<>();

        // Find all dates in the text
        Matcher matcher = DATE_PATTERN.matcher(text);
        while (matcher.find()) {
            String dateStr = matcher.group(1);
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                dates.add(date);
            }
        }

        if (dates.isEmpty()) {
            return false;
        }

        // Sort dates
        Collections.sort(dates);

        // Heuristic: Most recent date is likely the pay date
        // Earlier dates are likely period start/end
        if (dates.size() >= 1) {
            LocalDate payDate = dates.get(dates.size() - 1);
            paystub.setPayDate(payDate);
            logger.debug("Extracted pay date: {}", payDate);

            if (dates.size() >= 2) {
                paystub.setPayPeriodEndDate(dates.get(dates.size() - 2));
            }

            if (dates.size() >= 3) {
                paystub.setPayPeriodStartDate(dates.get(dates.size() - 3));
            }

            return true;
        }

        return false;
    }

    /**
     * Parses a date string using multiple format attempts.
     *
     * @param dateStr the date string
     * @return the parsed LocalDate, or null if parsing failed
     */
    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        return null;
    }

    /**
     * Extracts pay frequency from the text.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @return true if frequency was extracted, false otherwise
     */
    private boolean extractPayFrequency(String text, Paystub paystub) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("bi-weekly") || lowerText.contains("biweekly") ||
            lowerText.contains("bi weekly")) {
            paystub.setPayFrequency(PayFrequency.BI_WEEKLY);
            logger.debug("Extracted pay frequency: BI_WEEKLY");
            return true;
        } else if (lowerText.contains("semi-monthly") || lowerText.contains("semimonthly") ||
                   lowerText.contains("semi monthly")) {
            paystub.setPayFrequency(PayFrequency.SEMI_MONTHLY);
            logger.debug("Extracted pay frequency: SEMI_MONTHLY");
            return true;
        } else if (lowerText.contains("monthly") && !lowerText.contains("semi")) {
            paystub.setPayFrequency(PayFrequency.MONTHLY);
            logger.debug("Extracted pay frequency: MONTHLY");
            return true;
        } else if (lowerText.contains("weekly") && !lowerText.contains("bi")) {
            paystub.setPayFrequency(PayFrequency.WEEKLY);
            logger.debug("Extracted pay frequency: WEEKLY");
            return true;
        }

        return false;
    }

    /**
     * Extracts earnings from the text.
     * Looks for pay type names with associated current and YTD amounts.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @param result the parsing result for warnings
     * @return the number of earnings extracted
     */
    private int extractEarnings(String text, Paystub paystub, ParsingResult result) {
        int count = 0;

        // Split text into lines for line-by-line analysis
        String[] lines = text.split("\\n");

        for (String line : lines) {
            // Look for patterns like: "Regular  1600.00  8000.00" or "Overtime  $200.00  $800.00"
            // Pattern: word(s) followed by 2 currency amounts
            Pattern earningPattern = Pattern.compile(
                    "([A-Za-z\\s]+?)\\s+" +
                    CURRENCY_PATTERN.pattern() + "\\s+" +
                    CURRENCY_PATTERN.pattern()
            );

            Matcher matcher = earningPattern.matcher(line);
            if (matcher.find()) {
                String payTypeName = matcher.group(1).trim();

                // Skip if it looks like a header
                if (isLikelyHeader(payTypeName)) {
                    continue;
                }

                try {
                    BigDecimal currentAmount = parseCurrency(matcher.group(2));
                    BigDecimal ytdAmount = parseCurrency(matcher.group(3));

                    // Categorize the pay type
                    PayCategory category = categorizePayType(payTypeName);

                    Earning earning = new Earning(payTypeName, category, currentAmount, ytdAmount);
                    paystub.addEarning(earning);

                    logger.debug("Extracted earning: {} - Current: {}, YTD: {}",
                               payTypeName, currentAmount, ytdAmount);
                    count++;

                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse currency in line: {}", line);
                }
            }
        }

        if (count == 0) {
            result.addWarning("No earnings could be automatically extracted");
        }

        return count;
    }

    /**
     * Extracts deductions from the text.
     *
     * @param text the extracted text
     * @param paystub the paystub to populate
     * @param result the parsing result for warnings
     * @return the number of deductions extracted
     */
    private int extractDeductions(String text, Paystub paystub, ParsingResult result) {
        int count = 0;

        // Look for common deduction section headers
        boolean inDeductionsSection = false;
        String[] lines = text.split("\\n");

        for (String line : lines) {
            String lowerLine = line.toLowerCase();

            // Check if we're entering deductions section
            if (lowerLine.contains("deduction") || lowerLine.contains("withholding")) {
                inDeductionsSection = true;
                continue;
            }

            // Check if we've left the deductions section
            if (inDeductionsSection && (lowerLine.contains("net pay") || lowerLine.contains("total"))) {
                break;
            }

            if (inDeductionsSection) {
                // Look for deduction patterns
                Pattern deductionPattern = Pattern.compile(
                        "([A-Za-z\\s]+?)\\s+" +
                        CURRENCY_PATTERN.pattern() + "\\s+" +
                        CURRENCY_PATTERN.pattern()
                );

                Matcher matcher = deductionPattern.matcher(line);
                if (matcher.find()) {
                    String deductionName = matcher.group(1).trim();

                    if (isLikelyHeader(deductionName)) {
                        continue;
                    }

                    try {
                        BigDecimal currentAmount = parseCurrency(matcher.group(2));
                        BigDecimal ytdAmount = parseCurrency(matcher.group(3));

                        Deduction deduction = new Deduction(deductionName, currentAmount, ytdAmount);
                        paystub.addDeduction(deduction);

                        logger.debug("Extracted deduction: {} - Current: {}, YTD: {}",
                                   deductionName, currentAmount, ytdAmount);
                        count++;

                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse deduction currency in line: {}", line);
                    }
                }
            }
        }

        return count;
    }

    /**
     * Categorizes a pay type based on its name.
     *
     * @param payTypeName the name of the pay type
     * @return the appropriate PayCategory
     */
    private PayCategory categorizePayType(String payTypeName) {
        String lowerName = payTypeName.toLowerCase();

        for (Map.Entry<String, PayCategory> entry : PAY_TYPE_CATEGORIES.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default to OTHER if not recognized
        return PayCategory.OTHER;
    }

    /**
     * Parses a currency string to BigDecimal.
     *
     * @param currencyStr the currency string (may include $, commas)
     * @return the parsed BigDecimal
     */
    private BigDecimal parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Remove $, spaces, and commas
        String cleaned = currencyStr.replace("$", "")
                                    .replace(" ", "")
                                    .replace(",", "")
                                    .trim();

        return new BigDecimal(cleaned);
    }

    /**
     * Checks if a string is likely a table header rather than actual data.
     *
     * @param text the text to check
     * @return true if it looks like a header, false otherwise
     */
    private boolean isLikelyHeader(String text) {
        String lower = text.toLowerCase();
        return lower.contains("description") ||
               lower.contains("current") ||
               lower.contains("ytd") ||
               lower.contains("rate") ||
               lower.contains("hours") ||
               lower.contains("amount") ||
               lower.length() < 2;
    }

    /**
     * Determines the confidence level based on the number of fields extracted.
     *
     * @param result the parsing result
     * @param fieldsExtracted the number of fields successfully extracted
     */
    private void determineConfidenceLevel(ParsingResult result, int fieldsExtracted) {
        if (result.hasErrors()) {
            result.setConfidenceLevel(ParsingResult.ConfidenceLevel.FAILED);
        } else if (fieldsExtracted >= 5 && result.getPaystub().getEarnings().size() >= 2) {
            result.setConfidenceLevel(ParsingResult.ConfidenceLevel.HIGH);
        } else if (fieldsExtracted >= 3 && result.getPaystub().getEarnings().size() >= 1) {
            result.setConfidenceLevel(ParsingResult.ConfidenceLevel.MEDIUM);
        } else {
            result.setConfidenceLevel(ParsingResult.ConfidenceLevel.LOW);
        }
    }
}
