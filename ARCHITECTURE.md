# EZ Client Calculator - Technical Architecture

**Version 1.0-SNAPSHOT**
**Last Updated: January 6, 2026**

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Package Structure](#package-structure)
4. [Core Components](#core-components)
5. [Data Flow](#data-flow)
6. [Calculation Formulas](#calculation-formulas)
7. [Design Patterns](#design-patterns)
8. [Testing Strategy](#testing-strategy)
9. [Extension Points](#extension-points)
10. [Build and Deployment](#build-and-deployment)

---

## System Overview

### Purpose

EZ Client Calculator is a desktop application built with JavaFX to analyze paystubs and calculate qualified monthly income for mortgage lending purposes. The application follows a guided 4-step workflow optimized for loan officer efficiency.

### Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21.0.1
- **PDF Processing**: Apache PDFBox 3.0.1
- **OCR**: Tesseract 4 (via Tess4J 5.9.0)
- **Logging**: SLF4J 2.0.9 + Logback 1.4.14
- **Testing**: JUnit 5.10.1
- **Build Tool**: Maven 3.8.0+

### Key Design Principles

1. **Separation of Concerns**: Clear separation between model, view, and business logic
2. **Immutability**: Data models use final fields where possible
3. **Null Safety**: Comprehensive null checking with graceful defaults
4. **Testability**: All business logic is unit-testable
5. **User-Centric Design**: UI optimized for loan officer workflow

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     JavaFX Application Layer                    │
│                  (PaystubCalculatorApp)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ├───────────────┬───────────────┬──────────────┐
                              ▼               ▼               ▼              ▼
┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐
│   Import Tab     │  │ Analysis Tab │  │Calculation Tab│  │ Results Tab │
│  (ImportTab)     │  │(AnalysisTab) │  │(CalculationTab│  │(ResultsTab) │
└──────────────────┘  └──────────────┘  └──────────────┘  └─────────────┘
         │                    │                   │                │
         ▼                    ▼                   ▼                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Business Logic Layer                          │
│  ┌───────────────────┐  ┌──────────────────┐                    │
│  │ PDFPaystubParser  │  │ IncomeCalculator │                    │
│  └───────────────────┘  └──────────────────┘                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Model Layer                          │
│  ┌──────────┐  ┌─────────┐  ┌─────────┐  ┌──────────────────┐  │
│  │ Borrower │  │ Paystub │  │ Earning │  │IncomeCalculation │  │
│  └──────────┘  └─────────┘  └─────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Utilities Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐│
│  │ ValidationUtil  │  │ FormattingUtil  │  │   StatusBar      ││
│  └─────────────────┘  └─────────────────┘  └──────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## Package Structure

### com.mortgage.paystub
Root package containing the main application class.

**Classes**:
- `PaystubCalculatorApp.java` - Main JavaFX application class
- `Main.java` - JAR entry point for executable distribution

### com.mortgage.paystub.model
Domain model classes representing business entities.

**Classes**:
- `Borrower.java` - Represents a borrower with multiple paystubs
- `Paystub.java` - Represents a single paystub with earnings
- `Earning.java` - Represents a single earning line item (pay type)
- `PayFrequency.java` - Enum for pay frequency (WEEKLY, BI_WEEKLY, etc.)
- `IncomeCalculation.java` - Result object containing all calculations and warnings

**Relationships**:
```
Borrower 1──* Paystub
Paystub 1──* Earning
Paystub 1──1 PayFrequency
```

### com.mortgage.paystub.calculator
Business logic for income calculations.

**Classes**:
- `IncomeCalculator.java` - Core calculation engine implementing guardrail logic

**Responsibilities**:
- Convert pay frequency to monthly income
- Calculate YTD pacing
- Determine variance percentage
- Apply guardrail rules
- Separate base vs. variable income
- Generate warnings

### com.mortgage.paystub.parser
PDF and image parsing logic.

**Classes**:
- `PDFPaystubParser.java` - Extract data from PDF paystubs using PDFBox and OCR

**Responsibilities**:
- Text extraction from PDFs
- OCR for image-based PDFs
- Pattern matching for dates, currency, names
- Confidence scoring for extracted data
- Auto-detection of pay frequency

### com.mortgage.paystub.gui
JavaFX GUI components and controllers.

**Classes**:
- `StatusBar.java` - Status bar component for user feedback

### com.mortgage.paystub.gui.tabs
Tab implementations for the main workflow.

**Classes**:
- `ImportTab.java` - File import and processing interface
- `AnalysisTab.java` - Paystub data review and editing interface
- `CalculationTab.java` - Income calculation display interface
- `ResultsTab.java` - Final results with click-to-copy functionality

### com.mortgage.paystub.gui.dialogs
Dialog components.

**Classes**:
- `AboutDialog.java` - About dialog with application information

### com.mortgage.paystub.utils
Utility classes for common operations.

**Classes**:
- `ValidationUtil.java` - Data validation methods (dates, currency, names, etc.)
- `FormattingUtil.java` - Formatting methods (currency, dates, numbers, etc.)

---

## Core Components

### PaystubCalculatorApp

**Type**: JavaFX Application
**Purpose**: Main application entry point and window management

**Key Responsibilities**:
- Initialize JavaFX application window
- Create menu bar (File, Edit, Help)
- Create tab pane with all workflow tabs
- Wire tabs together for navigation
- Manage global exception handling
- Provide status bar for user feedback

**Lifecycle**:
1. `main()` - Launch JavaFX application
2. `start()` - Initialize UI components
3. User interaction - Process events
4. `stop()` - Clean shutdown

### Borrower

**Type**: Data Model
**Purpose**: Represent a borrower with their paystubs

**Key Fields**:
- `name: String` - Borrower's full name
- `paystubs: List<Paystub>` - Collection of paystubs
- `employer: String` - Employer name (derived from paystubs)

**Key Methods**:
- `addPaystub(Paystub)` - Add paystub to borrower
- `getPaystubs()` - Get all paystubs
- `getEmployer()` - Get employer from first paystub

**Validation**:
- Name must be non-empty
- At least one paystub required for calculations

### Paystub

**Type**: Data Model
**Purpose**: Represent a single paystub with all earning details

**Key Fields**:
- `employeeName: String` - Employee name on paystub
- `employer: String` - Employer/company name
- `payDate: LocalDate` - Pay date
- `payPeriodStart: LocalDate` - Pay period start date
- `payPeriodEnd: LocalDate` - Pay period end date
- `payFrequency: PayFrequency` - Pay frequency enum
- `earnings: List<Earning>` - Collection of earning line items
- `confidenceScore: double` - Parser confidence (0-100)
- `sourceFile: String` - Original filename

**Key Methods**:
- `addEarning(Earning)` - Add earning line item
- `getEarnings()` - Get all earnings
- `getTotalCurrent()` - Sum of all current amounts
- `getTotalYtd()` - Sum of all YTD amounts

**Validation**:
- Pay date within reasonable range (2 years past to 1 month future)
- YTD amounts ≥ current amounts
- Pay frequency must be valid enum

### Earning

**Type**: Data Model
**Purpose**: Represent a single earning line item on a paystub

**Key Fields**:
- `description: String` - Pay type description (e.g., "Regular", "Overtime")
- `currentAmount: BigDecimal` - Current pay period amount
- `ytdAmount: BigDecimal` - Year-to-date amount
- `isBaseWage: boolean` - True if base wage, false if variable income

**Key Methods**:
- `isVariableIncome()` - Check if this is variable income (overtime, commission, bonus)
- `isBaseWage()` - Check if this is base wage
- `getCurrent()` - Get current amount
- `getYtd()` - Get YTD amount

**Classification Logic**:
- **Base Wage**: Regular, Salary, Holiday, PTO, Vacation, Sick
- **Variable Income**: Overtime, Commission, Bonus

### IncomeCalculation

**Type**: Result Object
**Purpose**: Contain all calculation results and warnings

**Key Fields**:
- `expectedMonthlyIncome: BigDecimal` - (Base Pay × Periods) ÷ 12
- `ytdMonthlyPacing: BigDecimal` - (YTD ÷ Periods Elapsed) × (Periods/Year ÷ 12)
- `variancePercentage: BigDecimal` - ((Expected - YTD) ÷ Expected) × 100
- `recommendedIncome: BigDecimal` - Income to use based on guardrails
- `paychecksYtd: int` - Number of paychecks received year-to-date
- `totalPayPeriods: int` - Total pay periods per year
- `variableIncomeMonthly: BigDecimal` - Variable income monthly average
- `warnings: List<String>` - List of warning messages
- `hasSignificantVariance: boolean` - True if variance >10%
- `hasMediumVariance: boolean` - True if variance 5-10%
- `isOnTrack: boolean` - True if variance <5%

**Key Methods**:
- `getExpectedMonthly()` - Get expected monthly income
- `getYtdMonthlyPacing()` - Get YTD pacing
- `getVariancePercentage()` - Get variance percentage
- `getRecommendedIncome()` - Get recommended income (implements guardrails)
- `getWarnings()` - Get all warnings
- `addWarning(String)` - Add warning message

### IncomeCalculator

**Type**: Business Logic
**Purpose**: Core calculation engine implementing all income formulas

**Key Methods**:

```java
public static IncomeCalculation calculateIncome(Borrower borrower)
```
- Main entry point for all calculations
- Takes a borrower with paystubs
- Returns complete IncomeCalculation result

**Internal Methods**:

```java
private static BigDecimal calculateExpectedMonthlyIncome(Paystub paystub)
```
- Formula: `(Base Pay × Pay Periods per Year) ÷ 12`
- Sums all base wage earnings
- Converts to monthly based on pay frequency

```java
private static BigDecimal calculateYtdMonthlyPacing(Paystub paystub, int paychecksYtd)
```
- Formula: `(YTD Total ÷ Paychecks YTD) × (Total Periods ÷ 12)`
- Calculates actual income pacing from YTD data

```java
private static int determinePaychecksYtd(LocalDate payDate, PayFrequency frequency)
```
- Calculates number of paychecks received year-to-date
- Accounts for different pay frequencies

```java
private static BigDecimal calculateVariancePercentage(BigDecimal expected, BigDecimal ytd)
```
- Formula: `((Expected - YTD) ÷ Expected) × 100`
- Returns percentage variance (positive = YTD below expected)

**Guardrail Logic**:

```java
private static BigDecimal determineRecommendedIncome(
    BigDecimal expected,
    BigDecimal ytdPacing,
    BigDecimal variance
)
```

Implementation:
```java
if (variance < 5.0) {
    // Low variance - use expected
    return expected;
} else if (variance < 10.0) {
    // Medium variance - use YTD (conservative)
    return ytdPacing;
} else {
    // Significant variance - use YTD
    return ytdPacing;
}
```

**Variable Income Handling**:

```java
private static BigDecimal calculateVariableIncomeMonthly(Borrower borrower)
```
- Separates overtime, commission, bonuses
- Currently returns simple monthly average
- Future: Implement 24-month averaging

### PDFPaystubParser

**Type**: Parser
**Purpose**: Extract data from PDF and image paystubs

**Key Methods**:

```java
public Paystub parsePaystub(File file) throws IOException
```
- Main entry point for parsing
- Detects PDF vs. image format
- Returns populated Paystub object with confidence score

**Internal Methods**:

```java
private String extractTextFromPDF(PDDocument document)
```
- Extracts text from PDF using PDFBox PDFTextStripper
- Handles multi-page documents

```java
private String extractTextFromImage(File imageFile)
```
- Uses Tesseract OCR to extract text from images
- Slower but handles scanned paystubs

**Pattern Matching**:

```java
private String extractName(String text)
```
- Regex patterns for common name formats
- Confidence scoring based on match quality

```java
private LocalDate extractPayDate(String text)
```
- Multiple date format patterns (MM/dd/yyyy, M/d/yyyy, yyyy-MM-dd)
- Validates reasonable date range

```java
private BigDecimal extractCurrency(String text)
```
- Patterns for $1,234.56, 1234.56, -$1,234.56
- Returns BigDecimal for precision

```java
private PayFrequency detectPayFrequency(String text)
```
- Keyword matching for "weekly", "bi-weekly", "semi-monthly", "monthly"
- Defaults to BI_WEEKLY if uncertain

**Confidence Scoring**:
- 100% = All fields extracted with high confidence
- 75-99% = Most fields extracted, some uncertain
- 50-74% = Partial extraction, manual review recommended
- <50% = Poor extraction, manual entry likely needed

---

## Data Flow

### Complete Workflow

```
1. Import Tab
   ├─ User selects files
   ├─ PDFPaystubParser.parsePaystub(file)
   ├─ Creates Paystub objects
   ├─ Stores in AnalysisTab
   └─ Switches to Analysis Tab

2. Analysis Tab
   ├─ Displays list of paystubs
   ├─ User selects paystub to review
   ├─ User edits fields if needed
   ├─ User clicks Calculate button
   ├─ Creates Borrower object with paystubs
   ├─ Passes to CalculationTab
   └─ Switches to Calculation Tab

3. Calculation Tab
   ├─ Receives Borrower object
   ├─ Calls IncomeCalculator.calculateIncome(borrower)
   ├─ Receives IncomeCalculation result
   ├─ Displays expected income, YTD pacing, variance
   ├─ Shows guardrail recommendation
   ├─ Displays warnings
   ├─ User clicks View Results
   ├─ Passes calculation to ResultsTab
   └─ Switches to Results Tab

4. Results Tab
   ├─ Receives Borrower and IncomeCalculation
   ├─ Formats results using FormattingUtil
   ├─ Displays borrower info, calculations, warnings
   ├─ User clicks fields to copy to clipboard
   └─ User pastes into loan documents
```

### Data Transformations

```
File (PDF/Image)
    ↓ PDFPaystubParser
Paystub (raw extracted data)
    ↓ User review/editing
Paystub (validated data)
    ↓ Grouped by borrower
Borrower (collection of paystubs)
    ↓ IncomeCalculator
IncomeCalculation (all results)
    ↓ FormattingUtil
Formatted Text (clipboard-ready)
```

---

## Calculation Formulas

### Expected Monthly Income

**Purpose**: Convert any pay frequency to monthly income

**Formula**:
```
Expected Monthly = (Base Pay Amount × Pay Periods per Year) ÷ 12
```

**Pay Periods per Year**:
- Weekly: 52
- Bi-Weekly: 26
- Semi-Monthly: 24
- Monthly: 12

**Example** (Bi-Weekly):
```
Regular Pay: $2,000
Pay Frequency: Bi-Weekly (26 periods)
Expected Monthly = ($2,000 × 26) ÷ 12 = $4,333.33
```

### YTD Monthly Pacing

**Purpose**: Calculate actual income pacing from year-to-date figures

**Formula**:
```
YTD Pacing = (YTD Total ÷ Paychecks YTD) × (Total Periods per Year ÷ 12)
```

**Paychecks YTD Calculation**:
```
For Bi-Weekly (26 periods per year):
January 6, 2026 is day 6 of year
Estimated paychecks = floor(day of year / 14) = floor(6/14) = 0
(Simplified - actual implementation more complex)
```

**Example**:
```
YTD Amount: $8,000
Paychecks YTD: 2
Pay Frequency: Bi-Weekly (26 periods)
YTD Pacing = ($8,000 ÷ 2) × (26 ÷ 12) = $8,666.67
```

### Variance Percentage

**Purpose**: Measure difference between expected and YTD pacing

**Formula**:
```
Variance % = ((Expected - YTD Pacing) ÷ Expected) × 100
```

**Example**:
```
Expected: $4,333.33
YTD Pacing: $3,900.00
Variance = (($4,333.33 - $3,900.00) ÷ $4,333.33) × 100
Variance = 10.0%
```

**Interpretation**:
- Positive %: YTD pacing is below expected (income dropping)
- Negative %: YTD pacing is above expected (income increasing)
- 0%: Perfectly on track

### Guardrail Recommended Income

**Purpose**: Determine which income figure to use based on variance

**Decision Tree**:
```
if (variance < 5%) {
    return expected;  // Low variance - conservative to use expected
} else if (variance < 10%) {
    return ytdPacing;  // Medium variance - use actual pacing
} else {
    return ytdPacing;  // Significant variance - must use pacing
}
```

**Rationale**:
- **0-5%**: Within normal fluctuation; expected is conservative
- **5-10%**: Noticeable drop; YTD more accurate unless documented explanation
- **>10%**: Significant drop; must use YTD unless strong documentation

### Variable Income Monthly

**Purpose**: Calculate monthly average of overtime, commission, bonuses

**Current Implementation**:
```
Variable Monthly = (Variable YTD ÷ Paychecks YTD) × (Periods per Year ÷ 12)
```

**Future Enhancement** (24-month averaging):
```
If (has 24 months of data) {
    Variable Monthly = Sum of last 24 months ÷ 24
} else if (has 12+ months) {
    Variable Monthly = Average of available months (conservative)
} else {
    Variable Monthly = 0 (insufficient history)
}
```

---

## Design Patterns

### Model-View-Controller (MVC)

**Model**: Data classes in `com.mortgage.paystub.model`
**View**: JavaFX components in `com.mortgage.paystub.gui`
**Controller**: Logic in `IncomeCalculator`, `PDFPaystubParser`

### Builder Pattern

Used in constructing complex objects (e.g., Paystub):
```java
Paystub paystub = new Paystub.Builder()
    .employeeName(name)
    .employer(employer)
    .payDate(payDate)
    .payFrequency(frequency)
    .build();
```

(Note: Currently using standard constructors, but builder pattern would be beneficial for future refactoring)

### Observer Pattern

Tab navigation uses callbacks:
```java
importTab.setOnProcessComplete(() -> {
    tabPane.getSelectionModel().select(analysisTab);
});
```

### Singleton Pattern

Utility classes use static methods (stateless singletons):
- `ValidationUtil`
- `FormattingUtil`

### Strategy Pattern

Income calculation could be extended with different calculation strategies:
```java
interface CalculationStrategy {
    IncomeCalculation calculate(Borrower borrower);
}

class USDACalculationStrategy implements CalculationStrategy { ... }
class FHACalculationStrategy implements CalculationStrategy { ... }
```

(Note: Currently single strategy; future enhancement for guideline-specific calculations)

---

## Testing Strategy

### Unit Testing

**Framework**: JUnit 5 (Jupiter)
**Coverage**: >80% of core business logic

**Test Classes**:
- `IncomeCalculatorTest` - 26 tests for all calculation scenarios
- `ModelTest` - 9 tests for data models
- `PDFPaystubParserTest` - 25 tests for parsing logic
- `ValidationUtilTest` - 15 tests for validation methods
- `FormattingUtilTest` - 14 tests for formatting methods

**Testing Approach**:
1. **Arrange**: Set up test data
2. **Act**: Execute method under test
3. **Assert**: Verify expected results

**Example**:
```java
@Test
@DisplayName("Should calculate expected monthly income correctly for bi-weekly pay")
void testCalculateIncome_BiWeekly() {
    // Arrange
    Borrower borrower = createTestBorrower(PayFrequency.BI_WEEKLY, 2000.0);

    // Act
    IncomeCalculation calc = IncomeCalculator.calculateIncome(borrower);

    // Assert
    assertEquals(new BigDecimal("4333.33"), calc.getExpectedMonthly());
}
```

### Integration Testing

**Manual Testing**: See TESTING.md for comprehensive checklist

**Key Scenarios**:
- Complete workflow from import to results
- Multiple paystubs for same borrower
- Edge cases (missing data, unusual formats)
- Performance with large files

### Test Data

Located in `src/test/resources/`:
- Sample PDF paystubs
- Sample image paystubs
- Edge case examples

---

## Extension Points

### Adding New Pay Types

**Location**: `Earning.java`

**Steps**:
1. Update `isBaseWage()` logic to include new base wage types
2. Update `isVariableIncome()` logic to include new variable types
3. Add tests in `ModelTest.java`

**Example**:
```java
public boolean isBaseWage() {
    String lower = description.toLowerCase();
    return lower.contains("regular")
        || lower.contains("salary")
        || lower.contains("new-pay-type");  // Add new type
}
```

### Adding New Pay Frequencies

**Location**: `PayFrequency.java`

**Steps**:
1. Add new enum value with periods per year
2. Update `IncomeCalculator.determinePaychecksYtd()` logic
3. Update `PDFPaystubParser.detectPayFrequency()` pattern matching
4. Add tests for new frequency

**Example**:
```java
public enum PayFrequency {
    WEEKLY(52),
    BI_WEEKLY(26),
    SEMI_MONTHLY(24),
    MONTHLY(12),
    QUARTERLY(4);  // New frequency

    private final int periodsPerYear;
}
```

### Adding New Calculation Rules

**Location**: `IncomeCalculator.java`

**Steps**:
1. Add new method for specific calculation
2. Call from `calculateIncome()` method
3. Add result field to `IncomeCalculation`
4. Update UI to display new calculation
5. Add comprehensive tests

**Example** (Self-Employment Income):
```java
public static BigDecimal calculateSelfEmploymentIncome(TaxReturn[] returns) {
    // Average last 2 years of Schedule C profit
    BigDecimal total = BigDecimal.ZERO;
    for (TaxReturn ret : returns) {
        total = total.add(ret.getScheduleCProfit());
    }
    return total.divide(new BigDecimal(returns.length), 2, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
}
```

### Adding New Validation Rules

**Location**: `ValidationUtil.java`

**Steps**:
1. Add new static validation method
2. Call from appropriate location (parser, model, calculator)
3. Add tests in `ValidationUtilTest.java`

**Example**:
```java
public static boolean isValidSSN(String ssn) {
    if (ssn == null || ssn.isEmpty()) {
        return false;
    }
    String pattern = "^\\d{3}-\\d{2}-\\d{4}$";
    return ssn.matches(pattern);
}
```

### Adding New GUI Components

**Location**: `com.mortgage.paystub.gui`

**Steps**:
1. Create new JavaFX component class
2. Add to appropriate tab or create new tab
3. Wire with existing components
4. Update navigation if needed
5. Manual UI testing

**Example** (Borrower Info Dialog):
```java
public class BorrowerInfoDialog extends Dialog<Borrower> {
    public BorrowerInfoDialog() {
        setTitle("Borrower Information");
        // Add fields for name, SSN, phone, etc.
        // Return Borrower object when OK clicked
    }
}
```

### Adding Export Functionality

**Location**: New package `com.mortgage.paystub.export`

**Steps**:
1. Create `ExportService` interface
2. Implement `PDFExporter`, `ExcelExporter`, etc.
3. Add export button to Results tab
4. Allow user to choose export format
5. Add tests for export logic

**Example**:
```java
public interface ExportService {
    void export(IncomeCalculation calculation, File outputFile) throws IOException;
}

public class PDFExporter implements ExportService {
    @Override
    public void export(IncomeCalculation calc, File file) throws IOException {
        // Use iText or Apache PDFBox to create PDF report
    }
}
```

### Adding Database Persistence

**Location**: New package `com.mortgage.paystub.persistence`

**Steps**:
1. Add database dependency (H2, SQLite, etc.) to pom.xml
2. Create `DatabaseService` for CRUD operations
3. Create schema for Borrower, Paystub, Earning tables
4. Add Save/Load functionality to File menu
5. Add tests for persistence layer

**Example**:
```java
public class DatabaseService {
    public void saveBorrower(Borrower borrower) { ... }
    public Borrower loadBorrower(String name) { ... }
    public List<Borrower> getAllBorrowers() { ... }
}
```

---

## Build and Deployment

### Maven Build Lifecycle

**Phases**:
1. `validate` - Validate project structure
2. `compile` - Compile source code
3. `test` - Run unit tests
4. `package` - Create JAR file
5. `install` - Install to local Maven repository

### Build Commands

**Clean and compile**:
```bash
mvn clean compile
```

**Run tests**:
```bash
mvn test
```

**Create executable JAR**:
```bash
mvn clean package
```
Output: `target/EZClientCalculator-1.0-SNAPSHOT-shaded.jar`

**Run application from Maven**:
```bash
mvn javafx:run
```

### Executable JAR Structure

```
EZClientCalculator-1.0-SNAPSHOT-shaded.jar
├── META-INF/
│   └── MANIFEST.MF (Main-Class: com.mortgage.paystub.Main)
├── com/mortgage/paystub/ (application classes)
├── org/apache/pdfbox/ (PDFBox classes)
├── net/sourceforge/tess4j/ (Tess4J classes)
├── javafx/ (JavaFX classes)
├── org/slf4j/ (SLF4J classes)
├── ch/qos/logback/ (Logback classes)
└── styles.css (application styles)
```

### Running the JAR

```bash
java -jar EZClientCalculator-1.0-SNAPSHOT-shaded.jar
```

**With increased memory**:
```bash
java -Xmx1024m -jar EZClientCalculator-1.0-SNAPSHOT-shaded.jar
```

### Deployment Package

**Contents**:
- `EZClientCalculator-1.0-SNAPSHOT-shaded.jar` - Executable JAR
- `README.md` - User documentation
- `USER_GUIDE.md` - Detailed usage guide
- `LICENSE` - MIT License
- `CHANGELOG.md` - Version history

---

## Performance Considerations

### PDF Parsing

**Bottleneck**: Text extraction and OCR
**Optimization**:
- Cache extracted text
- Process PDFs in background thread
- Show progress indicator for long operations

### Calculation Performance

**Performance**: Near-instantaneous (<10ms for typical case)
**Optimization**: None needed; calculations are simple arithmetic

### Memory Usage

**Typical**: ~200-300 MB for 5-10 paystubs
**Large Files**: May spike to 500+ MB with many large PDFs

**Optimization**:
- Release paystub data after processing
- Avoid loading entire PDF into memory
- Use streaming for large files

### UI Responsiveness

**JavaFX Application Thread**: All UI updates must be on FX thread
**Background Tasks**: Use `Task` for long-running operations

**Example**:
```java
Task<List<Paystub>> parseTask = new Task<>() {
    @Override
    protected List<Paystub> call() {
        return parser.parseMultiplePaystubs(files);
    }
};
parseTask.setOnSucceeded(e -> {
    paystubs = parseTask.getValue();
    updateUI();
});
new Thread(parseTask).start();
```

---

## Security Considerations

### Data Privacy

**Current Implementation**:
- No data stored on disk
- No network transmission
- Data exists only in memory during session
- Logs do not contain sensitive information

**Future Enhancements**:
- Encrypt session files if persistence added
- Secure deletion of temporary files
- Compliance with financial data regulations

### Input Validation

**Protection Against**:
- SQL Injection: N/A (no database)
- XSS: N/A (desktop application)
- Path Traversal: File chooser limits to user-selected files
- Buffer Overflow: Java memory safety

**Validation Points**:
- All user input validated (dates, currency, names)
- File type checking before parsing
- Reasonable value range checking

---

## Logging

### Log Levels

**SLF4J Levels**:
- `ERROR`: Application errors, exceptions
- `WARN`: Potential issues, low confidence parsing
- `INFO`: Normal operations, workflow steps
- `DEBUG`: Detailed debugging information (disabled in production)
- `TRACE`: Very detailed debugging (disabled)

### Log Configuration

**File**: `src/main/resources/logback.xml`

**Output**:
- Console: INFO and above
- File: `logs/application.log` (DEBUG and above)

**Example Log Statements**:
```java
logger.info("Starting EZ Client Calculator application");
logger.debug("Parsing paystub: {}", filename);
logger.warn("Low confidence score for extraction: {}", confidence);
logger.error("Error parsing PDF", exception);
```

---

## Future Architecture Considerations

### Microservices

For enterprise deployment, consider splitting into:
- **Parser Service**: PDF/OCR processing
- **Calculation Service**: Income calculations
- **API Gateway**: RESTful API
- **Web UI**: Browser-based interface

### Plugin System

Allow third-party extensions:
- Custom parsers for proprietary paystub formats
- Custom calculation strategies for different guidelines
- Export plugins for various formats

### Cloud Deployment

For SaaS offering:
- AWS/Azure deployment
- Multi-tenant architecture
- Database persistence
- User authentication
- API integration with LOS systems

---

**Document Version**: 1.0
**Last Updated**: January 6, 2026
**Author**: James Bennett
