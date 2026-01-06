# EZ Client Calculator - Quick Reference Guide

**Version 1.0-SNAPSHOT**

---

## 4-Step Workflow

```
┌─────────────┐   ┌──────────────┐   ┌────────────────┐   ┌────────────┐
│  1. IMPORT  │ → │  2. ANALYSIS │ → │ 3. CALCULATION │ → │ 4. RESULTS │
│   Paystubs  │   │ Review Data  │   │  View Formula  │   │ Copy Data  │
└─────────────┘   └──────────────┘   └────────────────┘   └────────────┘
```

---

## Common Tasks

### Import Paystubs
1. Click **"Select Files"** or drag-and-drop PDFs/images
2. Click **"Process Files"**
3. Wait for parsing to complete
4. Automatically moves to Analysis tab

### Review Extracted Data
1. Click each paystub in the list
2. Check confidence scores (Green = good, Yellow = review, Red = fix)
3. Edit any incorrect fields by clicking them
4. Click **"Calculate"** when ready

### View Calculations
1. Review **Expected Monthly Income** formula
2. Review **YTD Monthly Pacing** calculation
3. Check **Variance %** and color indicator
4. Read **Recommended Income** (guardrail result)
5. Review any **Warnings**
6. Click **"View Results"**

### Copy Results
1. Click any field to copy to clipboard
2. Or click **"Copy All"** for full summary
3. Or press **Ctrl+Shift+C**
4. Paste into loan documents

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| **Ctrl+Shift+C** | Copy all results to clipboard |
| **Ctrl+O** | Open file chooser (Import tab) |
| **F1** | Open user guide |
| **Esc** | Close dialogs |

---

## Understanding the Calculations

### Expected Monthly Income
```
Formula: (Base Pay × Pay Periods per Year) ÷ 12

Example (Bi-Weekly):
$2,000 × 26 ÷ 12 = $4,333.33
```

### YTD Monthly Pacing
```
Formula: (YTD Total ÷ Paychecks YTD) × (Pay Periods ÷ 12)

Example:
$8,000 ÷ 2 × 26 ÷ 12 = $8,666.67
```

### Variance %
```
Formula: ((Expected - YTD Pacing) ÷ Expected) × 100

Example:
(($4,333.33 - $3,900.00) ÷ $4,333.33) × 100 = 10.0%
```

---

## Pay Frequency Conversion

| Frequency | Periods/Year | Example Pay | Monthly Income |
|-----------|--------------|-------------|----------------|
| **Weekly** | 52 | $1,000 | $4,333.33 |
| **Bi-Weekly** | 26 | $2,000 | $4,333.33 |
| **Semi-Monthly** | 24 | $2,166.67 | $4,333.33 |
| **Monthly** | 12 | $4,333.33 | $4,333.33 |

---

## Guardrail Rules (Income Variance)

### ✅ Low Variance (0-5%)
- **Color**: Green
- **Action**: Use Expected Monthly Income
- **Documentation**: Verbal explanation acceptable
- **Example**: Expected $4,333, YTD Pacing $4,200 (3.1% variance)

### ⚠️ Medium Variance (5-10%)
- **Color**: Yellow
- **Action**: Use Expected only with documented explanation, otherwise use YTD
- **Documentation**: Written explanation required
- **Example**: Expected $4,333, YTD Pacing $4,000 (7.7% variance)

### 🚨 Significant Variance (>10%)
- **Color**: Red
- **Action**: Use YTD Pacing unless clearly documented reason
- **Documentation**: Strong written explanation required
- **Example**: Expected $4,333, YTD Pacing $3,800 (12.3% variance)

---

## Income Type Categories

### Base Wages (Include in Expected)
- Regular wages
- Salary
- Holiday pay
- PTO (Paid Time Off)
- Vacation pay
- Sick pay

### Variable Income (Track Separately)
- Overtime
- Commission
- Bonuses

---

## Confidence Scores

| Score | Color | Meaning | Action Required |
|-------|-------|---------|-----------------|
| **80-100%** | 🟢 Green | High confidence | Minimal review |
| **50-79%** | 🟡 Yellow | Medium confidence | Review recommended |
| **0-49%** | 🔴 Red | Low confidence | Manual review required |

---

## Common Warnings

### "Name Inconsistency Detected"
- **Cause**: Borrower name differs across paystubs
- **Action**: Verify spelling, check for maiden/married names, document if legitimate

### "Significant Income Variance (>10%)"
- **Cause**: YTD pacing is much lower than expected
- **Action**: Use YTD pacing, obtain explanation letter from borrower/employer

### "Medium Income Variance (5-10%)"
- **Cause**: YTD pacing noticeably lower than expected
- **Action**: Use YTD unless documented explanation provided

### "Missing or Incomplete Data"
- **Cause**: Parser couldn't extract critical fields
- **Action**: Manually enter missing data in Analysis tab

### "Low Confidence Extraction"
- **Cause**: Paystub format unusual or image quality poor
- **Action**: Review all fields carefully, correct as needed

---

## Troubleshooting

### Paystub Not Parsing Correctly
- ✓ Ensure PDF is text-based (not scanned image)
- ✓ For images, ensure high resolution and good lighting
- ✓ Try converting PDF to image (PNG) or vice versa
- ✓ Manually correct data in Analysis tab

### Calculation Seems Wrong
- ✓ Verify pay frequency is correct
- ✓ Check that earnings are categorized correctly (base vs. variable)
- ✓ Ensure YTD amounts are ≥ current amounts
- ✓ Confirm pay date is reasonable (not future, not too old)

### Copy to Clipboard Not Working
- ✓ Close other clipboard management tools
- ✓ Try "Copy All" button instead of individual fields
- ✓ Restart application

### Application Slow or Freezing
- ✓ Reduce number of files processed at once
- ✓ Close other memory-intensive applications
- ✓ Increase Java heap size: `java -Xmx1024m -jar EZClientCalculator.jar`

---

## Lending Guidelines Quick Reference

### USDA Rural Development
- Use most conservative income figure
- Variable income requires 2-year history
- Document all income variance

### FHA (HUD 4000.1)
- Use lesser of expected or YTD when variance exists
- Overtime/bonus requires 2-year history for averaging
- Non-taxable income gets 25% gross-up

### Conventional (Fannie Mae/Freddie Mac)
- Average variable income over 2 years
- Stable/increasing variable income is preferable
- Document declining income with explanation

---

## Best Practices

### For Accurate Results
1. Import 2-3 most recent consecutive paystubs
2. Verify all extracted data before calculating
3. Pay attention to confidence scores and warnings
4. Document variance when present
5. Double-check pay frequency selection

### For Efficient Workflow
1. Process similar borrowers in batches (all hourly, all salary)
2. Keep commonly used explanations in template document
3. Use keyboard shortcuts (Ctrl+Shift+C)
4. Review PDF quality before importing (clear, readable)

### For Compliance
1. Always document significant variance (>10%)
2. Keep copies of paystubs with loan file
3. Note any name inconsistencies
4. Follow guideline-specific requirements for variable income

---

## File Support

### Supported Formats
- ✅ PDF files (.pdf)
- ✅ PNG images (.png)
- ✅ JPEG images (.jpg, .jpeg)
- ✅ BMP images (.bmp)
- ✅ GIF images (.gif)
- ✅ TIFF images (.tiff, .tif)

### Not Supported
- ❌ Word documents (.doc, .docx)
- ❌ Excel spreadsheets (.xls, .xlsx)
- ❌ Scanned PDFs (use OCR, but slower)

---

## Data Validation Rules

### Dates
- Must be MM/dd/yyyy, M/d/yyyy, or yyyy-MM-dd format
- Must be within 2 years past to 1 month future
- Pay date must be reasonable

### Currency
- Accepts $1,234.56, 1234.56, or 1,234 formats
- Must be within reasonable range ($0 to $1,000,000)
- YTD must be ≥ current amount

### Names
- 2-100 characters
- Letters, spaces, hyphens, apostrophes only
- Should be consistent across paystubs

---

## Quick Calculations (Mental Math)

### Bi-Weekly to Monthly (×26÷12 = ×2.167)
- $1,000 → $2,167
- $2,000 → $4,333
- $3,000 → $6,500

### Weekly to Monthly (×52÷12 = ×4.333)
- $500 → $2,167
- $1,000 → $4,333
- $1,500 → $6,500

### Semi-Monthly to Monthly (×24÷12 = ×2)
- $2,000 → $4,000
- $2,500 → $5,000
- $3,000 → $6,000

---

## Support & Documentation

### Need Help?
- **F1**: Open User Guide from within application
- **Help → User Guide**: Detailed step-by-step instructions
- **README.md**: Installation and overview
- **USER_GUIDE.md**: Comprehensive 600+ line guide
- **TESTING.md**: Testing documentation
- **ARCHITECTURE.md**: Technical documentation

### Report Issues
- GitHub Issues: [https://github.com/jamesphbennett/EZClientCalculator/issues](https://github.com/jamesphbennett/EZClientCalculator/issues)

---

## Version Information

- **Current Version**: 1.0-SNAPSHOT
- **Java Required**: JDK 17 or higher
- **Platform**: Windows, macOS, Linux
- **License**: MIT License

---

**Print this page for quick desk reference**

---

**EZ Client Calculator** | Made with ☕ by James Bennett | January 2026
