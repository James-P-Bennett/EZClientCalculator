# EZ Client Calculator - User Guide

**Version 1.0-SNAPSHOT**
A paystub income calculator for mortgage lending purposes

---

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Step 1: Importing Paystubs](#step-1-importing-paystubs)
4. [Step 2: Reviewing and Correcting Data](#step-2-reviewing-and-correcting-data)
5. [Step 3: Viewing Calculations](#step-3-viewing-calculations)
6. [Step 4: Copying Results](#step-4-copying-results)
7. [Understanding the Calculations](#understanding-the-calculations)
8. [Interpreting Warnings](#interpreting-warnings)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices](#best-practices)

---

## Introduction

EZ Client Calculator is designed to streamline the income calculation process for mortgage underwriters and loan officers. The application:

- Automatically extracts data from paystub PDFs and images
- Calculates qualified monthly income following USDA, FHA, and Conventional guidelines
- Applies guardrail logic to ensure conservative income estimates
- Provides click-to-copy results for easy documentation

### Key Features

- **Automated Parsing**: Extract data from PDF and image paystubs using OCR technology
- **Income Calculations**: Accurate calculations following lending guidelines
- **Guardrail Logic**:
  - 0-5% variance: Within acceptable range
  - 5-10% variance: Medium variance - documentation needed
  - >10% variance: Use YTD pacing for conservative estimate
- **Variable Income Tracking**: Separate analysis for overtime, commission, and bonuses
- **Click-to-Copy**: Every field in the results can be copied to clipboard
- **Professional Output**: Formatted results ready for loan files

---

## Getting Started

### System Requirements

- Java 17 or higher
- Windows, macOS, or Linux
- Minimum 4GB RAM recommended
- Screen resolution: 1024x768 or higher

### Launching the Application

**Using Maven:**
```bash
mvn javafx:run
```

**Using the JAR file:**
```bash
java -jar EZClientCalculator-1.0-SNAPSHOT.jar
```

---

## Step 1: Importing Paystubs

The Import tab allows you to select paystub files for processing.

### Supported File Types

- **PDF files** (.pdf)
- **Image files** (.png, .jpg, .jpeg, .tif, .tiff, .bmp)

### How to Import Files

1. **Option A: File Chooser**
   - Click the "Select Files..." button
   - Navigate to your paystub files
   - Select one or more files (Ctrl+Click for multiple)
   - Click "Open"

2. **Option B: Drag and Drop**
   - Drag files from your file explorer
   - Drop them into the dashed box area
   - Files will be automatically added to the list

### Managing Your File List

- **View Files**: The list shows all selected files with their sizes
- **Remove Files**: Click "Remove" next to any file to delete it from the list
- **Clear All**: Click "Clear All" to remove all files
- **Process**: Click "Process Files" when ready to extract data

### Tips for Best Results

- Use clear, high-quality scans (300 DPI or higher for images)
- Ensure paystubs are properly oriented (not upside down)
- Include the most recent 2-3 paystubs for best accuracy
- All paystubs should be for the same borrower

---

## Step 2: Reviewing and Correcting Data

After processing, the Analysis tab displays extracted data for review and correction.

### Understanding the Layout

**Navigation Panel (Left)**
- Lists all processed paystubs
- Click any paystub to view its details
- Current paystub is highlighted

**Data Entry Panel (Right)**
- Shows all extracted fields
- Confidence scores indicate parsing accuracy
- Editable fields for corrections

### Field Descriptions

#### Borrower Information
- **Borrower Name**: Employee name (used for consistency checking)
- **Employer Name**: Company/organization name
- **Employee ID**: Employee identification number

#### Pay Period Information
- **Pay Frequency**: How often paid (Weekly, Bi-Weekly, Semi-Monthly, Monthly)
  - **Weekly**: 52 pay periods/year
  - **Bi-Weekly**: 26 pay periods/year
  - **Semi-Monthly**: 24 pay periods/year
  - **Monthly**: 12 pay periods/year
- **Pay Date**: Date payment was issued
- **Pay Period Start**: First day of the pay period
- **Pay Period End**: Last day of the pay period

#### Earnings Breakdown
Each earning shows:
- **Pay Type Name**: Description (e.g., "Regular Pay", "Overtime", "Commission")
- **Category**: BASE_WAGE or VARIABLE
  - **Base Wage**: Regular salary/hourly, determines expected income
  - **Variable**: Overtime, commission, bonuses - calculated separately
- **Current Amount**: Payment for this pay period
- **YTD Amount**: Year-to-date total

### Making Corrections

1. **Edit Field Values**
   - Click any field to edit
   - Type the correct value
   - Press Enter or Tab to save

2. **Change Pay Categories**
   - Use dropdown to change earning category
   - **Important**: Categorization affects calculations!
   - Regular pay, salary = BASE_WAGE
   - Overtime, commission, bonuses = VARIABLE

3. **Review Confidence Scores**
   - Red (Low): Field likely needs correction
   - Yellow (Medium): Review recommended
   - Green (High): Value likely accurate

4. **Add Missing Earnings**
   - Click "Add Earning" button
   - Enter pay type, category, and amounts
   - Useful if parser missed a line item

5. **Remove Incorrect Earnings**
   - Click "Remove" next to any earning
   - Useful for duplicate or erroneous entries

### Validation Checks

The system validates:
- YTD amounts ≥ current amounts
- Dates are reasonable (not in future, not too old)
- Currency values are properly formatted
- Name consistency across paystubs

### When Ready to Calculate

Click the **"Calculate Income"** button to proceed to the Calculation tab.

---

## Step 3: Viewing Calculations

The Calculation tab shows step-by-step income calculations with formulas.

### Borrower Summary

Shows:
- Borrower name
- Employer
- Pay frequency with periods per year
- Number of paystubs analyzed

### Base Income Calculations

**Expected Monthly Income**
- Formula: Current Base Pay × Pay Periods/Year ÷ 12
- Example: $1,000 × 26 ÷ 12 = $2,166.67
- Based on most recent paystub

**YTD Monthly Pacing**
- Formula: (YTD Base / Paychecks YTD) × Pay Periods/Year ÷ 12
- Example: ($20,000 ÷ 10) × 26 ÷ 12 = $4,333.33
- Based on year-to-date performance

**Variance Analysis**
Shows the percentage difference between expected and YTD pacing:
- **Green (0-5%)**: Within acceptable range
- **Orange (5-10%)**: Medium variance - use YTD, document reason
- **Red (>10%)**: Significant variance - use YTD pacing

### Recommended Usable Base Income

The system automatically selects:
- **Expected Income**: If variance is 0-5%
- **YTD Pacing**: If variance is >5%

This follows conservative lending guidelines.

### Variable Income Analysis

For each variable income type (overtime, commission, bonuses):
- YTD Total
- Monthly Average (YTD ÷ months worked × 12)
- Included in total qualified income

### Warnings & Flags

Shows any issues detected:
- Name inconsistencies across paystubs
- Data validation warnings
- Missing information
- Unusual patterns

### Next Steps

Click **"View Results"** to see the final formatted output.

---

## Step 4: Copying Results

The Results tab provides a professional summary with click-to-copy functionality.

### Results Sections

**Section 1: Borrower Information**
- Name, employer, pay frequency
- All fields are clickable

**Section 2: Base Income Analysis**
- Expected monthly income
- YTD monthly pacing
- Variance percentage
- **RECOMMENDED USABLE BASE INCOME** (prominent)

**Section 3: Pay Type Breakdown**
- Table of all earnings with current and YTD amounts
- Click individual cells to copy

**Section 4: Variable Income Analysis**
- Details for each variable income type
- Total monthly variable income

**Section 5: Income Summary**
- Base income subtotal
- Variable income subtotal
- **TOTAL QUALIFIED MONTHLY INCOME** (very prominent)

**Section 6: Warnings & Documentation**
- List of any warnings or flags
- Documentation requirements

### How to Copy Fields

**Individual Fields:**
1. Click any field showing data
2. Value is automatically copied to clipboard
3. Visual confirmation appears ("✓ Copied!")
4. Status bar shows what was copied

**Copy All Results:**
1. Click the "Copy All Results" button
2. A formatted text summary is copied
3. Ready to paste into Word, Excel, or other documents

**Keyboard Shortcut:**
- Press **Ctrl+Shift+C** to copy all results

### Using Copied Data

The copied text is formatted for easy pasting:
- Clear section headers
- Properly aligned values
- Professional formatting
- Ready for loan documents

---

## Understanding the Calculations

### Income Calculation Methodology

The calculator follows standard mortgage lending guidelines used by USDA, FHA, and Conventional lenders.

### Base Income vs. Variable Income

**Base Income:**
- Regular wages, salaries
- Used to calculate expected monthly income
- Primary income source for qualification

**Variable Income:**
- Overtime, commission, bonuses
- Calculated separately using YTD averages
- Added to base income for total qualification

### Guardrail Logic Explained

The guardrail system protects against using inflated income estimates:

**Scenario 1: Income on Track (0-5% variance)**
- Expected income and YTD pacing are similar
- Use expected monthly income
- No documentation needed
- Example: Expected $4,000, YTD $3,900 (2.5% variance)

**Scenario 2: Medium Variance (5-10%)**
- Expected and YTD differ moderately
- Use YTD pacing (more conservative)
- Documentation required explaining variance
- Example: Expected $4,000, YTD $3,700 (7.5% variance)

**Scenario 3: Significant Variance (>10%)**
- Large difference between expected and YTD
- Use YTD pacing (much more conservative)
- Indicates income may be declining or irregular
- Example: Expected $4,000, YTD $3,400 (15% variance)

### Why This Matters

Using conservative income estimates:
- Reduces risk of over-qualification
- Complies with lending guidelines
- Provides accurate debt-to-income ratios
- Prevents loan defaults

---

## Interpreting Warnings

### Common Warnings

**"Name mismatch detected across paystubs"**
- **Cause**: Borrower name differs between paystubs
- **Action**: Verify correct name, update if needed
- **Impact**: May require additional documentation

**"YTD total is less than current amount"**
- **Cause**: Current pay exceeds year-to-date total (impossible)
- **Action**: Correct one of the values
- **Impact**: Prevents calculation until fixed

**"Pay date is in the future"**
- **Cause**: Pay date is after today's date
- **Action**: Verify and correct the date
- **Impact**: May indicate data entry error

**"Unusual pay period length detected"**
- **Cause**: Pay period length doesn't match frequency
- **Action**: Verify pay frequency and dates
- **Impact**: Affects income calculation accuracy

**"Significant variance requires documented explanation"**
- **Cause**: Income variance >5%
- **Action**: Document reason in loan file
- **Impact**: YTD pacing will be used

**"Insufficient paystubs for reliable calculation"**
- **Cause**: Only 1 paystub provided
- **Action**: Obtain additional recent paystubs
- **Impact**: Less reliable income estimate

---

## Troubleshooting

### Parsing Issues

**Problem: Text not extracted correctly**
- **Solution**: Ensure PDF is text-based, not a scanned image
- **Solution**: For images, use high-quality scans (300+ DPI)
- **Solution**: Check image orientation

**Problem: Numbers are wrong**
- **Solution**: OCR may misread similar characters (0/O, 1/I)
- **Solution**: Always review and correct in Analysis tab
- **Solution**: Use manual entry if needed

**Problem: Fields are empty**
- **Solution**: Paystub format may not be recognized
- **Solution**: Manually enter data in Analysis tab
- **Solution**: Ensure paystub is clear and readable

### Calculation Issues

**Problem: Total seems incorrect**
- **Solution**: Verify base vs. variable categorization
- **Solution**: Check YTD amounts are correct
- **Solution**: Confirm pay frequency is accurate

**Problem: Variance is unexpectedly high**
- **Solution**: Verify current amounts on most recent stub
- **Solution**: Check YTD totals are accurate
- **Solution**: Consider if income has actually decreased

### Application Issues

**Problem: Application won't start**
- **Solution**: Verify Java 17+ is installed
- **Solution**: Check system requirements
- **Solution**: Review console for error messages

**Problem: Copy to clipboard doesn't work**
- **Solution**: Check application has clipboard permissions
- **Solution**: Try keyboard shortcut (Ctrl+Shift+C)
- **Solution**: Manually select and copy if needed

---

## Best Practices

### For Best Results

1. **Use Multiple Paystubs**
   - 2-3 recent consecutive paystubs recommended
   - Provides better YTD pacing calculation
   - More reliable income estimate

2. **Verify All Data**
   - Always review extracted data carefully
   - Check confidence scores
   - Correct any errors before calculating

3. **Categorize Correctly**
   - Base wage = regular salary/hourly
   - Variable = overtime, commission, bonuses
   - Affects final calculation significantly

4. **Document Variances**
   - If variance >5%, document reason
   - Include explanation in loan file
   - Note any seasonal patterns

5. **Check Name Consistency**
   - Ensure borrower name matches across all paystubs
   - Verify against loan application
   - Note any discrepancies

### Workflow Tips

1. **Import → Review → Calculate → Copy**
   - Follow the tab sequence
   - Don't skip the review step
   - Use copy features for documentation

2. **Save Your Work**
   - Use File → Save Session for complex cases
   - Reload previous sessions as needed
   - Maintain audit trail

3. **Quality Over Speed**
   - Take time to verify data
   - Correct errors carefully
   - Ensure calculations are accurate

---

## Keyboard Shortcuts

- **Ctrl+Shift+C**: Copy all results to clipboard
- **Tab**: Move to next field
- **Shift+Tab**: Move to previous field
- **Enter**: Save field and move to next
- **Ctrl+O**: Open files (in Import tab)

---

## Getting Help

### Resources

- **User Guide**: This document
- **About Dialog**: Help → About for version info
- **In-App Tooltips**: Hover over fields for explanations

### Support

For issues or questions:
- Review this guide first
- Check the Troubleshooting section
- Consult your organization's support resources

---

## Appendix: Lending Guidelines Reference

### USDA Guidelines

- Use base salary/hourly income
- Variable income requires 2-year history
- Conservative income calculation required

### FHA Guidelines

- Base income from most recent paystub
- Variable income averaged over 2 years
- Document significant variances

### Conventional Guidelines

- Expected monthly income if stable
- YTD pacing if declining
- Variable income separately calculated

---

**EZ Client Calculator v1.0-SNAPSHOT**
© 2026 James Bennett. All Rights Reserved.

Built with JavaFX, Apache PDFBox, and Tesseract OCR
