package com.mortgage.paystub.parser;

import java.io.File;
import java.io.IOException;

/**
 * Interface for paystub parsers.
 * Defines the contract for extracting data from paystub documents in various formats.
 *
 * @author James Bennett
 * @version 1.0
 */
public interface PaystubParser {

    /**
     * Parses a paystub file and extracts the data.
     *
     * @param file the paystub file to parse
     * @return a ParsingResult containing the extracted paystub and metadata
     * @throws IOException if an error occurs reading the file
     * @throws IllegalArgumentException if the file is null or invalid
     */
    ParsingResult parse(File file) throws IOException;

    /**
     * Extracts text from a paystub file.
     *
     * @param file the file to extract text from
     * @return the extracted text
     * @throws IOException if an error occurs reading the file
     */
    String extractText(File file) throws IOException;

    /**
     * Checks if this parser supports the given file type.
     *
     * @param file the file to check
     * @return true if the parser supports this file type, false otherwise
     */
    boolean supports(File file);

    /**
     * Gets the name of this parser.
     *
     * @return the parser name
     */
    String getParserName();
}
