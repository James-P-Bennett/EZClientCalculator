package com.mortgage.paystub.parser;

import com.mortgage.paystub.model.Paystub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of parsing a paystub document.
 * Contains the extracted paystub data, confidence level, and any warnings or errors.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ParsingResult {

    /**
     * Enumeration representing the confidence level of the parsing result.
     */
    public enum ConfidenceLevel {
        /**
         * High confidence - most fields extracted successfully with clear patterns
         */
        HIGH("High", "Most fields extracted successfully"),

        /**
         * Medium confidence - some fields extracted, manual verification recommended
         */
        MEDIUM("Medium", "Some fields extracted, verification recommended"),

        /**
         * Low confidence - minimal extraction, significant manual entry required
         */
        LOW("Low", "Minimal extraction, manual entry required"),

        /**
         * Failed - parsing failed completely
         */
        FAILED("Failed", "Parsing failed");

        private final String displayName;
        private final String description;

        ConfidenceLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private Paystub paystub;
    private ConfidenceLevel confidenceLevel;
    private List<String> fieldsNeedingVerification;
    private List<String> warnings;
    private List<String> errors;
    private String extractedText;

    /**
     * Default constructor for ParsingResult.
     */
    public ParsingResult() {
        this.paystub = new Paystub();
        this.confidenceLevel = ConfidenceLevel.LOW;
        this.fieldsNeedingVerification = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.extractedText = "";
    }

    /**
     * Constructs a ParsingResult with a paystub.
     *
     * @param paystub the extracted paystub
     */
    public ParsingResult(Paystub paystub) {
        this.paystub = paystub != null ? paystub : new Paystub();
        this.confidenceLevel = ConfidenceLevel.MEDIUM;
        this.fieldsNeedingVerification = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.extractedText = "";
    }

    /**
     * Gets the extracted paystub.
     *
     * @return the paystub
     */
    public Paystub getPaystub() {
        return paystub;
    }

    /**
     * Sets the extracted paystub.
     *
     * @param paystub the paystub to set
     */
    public void setPaystub(Paystub paystub) {
        this.paystub = paystub != null ? paystub : new Paystub();
    }

    /**
     * Gets the confidence level of the parsing.
     *
     * @return the confidence level
     */
    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    /**
     * Sets the confidence level of the parsing.
     *
     * @param confidenceLevel the confidence level to set
     */
    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel != null ? confidenceLevel : ConfidenceLevel.LOW;
    }

    /**
     * Gets the list of fields that need manual verification.
     *
     * @return the list of field names
     */
    public List<String> getFieldsNeedingVerification() {
        return fieldsNeedingVerification;
    }

    /**
     * Sets the list of fields that need manual verification.
     *
     * @param fieldsNeedingVerification the list of field names
     */
    public void setFieldsNeedingVerification(List<String> fieldsNeedingVerification) {
        this.fieldsNeedingVerification = fieldsNeedingVerification != null ?
                fieldsNeedingVerification : new ArrayList<>();
    }

    /**
     * Adds a field that needs manual verification.
     *
     * @param fieldName the name of the field
     */
    public void addFieldNeedingVerification(String fieldName) {
        if (fieldName != null && !fieldName.trim().isEmpty()) {
            this.fieldsNeedingVerification.add(fieldName);
        }
    }

    /**
     * Gets the list of warnings generated during parsing.
     *
     * @return the list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Sets the list of warnings.
     *
     * @param warnings the list of warnings
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    /**
     * Adds a warning message.
     *
     * @param warning the warning message
     */
    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            this.warnings.add(warning);
        }
    }

    /**
     * Gets the list of errors encountered during parsing.
     *
     * @return the list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Sets the list of errors.
     *
     * @param errors the list of errors
     */
    public void setErrors(List<String> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    /**
     * Adds an error message.
     *
     * @param error the error message
     */
    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            this.errors.add(error);
        }
    }

    /**
     * Gets the raw extracted text from the document.
     *
     * @return the extracted text
     */
    public String getExtractedText() {
        return extractedText;
    }

    /**
     * Sets the raw extracted text.
     *
     * @param extractedText the text to set
     */
    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText != null ? extractedText : "";
    }

    /**
     * Checks if parsing was successful.
     *
     * @return true if confidence is not FAILED, false otherwise
     */
    public boolean isSuccessful() {
        return confidenceLevel != ConfidenceLevel.FAILED;
    }

    /**
     * Checks if there are any errors.
     *
     * @return true if errors exist, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if there are any warnings.
     *
     * @return true if warnings exist, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Checks if any fields need manual verification.
     *
     * @return true if fields need verification, false otherwise
     */
    public boolean hasFieldsNeedingVerification() {
        return !fieldsNeedingVerification.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsingResult that = (ParsingResult) o;
        return confidenceLevel == that.confidenceLevel &&
                Objects.equals(paystub, that.paystub);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paystub, confidenceLevel);
    }

    /**
     * Returns a string representation of this parsing result.
     *
     * @return a summary of the parsing result
     */
    @Override
    public String toString() {
        return "ParsingResult{" +
                "confidenceLevel=" + confidenceLevel +
                ", fieldsNeedingVerification=" + fieldsNeedingVerification.size() +
                ", warnings=" + warnings.size() +
                ", errors=" + errors.size() +
                ", successful=" + isSuccessful() +
                '}';
    }
}
