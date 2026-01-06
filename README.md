# EZ Client Calculator

A professional Java desktop application for analyzing paystubs to calculate qualified monthly income for mortgage lending purposes (USDA, FHA, and Conventional loans).

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [System Requirements](#system-requirements)
4. [Installation](#installation)
5. [Quick Start Guide](#quick-start-guide)
6. [Building from Source](#building-from-source)
7. [Usage](#usage)
8. [Calculation Methodology](#calculation-methodology)
9. [Troubleshooting](#troubleshooting)
10. [FAQ](#faq)
11. [Documentation](#documentation)
12. [License](#license)
13. [Credits](#credits)

---

## Overview

EZ Client Calculator is a specialized tool designed for mortgage loan officers to streamline the process of calculating qualified monthly income from borrower paystubs. The application automates data extraction, performs accurate calculations following mortgage lending guidelines, and provides an efficient workflow for documentation.

### Problem It Solves

Mortgage loan officers traditionally spend significant time:
- Manually extracting data from paystubs
- Performing complex income calculations
- Checking for documentation inconsistencies
- Copying calculated values into loan documents

This application automates these tasks, reducing processing time from 10-15 minutes per borrower to under 2 minutes while improving accuracy and compliance.

---

## Features

### Core Functionality

- **Automated PDF Parsing**: Extract paystub data using Apache PDFBox with intelligent pattern matching
- **OCR Support**: Process image-based paystubs with Tesseract OCR
- **Multi-Paystub Analysis**: Import and analyze multiple paystubs simultaneously
- **Intelligent Data Validation**: Automatic detection of inconsistencies, missing data, and unusual values

### Income Calculations

- **Expected Monthly Income**: Accurate conversion from all pay frequencies (weekly, bi-weekly, semi-monthly, monthly)
- **YTD Pacing Analysis**: Calculate actual income pacing based on year-to-date figures
- **Variance Detection**: Guardrail logic (0-5%, 5-10%, >10%) following lending guidelines
- **Variable Income Averaging**: Proper handling of overtime, commission, and bonuses
- **Base vs. Variable Separation**: Automatic categorization of income types

### User Experience

- **Professional JavaFX GUI**: Clean interface optimized for financial workflows
- **Click-to-Copy Functionality**: Every result field copies to clipboard with a single click
- **Keyboard Shortcuts**: Ctrl+Shift+C to copy all results
- **Tab-Based Workflow**: Guided 4-step process (Import → Analysis → Calculation → Results)
- **Warning System**: Automatic flagging of potential documentation issues
- **Status Bar Feedback**: Real-time feedback on all operations

### Lending Guideline Compliance

- **USDA Guidelines**: Income calculations follow USDA requirements
- **FHA Guidelines**: Compliance with FHA income documentation rules
- **Conventional Loans**: Adherence to Fannie Mae/Freddie Mac standards
- **Guardrail Logic**: Conservative approach when income shows variance

---

## System Requirements

### Minimum Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, or Linux (Ubuntu 20.04+)
- **Java Runtime**: JDK 17 or higher
- **Memory**: 512 MB RAM
- **Disk Space**: 200 MB free space
- **Display**: 1024x768 minimum resolution

### Recommended Requirements

- **Operating System**: Windows 11 or macOS 12+
- **Java Runtime**: JDK 17 or higher
- **Memory**: 1 GB RAM
- **Disk Space**: 500 MB free space
- **Display**: 1920x1080 or higher resolution

### Development Requirements

- **Java JDK**: JDK 17 or higher
- **Maven**: 3.8.0 or higher
- **IDE**: IntelliJ IDEA, Eclipse, or VSCode (optional)
- **Git**: For version control

---

## Installation

### Option 1: Download Executable JAR (Recommended)

1. **Download the latest release**:
   - Visit the [Releases](https://github.com/jamesphbennett/EZClientCalculator/releases) page
   - Download `EZClientCalculator-1.0-SNAPSHOT-shaded.jar`

2. **Ensure Java 17+ is installed**:
   ```bash
   java -version
   ```
   If not installed, download from [Adoptium](https://adoptium.net/)

3. **Run the application**:
   ```bash
   java -jar EZClientCalculator-1.0-SNAPSHOT-shaded.jar
   ```

### Option 2: Build from Source

See [Building from Source](#building-from-source) section below.

---

## Quick Start Guide

### Basic Workflow

1. **Launch the application**
2. **Import paystubs** (Tab 1):
   - Click "Select Files" or drag-and-drop PDF/image files
   - Click "Process Files" to extract data
3. **Review and correct data** (Tab 2):
   - Click each paystub to review extracted data
   - Correct any parsing errors
   - Click "Calculate" when ready
4. **View calculations** (Tab 3):
   - Review expected income, YTD pacing, and variance
   - Check warnings and guardrail recommendations
   - Click "View Results" to proceed
5. **Copy results** (Tab 4):
   - Click any field to copy to clipboard
   - Use "Copy All" button for complete summary
   - Paste into loan documents

### First-Time Setup

1. **Test with sample paystubs**: Use the sample files in `src/test/resources/` to familiarize yourself with the workflow
2. **Review the User Guide**: See `USER_GUIDE.md` for detailed instructions
3. **Practice the workflow**: Import → Analysis → Calculation → Results

---

## Building from Source

### Prerequisites

1. **Install Java JDK 17+**:
   ```bash
   java -version
   # Should show version 17 or higher
   ```

2. **Install Maven 3.8+**:
   ```bash
   mvn -version
   # Should show Maven 3.8.0 or higher
   ```

3. **Clone the repository**:
   ```bash
   git clone https://github.com/jamesphbennett/EZClientCalculator.git
   cd EZClientCalculator
   ```

### Build Commands

**Compile and run tests**:
```bash
mvn clean test
```

**Build executable JAR**:
```bash
mvn clean package
```
The executable JAR will be created at:
- `target/EZClientCalculator-1.0-SNAPSHOT-shaded.jar`

**Run from Maven**:
```bash
mvn javafx:run
```

**Run tests only**:
```bash
mvn test
```

**Run specific test class**:
```bash
mvn test -Dtest=IncomeCalculatorTest
```

**Clean build directory**:
```bash
mvn clean
```

---

## Usage

### Importing Paystubs

**Supported File Types**:
- PDF files (.pdf)
- Images (PNG, JPG, JPEG, BMP, GIF, TIFF)

**Import Methods**:
- **File Chooser**: Click "Select Files" button
- **Drag-and-Drop**: Drag files directly onto the import area

**Best Practices**:
- Import 2-3 recent paystubs for best accuracy
- Ensure paystubs are clear and readable
- Prefer PDF format for better parsing accuracy

### Reviewing Extracted Data

**Data Fields**:
- **Borrower Name**: Employee name (should be consistent)
- **Employer**: Company name
- **Pay Date**: Date of paystub
- **Pay Frequency**: Weekly, Bi-Weekly, Semi-Monthly, or Monthly
- **Earnings**: List of all pay types with current and YTD amounts

**Confidence Scores**:
- **Green (>80%)**: High confidence in extraction
- **Yellow (50-80%)**: Medium confidence - review recommended
- **Red (<50%)**: Low confidence - manual review required

**Corrections**:
- Click any field to edit manually
- Use "Add Earning" to add missing income types
- Use "Remove Earning" to delete incorrect entries

### Understanding Calculations

**Expected Monthly Income**:
```
For hourly/salary: (Regular Pay × Pay Periods) ÷ 12
```

**YTD Monthly Pacing**:
```
(YTD Total ÷ Pay Periods Elapsed) × (Total Periods per Year ÷ 12)
```

**Variance Calculation**:
```
((Expected - YTD Pacing) ÷ Expected) × 100
```

**Guardrail Rules**:
- **0-5% variance**: Use Expected income (conservative, no documentation needed)
- **5-10% variance**: Use Expected with documented explanation, otherwise use YTD
- **>10% variance**: Use YTD pacing unless clearly documented reason exists

### Copying Results

**Individual Fields**:
- Click any field in the Results tab to copy to clipboard
- Status bar shows what was copied

**Copy All**:
- Click "Copy All" button to copy formatted summary
- Or use keyboard shortcut: **Ctrl+Shift+C**

**Clipboard Format**:
- Formatted with proper spacing for easy pasting
- Includes borrower information, calculations, and warnings
- Ready to paste into Word, Excel, or other loan documents

---

## Calculation Methodology

### Pay Frequency Conversion

The application uses the following periods per year:
- **Weekly**: 52 periods
- **Bi-Weekly**: 26 periods
- **Semi-Monthly**: 24 periods
- **Monthly**: 12 periods

### Base Wage Categories

Included in base income:
- Regular wages/salary
- Holiday pay
- PTO (Paid Time Off)
- Vacation pay
- Sick pay

### Variable Income Categories

Tracked separately and averaged:
- Overtime
- Commission
- Bonuses

### Income Guardrails

The application implements a conservative guardrail system:

1. **Low Variance (0-5%)**:
   - YTD pacing is within 5% of expected
   - **Action**: Use expected monthly income
   - **Documentation**: Verbal explanation acceptable

2. **Medium Variance (5-10%)**:
   - YTD pacing is 5-10% lower than expected
   - **Action**: Use expected only with documented explanation, otherwise use YTD
   - **Documentation**: Written explanation required

3. **Significant Variance (>10%)**:
   - YTD pacing is more than 10% lower than expected
   - **Action**: Use YTD pacing unless clearly documented reason exists
   - **Documentation**: Strong written explanation required

### Variable Income Averaging

For overtime, commission, and bonuses:
- **2+ years consistent/increasing**: Use 24-month average
- **2+ years not increasing but YTD paces to prior year**: Use lower of prior year or YTD
- **1-2 years available**: Conservative figure, may need approval for aggressive amounts
- **Less than 1 year**: Generally not included unless fully documented

---

## Troubleshooting

### Application Won't Start

**Problem**: Double-clicking JAR does nothing

**Solutions**:
1. Ensure Java 17+ is installed: `java -version`
2. Run from command line: `java -jar EZClientCalculator-1.0-SNAPSHOT-shaded.jar`
3. Check for error messages in terminal
4. Try reinstalling Java from [Adoptium](https://adoptium.net/)

### PDF Parsing Issues

**Problem**: Paystub data not extracted correctly

**Solutions**:
1. Ensure PDF is text-based, not scanned image
2. For scanned PDFs, OCR may be slow - be patient
3. Manually correct extracted data in Analysis tab
4. Try converting PDF to image format (PNG/JPG)
5. Check confidence scores - yellow/red indicates uncertain extraction

### Calculation Errors

**Problem**: Calculated income doesn't match manual calculation

**Solutions**:
1. Verify pay frequency is correct
2. Check that all earnings are categorized correctly (base vs. variable)
3. Ensure YTD amounts are greater than or equal to current amounts
4. Review pay date - ensure it's reasonable (not in future, not too old)
5. Check the Calculation tab for formula breakdown

### Memory Issues

**Problem**: Application crashes or runs slowly with large files

**Solutions**:
1. Increase Java heap size: `java -Xmx1024m -jar EZClientCalculator-1.0-SNAPSHOT-shaded.jar`
2. Process fewer files at once
3. Close other applications to free memory
4. Restart the application

### Clipboard Issues

**Problem**: Copy to clipboard not working

**Solutions**:
1. Ensure clipboard permissions are granted (macOS/Linux)
2. Try closing other clipboard management tools
3. Restart the application
4. Try "Copy All" button instead of individual field clicks

---

## FAQ

### General Questions

**Q: Is this application free to use?**
A: Yes, this application is open-source. See LICENSE file for details.

**Q: Does this work on Mac/Linux?**
A: Yes, it's built with Java and JavaFX, so it works on Windows, macOS, and Linux.

**Q: Does this application store or transmit borrower data?**
A: No, all data processing is done locally. No data is stored or transmitted.

**Q: Can I use this for non-mortgage purposes?**
A: While designed for mortgage lending, it can be used for any paystub income analysis.

### Calculation Questions

**Q: Why does the calculator recommend using YTD instead of expected income?**
A: When YTD pacing is more than 10% below expected, guidelines require using the more conservative YTD figure unless there's documented justification.

**Q: How are bonuses and commissions handled?**
A: Variable income is tracked separately and typically averaged over 24 months if available and consistent.

**Q: What if the paystub doesn't show YTD amounts?**
A: You'll need to manually enter YTD amounts or obtain paystubs that include YTD data. The calculation requires YTD for guardrail analysis.

**Q: Can I override the guardrail recommendation?**
A: The application provides recommendations based on guidelines. Final decision is yours as the loan officer, but documentation requirements still apply.

### Technical Questions

**Q: Which Java version do I need?**
A: Java 17 or higher is required. Download from [Adoptium](https://adoptium.net/).

**Q: Why is PDF parsing slow?**
A: Large PDF files with complex layouts may take a few seconds to parse. OCR on scanned images takes longer (10-30 seconds per page).

**Q: Can I process multiple borrowers at once?**
A: The application processes one borrower at a time. Start a new session for each borrower.

**Q: How do I report a bug or request a feature?**
A: Open an issue on the [GitHub repository](https://github.com/jamesphbennett/EZClientCalculator/issues).

---

## Documentation

This project includes comprehensive documentation:

- **USER_GUIDE.md**: Detailed user guide with step-by-step instructions
- **TESTING.md**: Testing documentation and test coverage information
- **ARCHITECTURE.md**: Technical architecture and code organization
- **CHANGELOG.md**: Version history and feature changes
- **QUICK_REFERENCE.md**: One-page quick reference guide

---

## License

Copyright (c) 2026 James Bennett

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Credits

### Author

**James Bennett**
- GitHub: [@jamesphbennett](https://github.com/jamesphbennett)
- Email: (contact via GitHub)

### Technologies

This application is built with:
- **Java 17**: Core programming language
- **JavaFX 21**: GUI framework
- **Apache PDFBox 3.0**: PDF parsing
- **Tesseract 4 (Tess4J 5.9)**: OCR for image-based paystubs
- **SLF4J + Logback**: Logging framework
- **JUnit 5**: Testing framework
- **Maven**: Build and dependency management

### Acknowledgments

- Mortgage lending guidelines based on USDA, FHA, and Fannie Mae/Freddie Mac standards
- Income calculation methodology follows industry best practices
- Special thanks to the loan officer community for feedback and requirements

---

## Project Status

**Current Version**: 1.0-SNAPSHOT
**Status**: Production-Ready
**Last Updated**: January 6, 2026

### Test Coverage

- **Total Tests**: 89
- **Success Rate**: 100%
- **Coverage**: >80% of core logic

### Key Features Status

- ✅ PDF paystub parsing
- ✅ OCR for image-based paystubs
- ✅ Income calculations with guardrails
- ✅ Variable income averaging
- ✅ Warning system
- ✅ Click-to-copy functionality
- ✅ Comprehensive testing
- ✅ User documentation

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please ensure:
- All tests pass (`mvn test`)
- Code follows existing style
- Documentation is updated
- New features include tests

---

## Support

For issues, questions, or suggestions:
- **GitHub Issues**: [Open an issue](https://github.com/jamesphbennett/EZClientCalculator/issues)
- **Email**: Contact via GitHub profile

---

**Made with ☕ by James Bennett**
