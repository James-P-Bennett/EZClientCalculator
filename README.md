# EZ Client Calculator

A Java desktop application for analyzing paystubs to calculate qualified monthly income for mortgage lending purposes (USDA, FHA, and Conventional loans).

## Overview

This application helps mortgage loan officers quickly and accurately calculate a borrower's qualified monthly income by:
- Extracting data from paystubs (PDF and image formats)
- Performing income calculations following mortgage lending guidelines
- Providing click-to-copy functionality for easy data entry into loan documents
- Flagging potential documentation issues and income variances

## Features

- **Automated PDF Parsing**: Extract paystub data using Apache PDFBox
- **OCR Support**: Process image-based paystubs with Tesseract OCR
- **Lending Guideline Compliance**: Calculations follow USDA, FHA, and Conventional loan requirements
- **Income Analysis**:
  - Expected monthly income calculations
  - YTD pacing analysis
  - Variance detection and guardrail logic
  - Variable income averaging (overtime, commission, bonus)
- **Professional GUI**: Clean JavaFX interface optimized for financial workflows
- **Click-to-Copy**: Every result field copies to clipboard with a single click
- **Warning System**: Automatic detection of data inconsistencies and missing documentation

## System Requirements

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Operating System**: Windows, macOS, or Linux
- **Memory**: Minimum 2GB RAM recommended
- **Optional**: Tesseract OCR installed for image-based paystub processing

## Installation

### Prerequisites

1. Install Java JDK 17 or higher:
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
   - Verify installation: `java -version`

2. Install Maven:
   - Download from [Apache Maven](https://maven.apache.org/download.cgi)
   - Verify installation: `mvn -version`

3. (Optional) Install Tesseract OCR for image support:
   - Windows: Download from [UB Mannheim](https://github.com/UB-Mannheim/tesseract/wiki)
   - macOS: `brew install tesseract`
   - Linux: `sudo apt-get install tesseract-ocr`

### Building from Source

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd EZClientCalculator
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. The executable JAR will be created in the `target/` directory.

## Running the Application

### Using Maven

```bash
mvn javafx:run
```

### Using the JAR file

```bash
java -jar target/EZClientCalculator-1.0-SNAPSHOT.jar
```

## Project Structure

```
com.mortgage.paystub
├── model/          # Data classes (Paystub, Borrower, Earning, etc.)
├── parser/         # PDF and OCR parsing functionality
├── calculator/     # Income calculation logic
├── gui/            # JavaFX UI components and tabs
└── utils/          # Clipboard, validation, and formatting utilities
```

## Development

### Building

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn package

# Clean install (compile, test, and package)
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Code Style

This project follows standard Java conventions:
- Package names: lowercase
- Class names: PascalCase
- Methods/variables: camelCase
- Constants: UPPER_SNAKE_CASE

## Usage Guide

### Basic Workflow

1. **Import Paystubs**: Select PDF or image files containing paystub data
2. **Review Data**: Verify extracted information and categorize pay types
3. **Calculate Income**: View step-by-step calculations following lending guidelines
4. **Copy Results**: Click any field to copy values to your clipboard for loan documents

### Income Calculation Rules

The application follows mortgage lending guidelines:

- **Pay Frequencies**:
  - Weekly: 52 pay periods/year
  - Bi-Weekly: 26 pay periods/year
  - Semi-Monthly: 24 pay periods/year
  - Monthly: 12 pay periods/year

- **Guardrail Logic**:
  - 0-5% low: Use Expected income (verbal explanation acceptable)
  - 5-10% low: Use Expected with documented explanation, otherwise YTD
  - >10% low: Use YTD unless clearly documented reason exists

- **Variable Income**:
  - 2+ years consistent increasing: 24-month average
  - 2+ years stable: Conservative figure (lower of prior year or YTD)
  - 1-2 years: Conservative approach with possible underwriting approval

## Troubleshooting

### Build Issues

- **Java version error**: Ensure JDK 17+ is installed and JAVA_HOME is set correctly
- **Maven not found**: Verify Maven is installed and added to your PATH
- **Dependency download fails**: Check internet connection and Maven settings

### Runtime Issues

- **Application won't start**: Verify JavaFX modules are available for your platform
- **PDF parsing fails**: Ensure the PDF is not encrypted or corrupted
- **OCR not working**: Verify Tesseract is installed and accessible in PATH

## Technology Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **GUI Framework**: JavaFX 21
- **PDF Processing**: Apache PDFBox 3.0
- **OCR**: Tesseract (Tess4J)
- **Logging**: SLF4J + Logback
- **Testing**: JUnit 5

## Contributing

This is a mortgage industry application. When contributing:
1. Ensure all calculations match lending guidelines exactly
2. Add unit tests for new calculation logic
3. Maintain professional UI/UX standards
4. Document all formulas and business rules

## License

[Specify your license here]

## Support

For issues or questions:
- Create an issue in the repository
- Consult the USER_GUIDE.md for detailed usage instructions
- Review the Income Worksheet for lending guideline details

## Roadmap

- [ ] Step 1: Project Setup (Current)
- [ ] Step 2: Data Models & Core Classes
- [ ] Step 3: Income Calculation Engine
- [ ] Step 4: PDF Parsing & Data Extraction
- [ ] Step 5: Basic GUI Framework
- [ ] Step 6: Import Tab - File Selection
- [ ] Step 7: Analysis Tab - Data Review
- [ ] Step 8: Calculation Tab - Display Results
- [ ] Step 9: Results Tab - Clipboard Copy
- [ ] Step 10: Utilities & Polish
- [ ] Step 11: Testing & QA
- [ ] Step 12: Deployment

---

**Version**: 1.0-SNAPSHOT
**Last Updated**: 2026-01-05
