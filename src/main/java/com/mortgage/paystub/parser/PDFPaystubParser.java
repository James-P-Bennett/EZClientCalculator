package com.mortgage.paystub.parser;

import com.mortgage.paystub.model.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

            logger.debug("Initial text extraction: {} characters, content: '{}'",
                text == null ? 0 : text.length(),
                text == null ? "null" : (text.isEmpty() ? "empty" : text.substring(0, Math.min(100, text.length()))));

            // Check if we have REAL text (not just whitespace)
            String trimmedText = text == null ? "" : text.trim();
            boolean hasRealText = trimmedText.length() > 10; // At least 10 non-whitespace characters

            // If no meaningful text extracted, try OCR (for scanned/image-based PDFs)
            if (!hasRealText) {
                logger.info("No meaningful text layer found (extracted {} chars, {} after trim), attempting OCR for: {}",
                    text == null ? 0 : text.length(), trimmedText.length(), file.getName());
                try {
                    text = extractTextWithOCR(file);
                    result.setExtractedText(text);
                    if (text != null && !text.trim().isEmpty()) {
                        logger.info("OCR successful, extracted {} characters", text.length());
                        logger.debug("OCR extracted text preview: {}",
                            text.length() > 200 ? text.substring(0, 200) + "..." : text);
                    } else {
                        logger.warn("OCR completed but extracted no text from: {}", file.getName());
                    }
                } catch (Exception ocrException) {
                    logger.error("OCR failed for {}: {}", file.getName(), ocrException.getMessage(), ocrException);
                }
            }

            // Check final result
            trimmedText = text == null ? "" : text.trim();
            if (trimmedText.length() < 10) {
                result.addError("No meaningful text could be extracted from PDF (tried both text extraction and OCR). The PDF may be blank, corrupted, or use an unsupported format.");
                result.setConfidenceLevel(ParsingResult.ConfidenceLevel.FAILED);
                logger.error("Complete parsing failure for {}: only {} chars of text extracted", file.getName(), trimmedText.length());
                return result;
            } else {
                logger.info("Successfully extracted {} characters of text from {}", trimmedText.length(), file.getName());
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

    /**
     * Extracts text from a PDF using OCR (Optical Character Recognition).
     * This is used as a fallback when the PDF has no text layer (scanned documents).
     *
     * @param file the PDF file to process
     * @return the extracted text
     * @throws IOException if there's an error reading the PDF
     * @throws TesseractException if OCR fails
     */
    private String extractTextWithOCR(File file) throws IOException, TesseractException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist");
        }

        StringBuilder fullText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();

            // Set tessdata path - check multiple locations
            File tessdataDir = new File("./tessdata");
            if (tessdataDir.exists() && tessdataDir.isDirectory()) {
                // Running from IDE or unpacked - tessdata exists in current directory
                tesseract.setDatapath("./tessdata");
                logger.debug("Using tessdata from: ./tessdata");
            } else {
                // Running from JAR - extract tessdata from classpath to temp directory
                try {
                    File tempDir = new File(System.getProperty("java.io.tmpdir"), "EZClientCalculator");
                    File tempTessdata = new File(tempDir, "tessdata");

                    // Only extract if not already extracted
                    if (!tempTessdata.exists() || !new File(tempTessdata, "eng.traineddata").exists()) {
                        logger.info("Extracting tessdata from JAR to: {}", tempTessdata.getAbsolutePath());
                        extractTessdataFromJar(tempTessdata);
                    }

                    tesseract.setDatapath(tempTessdata.getAbsolutePath());
                    logger.debug("Using tessdata from: {}", tempTessdata.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to extract tessdata from JAR: {}", e.getMessage(), e);
                    // Fallback to current directory (will likely fail but better than crashing)
                    tesseract.setDatapath("./tessdata");
                    logger.warn("Fallback to ./tessdata (may not exist)");
                }
            }

            // Configure Tesseract for better accuracy
            tesseract.setLanguage("eng");
            // Try different PSM modes - paystubs have tabular/structured layout
            // PSM 6 = Assume a single uniform block of text
            tesseract.setPageSegMode(6);
            logger.debug("Tesseract configured: language=eng, PSM=6 (SINGLE_BLOCK)");

            // Process each page
            int pageCount = document.getNumberOfPages();
            logger.debug("Processing {} pages with OCR", pageCount);

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                // Try different DPIs - sometimes higher/lower works better
                int[] dpiOptions = {300, 200, 150};
                BufferedImage image = null;

                for (int dpi : dpiOptions) {
                    logger.debug("Rendering page {} to image at {} DPI", pageIndex + 1, dpi);
                    image = renderer.renderImageWithDPI(pageIndex, dpi);
                    logger.debug("Image rendered: {}x{} pixels at {} DPI",
                        image.getWidth(), image.getHeight(), dpi);

                    // Quick test - check if image is not blank
                    if (!isImageBlank(image)) {
                        logger.debug("Image at {} DPI appears to have content", dpi);
                        break;
                    } else {
                        logger.warn("Image at {} DPI appears blank!", dpi);
                    }
                }

                if (image == null) {
                    logger.error("Failed to render page {} at any DPI", pageIndex + 1);
                    continue;
                }

                // Preprocess image for better OCR accuracy
                logger.debug("Preprocessing image for OCR...");
                BufferedImage processedImage = preprocessImageForOCR(image);

                // Try multiple PSM modes on preprocessed image first, then original
                int[] psmModes = {6, 4, 3, 1, 11, 12}; // Different page segmentation modes
                String[] psmNames = {"SINGLE_BLOCK", "SINGLE_COLUMN", "AUTO", "AUTO_OSD", "SPARSE_TEXT", "SPARSE_TEXT_OSD"};

                String pageText = null;

                // Try preprocessed image first (usually works better)
                for (int i = 0; i < psmModes.length && (pageText == null || pageText.trim().isEmpty()); i++) {
                    tesseract.setPageSegMode(psmModes[i]);
                    logger.debug("Trying PSM {} ({}) on PREPROCESSED image page {}", psmModes[i], psmNames[i], pageIndex + 1);

                    try {
                        pageText = tesseract.doOCR(processedImage);
                        if (pageText != null && !pageText.trim().isEmpty()) {
                            logger.info("SUCCESS with PSM {} ({}) on PREPROCESSED image! Extracted {} characters",
                                psmModes[i], psmNames[i], pageText.length());
                            break;
                        } else {
                            logger.debug("PSM {} on preprocessed failed - empty result", psmModes[i]);
                        }
                    } catch (TesseractException te) {
                        logger.warn("PSM {} on preprocessed threw exception: {}", psmModes[i], te.getMessage());
                    }
                }

                // If preprocessed didn't work, try original image
                if (pageText == null || pageText.trim().isEmpty()) {
                    logger.debug("Preprocessed image OCR failed, trying original image...");
                    for (int i = 0; i < psmModes.length && (pageText == null || pageText.trim().isEmpty()); i++) {
                        tesseract.setPageSegMode(psmModes[i]);
                        logger.debug("Trying PSM {} ({}) on ORIGINAL image page {}", psmModes[i], psmNames[i], pageIndex + 1);

                        try {
                            pageText = tesseract.doOCR(image);
                            if (pageText != null && !pageText.trim().isEmpty()) {
                                logger.info("SUCCESS with PSM {} ({}) on ORIGINAL image! Extracted {} characters",
                                    psmModes[i], psmNames[i], pageText.length());
                                break;
                            } else {
                                logger.debug("PSM {} on original failed - empty result", psmModes[i]);
                            }
                        } catch (TesseractException te) {
                            logger.warn("PSM {} on original threw exception: {}", psmModes[i], te.getMessage());
                        }
                    }
                }

                if (pageText != null && !pageText.trim().isEmpty()) {
                    fullText.append(pageText).append("\n");
                    logger.info("OCR SUCCESS! Extracted {} characters from page {}", pageText.length(), pageIndex + 1);
                } else {
                    logger.error("OCR FAILED on BOTH original and processed images for page {}", pageIndex + 1);
                }
            }

            logger.info("OCR processing complete. Total text extracted: {} characters", fullText.length());
        }

        return fullText.toString();
    }

    /**
     * Checks if an image appears to be blank (all white or all one color).
     *
     * @param image the image to check
     * @return true if image appears blank, false otherwise
     */
    private boolean isImageBlank(BufferedImage image) {
        // Sample pixels to check if image has variation
        int width = image.getWidth();
        int height = image.getHeight();
        int sampleSize = Math.min(1000, width * height / 100); // Sample 1% of pixels

        int firstPixel = image.getRGB(0, 0);
        int differentPixels = 0;

        for (int i = 0; i < sampleSize; i++) {
            int x = (i * 97) % width;  // Use prime number for better distribution
            int y = (i * 101) % height;
            if (image.getRGB(x, y) != firstPixel) {
                differentPixels++;
            }
        }

        // If less than 1% of sampled pixels are different, consider it blank
        boolean isBlank = differentPixels < (sampleSize / 100);
        if (isBlank) {
            logger.warn("Image appears blank: only {} of {} sampled pixels differ from first pixel",
                differentPixels, sampleSize);
        }
        return isBlank;
    }

    /**
     * Preprocesses an image to improve OCR accuracy.
     * Applies grayscale conversion and adaptive thresholding.
     *
     * @param original the original image
     * @return the preprocessed image optimized for OCR
     */
    private BufferedImage preprocessImageForOCR(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        // First pass: calculate optimal threshold using Otsu's method
        int[] histogram = new int[256];

        // Build histogram and convert to grayscale
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convert to grayscale using luminosity method
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                histogram[gray]++;

                int grayRGB = (gray << 16) | (gray << 8) | gray;
                grayscale.setRGB(x, y, grayRGB);
            }
        }

        // Calculate optimal threshold using Otsu's method
        int totalPixels = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float maxVariance = 0;
        int threshold = 128;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += t * histogram[t];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float variance = wB * wF * (mB - mF) * (mB - mF);
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }

        logger.debug("Calculated optimal threshold using Otsu's method: {}", threshold);

        // Second pass: apply adaptive threshold with safety bounds
        // Ensure threshold is reasonable for scanned documents (not too dark)
        if (threshold < 100) {
            logger.warn("Calculated threshold {} is too low, adjusting to 100", threshold);
            threshold = 100;
        }

        BufferedImage processed = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = grayscale.getRGB(x, y);
                int gray = rgb & 0xFF;

                // Apply threshold for binarization
                gray = gray > threshold ? 255 : 0;

                int grayRGB = (gray << 16) | (gray << 8) | gray;
                processed.setRGB(x, y, grayRGB);
            }
        }

        logger.debug("Image preprocessing complete: grayscale + Otsu binarization (threshold={})", threshold);
        return processed;
    }

    /**
     * Extracts tessdata directory from JAR to a temporary location.
     *
     * @param tessdataDir the target directory to extract to
     * @throws IOException if extraction fails
     */
    private void extractTessdataFromJar(File tessdataDir) throws IOException {
        // Create directory if it doesn't exist
        if (!tessdataDir.exists()) {
            tessdataDir.mkdirs();
        }

        // List of critical tessdata files to extract
        String[] files = {
            "tessdata/eng.traineddata",
            "tessdata/osd.traineddata",
            "tessdata/pdf.ttf"
        };

        for (String resourcePath : files) {
            try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    logger.warn("Resource not found in JAR: {}", resourcePath);
                    continue;
                }

                String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                File targetFile = new File(tessdataDir, fileName);

                logger.debug("Extracting {} to {}", resourcePath, targetFile.getAbsolutePath());

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                logger.debug("Extracted {}", fileName);
            }
        }

        logger.info("Tessdata extraction complete");
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
