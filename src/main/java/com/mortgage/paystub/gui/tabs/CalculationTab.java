package com.mortgage.paystub.gui.tabs;

import com.mortgage.paystub.calculator.IncomeCalculator;
import com.mortgage.paystub.gui.StatusBar;
import com.mortgage.paystub.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Calculation tab for displaying income calculations step-by-step.
 * Shows formulas, guardrail logic, and final recommended income.
 *
 * @author James Bennett
 * @version 1.0
 */
public class CalculationTab extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(CalculationTab.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final StatusBar statusBar;
    private final IncomeCalculator calculator;

    private VBox contentSection;
    private Button viewResultsButton;
    private Runnable onViewResults;

    // Calculation data
    private Borrower borrower;
    private IncomeCalculation calculation;

    /**
     * Creates a new CalculationTab.
     *
     * @param statusBar the status bar for displaying messages
     */
    public CalculationTab(StatusBar statusBar) {
        super(20);
        this.statusBar = statusBar;
        this.calculator = new IncomeCalculator();

        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);

        // Create empty state
        contentSection = createEmptyState();
        this.getChildren().add(contentSection);
    }

    /**
     * Creates the empty state shown when no calculations have been performed.
     */
    private VBox createEmptyState() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(40));

        Label icon = new Label("\uD83D\uDCCA");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Income Calculation");
        title.getStyleClass().add("section-header");

        Label description = new Label("Calculate income from the Analysis tab to view results here");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        section.getChildren().addAll(icon, title, description);
        return section;
    }

    /**
     * Performs income calculation and displays results.
     * @return true if calculation succeeded, false otherwise
     */
    public boolean performCalculation(List<Paystub> paystubs, String borrowerName,
                                       EmploymentType employmentType,
                                       PayFrequency payFrequency,
                                       BigDecimal rateOrSalary) {
        if (paystubs == null || paystubs.isEmpty()) {
            showError("No paystubs available", "Please import and review paystubs first.");
            return false;
        }

        logger.info("Performing income calculation for borrower: {}", borrowerName);

        // Create borrower with paystubs
        borrower = new Borrower();
        borrower.setName(borrowerName != null ? borrowerName : "Unknown");
        borrower.setEmploymentType(employmentType);
        borrower.setPayFrequency(payFrequency);

        // Set hourly rate or salary based on employment type
        if (employmentType == EmploymentType.HOURLY) {
            borrower.setHourlyRate(rateOrSalary);
        } else if (employmentType == EmploymentType.SALARY) {
            borrower.setSalary(rateOrSalary);
        }

        for (Paystub paystub : paystubs) {
            borrower.addPaystub(paystub);
        }

        try {
            // Perform calculation
            calculation = calculator.calculateIncome(borrower);

            // Display results
            displayCalculation();

            statusBar.setStatus("Calculation complete for " + borrower.getName());
            logger.info("Calculation completed successfully");
            return true;

        } catch (Exception e) {
            logger.error("Error performing calculation", e);
            showError("Calculation Error", "An error occurred during calculation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays the calculation results.
     */
    private void displayCalculation() {
        // Clear existing content
        this.getChildren().clear();

        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(20);
        content.setMaxWidth(900);
        content.setAlignment(Pos.TOP_CENTER);

        // Add sections
        content.getChildren().addAll(
            createBorrowerSummary(),
            createBaseIncomeSection(),
            createRecommendedIncomeSection(),
            createVariableIncomeSection(),
            createWarningsSection(),
            createActionButtons()
        );

        scrollPane.setContent(content);
        this.getChildren().add(scrollPane);
    }

    /**
     * Creates the borrower summary section.
     */
    private VBox createBorrowerSummary() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Borrower Summary");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 18px;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        // Get most recent paystub for details
        Paystub mostRecent = borrower.getMostRecentPaystub();

        // Borrower name
        grid.add(createLabel("Borrower Name:"), 0, row);
        grid.add(createValueLabel(borrower.getName()), 1, row++);

        // Employer
        if (mostRecent != null && mostRecent.getEmployerName() != null) {
            grid.add(createLabel("Employer:"), 0, row);
            grid.add(createValueLabel(mostRecent.getEmployerName()), 1, row++);
        }

        // Pay frequency
        if (mostRecent != null && mostRecent.getPayFrequency() != null) {
            grid.add(createLabel("Pay Frequency:"), 0, row);
            String freqText = mostRecent.getPayFrequency().getDisplayName() +
                            " (" + mostRecent.getPayFrequency().getPeriodsPerYear() + " periods/year)";
            grid.add(createValueLabel(freqText), 1, row++);
        }

        // Number of paystubs
        grid.add(createLabel("Paystubs Analyzed:"), 0, row);
        grid.add(createValueLabel(String.valueOf(borrower.getPaystubs().size())), 1, row++);

        section.getChildren().addAll(title, grid);
        return section;
    }

    /**
     * Creates the base income calculation section.
     */
    private VBox createBaseIncomeSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Base Income Calculations");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 18px;");

        // Expected Monthly Income
        VBox expectedSection = createCalculationBox(
            "Expected Monthly Income",
            calculation.getExpectedMonthlyIncome(),
            createExpectedIncomeFormula()
        );

        // YTD Monthly Pacing
        VBox ytdSection = createCalculationBox(
            "YTD Monthly Pacing",
            calculation.getYtdMonthlyPacing(),
            createYtdPacingFormula()
        );

        // Variance
        VBox varianceSection = createVarianceBox();

        section.getChildren().addAll(title, expectedSection, ytdSection, varianceSection);
        return section;
    }

    /**
     * Creates the expected income formula display.
     */
    private TextFlow createExpectedIncomeFormula() {
        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent == null || mostRecent.getPayFrequency() == null) {
            return new TextFlow(new Text("Unable to calculate - missing pay frequency"));
        }

        // Calculate base wage total from current earnings
        BigDecimal baseWageTotal = mostRecent.getEarnings().stream()
            .filter(e -> e.getCategory() == PayCategory.BASE_WAGE)
            .map(Earning::getCurrentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int periodsPerYear = mostRecent.getPayFrequency().getPeriodsPerYear();

        Text formula = new Text("Formula: ");
        formula.setStyle("-fx-font-weight: bold;");

        Text calc = new Text(String.format("$%,.2f × %d ÷ 12 = $%,.2f",
            baseWageTotal, periodsPerYear, calculation.getExpectedMonthlyIncome()));

        TextFlow flow = new TextFlow(formula, calc);
        flow.setStyle("-fx-font-size: 13px;");
        return flow;
    }

    /**
     * Creates the YTD pacing formula display.
     */
    private TextFlow createYtdPacingFormula() {
        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent == null) {
            return new TextFlow(new Text("Unable to calculate - missing paystub data"));
        }

        // Calculate base wage YTD total from earnings
        BigDecimal ytdTotal = mostRecent.getEarnings().stream()
            .filter(e -> e.getCategory() == PayCategory.BASE_WAGE)
            .map(Earning::getYtdAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int paychecksYtd = calculation.getNumberOfPaychecksYtd();

        Text formula = new Text("Formula: ");
        formula.setStyle("-fx-font-weight: bold;");

        Text calc = new Text(String.format("$%,.2f ÷ %d paychecks = $%,.2f per paycheck\n",
            ytdTotal, paychecksYtd, ytdTotal.divide(BigDecimal.valueOf(paychecksYtd), 2, BigDecimal.ROUND_HALF_UP)));

        Text monthly = new Text(String.format("$%,.2f × %d ÷ 12 = $%,.2f monthly",
            ytdTotal.divide(BigDecimal.valueOf(paychecksYtd), 2, BigDecimal.ROUND_HALF_UP),
            mostRecent.getPayFrequency() != null ? mostRecent.getPayFrequency().getPeriodsPerYear() : 0,
            calculation.getYtdMonthlyPacing()));

        TextFlow flow = new TextFlow(formula, calc, monthly);
        flow.setStyle("-fx-font-size: 13px;");
        return flow;
    }

    /**
     * Creates the variance display box.
     */
    private VBox createVarianceBox() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("variance-box");

        Label varianceLabel = new Label("Variance Analysis");
        varianceLabel.getStyleClass().add("variance-box-label");

        BigDecimal variance = calculation.getVariancePercentage();
        String varianceText = String.format("%.2f%%", variance);

        Label varianceValue = new Label(varianceText);
        varianceValue.getStyleClass().add("variance-box-value");

        // Color code based on guardrail
        String varianceClass;
        String guardrailText;
        if (calculation.isWithinAcceptableRange()) {
            varianceClass = "variance-acceptable";
            guardrailText = "✓ Within acceptable range (0-5%)";
        } else if (!calculation.hasSignificantVariance()) {
            // Medium variance: not acceptable but not significant (5-10%)
            varianceClass = "variance-medium";
            guardrailText = "⚠ Medium variance (5-10%) - Documentation needed";
        } else {
            varianceClass = "variance-significant";
            guardrailText = "⚠ Significant variance (>10%) - Use YTD pacing";
        }

        varianceValue.getStyleClass().add(varianceClass);

        Label guardrailLabel = new Label(guardrailText);
        guardrailLabel.getStyleClass().addAll("variance-guardrail", varianceClass);

        box.getChildren().addAll(varianceLabel, varianceValue, guardrailLabel);
        return box;
    }

    /**
     * Creates the recommended income section.
     */
    private VBox createRecommendedIncomeSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.getStyleClass().addAll("card", "recommended-income-section");

        Label title = new Label("RECOMMENDED USABLE BASE INCOME");
        title.getStyleClass().add("recommended-income-title");

        Label amount = new Label(String.format("$%,.2f", calculation.getRecommendedUsableBaseIncome()));
        amount.getStyleClass().add("recommended-income-amount");

        // Explanation
        String explanation = getIncomeExplanation();
        Label explanationLabel = new Label(explanation);
        explanationLabel.setWrapText(true);
        explanationLabel.setMaxWidth(800);
        explanationLabel.getStyleClass().add("recommended-income-explanation");

        // Notes area
        Label notesTitle = new Label("Additional Notes:");
        notesTitle.getStyleClass().add("notes-title");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add any additional notes or explanations here...");
        notesArea.setPrefRowCount(3);
        notesArea.setMaxWidth(800);
        notesArea.setWrapText(true);

        section.getChildren().addAll(title, amount, explanationLabel, notesTitle, notesArea);
        return section;
    }

    /**
     * Gets the explanation for the recommended income.
     */
    private String getIncomeExplanation() {
        if (calculation.isWithinAcceptableRange()) {
            return "Income is on track. Expected monthly income is used (variance within 0-5% acceptable range).";
        } else if (!calculation.hasSignificantVariance()) {
            // Medium variance: not acceptable but not significant (5-10%)
            BigDecimal variance = calculation.getVariancePercentage();
            return String.format("Variance of %.2f%% is in the 5-10%% range. Using YTD monthly pacing. " +
                               "Documentation explaining the variance should be included in the file.", variance);
        } else {
            BigDecimal variance = calculation.getVariancePercentage();
            return String.format("Significant variance of %.2f%% (>10%%). Using YTD monthly pacing " +
                               "as the more conservative income figure per lending guidelines.", variance);
        }
    }

    /**
     * Creates the variable income section.
     */
    private VBox createVariableIncomeSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Variable Income Analysis");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 18px;");

        BigDecimal variableIncome = calculation.getTotalVariableIncome();

        if (variableIncome.compareTo(BigDecimal.ZERO) == 0) {
            Label noVariable = new Label("No variable income detected (overtime, commission, bonuses, etc.)");
            noVariable.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-style: italic;");
            section.getChildren().addAll(title, noVariable);
            return section;
        }

        // Get most recent paystub
        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent != null) {
            // Display each variable earning type
            for (Earning earning : mostRecent.getEarnings()) {
                if (earning.getCategory() == PayCategory.VARIABLE) {
                    VBox earningBox = createVariableEarningBox(earning);
                    section.getChildren().add(earningBox);
                }
            }
        }

        // Total variable income
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 0, 0, 0));
        totalBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Label totalLabel = new Label("Total Monthly Variable Income:");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label totalValue = new Label(String.format("$%,.2f", variableIncome));
        totalValue.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1E88E5;");

        totalBox.getChildren().addAll(totalLabel, totalValue);
        section.getChildren().add(totalBox);

        section.getChildren().add(0, title);
        return section;
    }

    /**
     * Creates a variable earning display box.
     */
    private VBox createVariableEarningBox(Earning earning) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("variable-earning-box");

        Label nameLabel = new Label(earning.getPayTypeName());
        nameLabel.getStyleClass().add("variable-earning-name");

        // Calculate monthly amount (YTD / number of months YTD)
        Paystub mostRecent = borrower.getMostRecentPaystub();
        BigDecimal monthly = BigDecimal.ZERO;

        if (mostRecent != null && mostRecent.getPayFrequency() != null) {
            int paychecksYtd = calculator.determinePaychecksYtd(mostRecent, mostRecent.getPayFrequency());
            if (paychecksYtd > 0) {
                BigDecimal perPaycheck = earning.getYtdAmount().divide(
                    BigDecimal.valueOf(paychecksYtd), 2, BigDecimal.ROUND_HALF_UP);
                monthly = perPaycheck.multiply(BigDecimal.valueOf(mostRecent.getPayFrequency().getPeriodsPerYear()))
                                    .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            }
        }

        Label ytdLabel = new Label(String.format("YTD Total: $%,.2f", earning.getYtdAmount()));
        ytdLabel.getStyleClass().add("variable-earning-ytd");

        Label monthlyLabel = new Label(String.format("Monthly Average: $%,.2f", monthly));
        monthlyLabel.getStyleClass().add("variable-earning-monthly");

        box.getChildren().addAll(nameLabel, ytdLabel, monthlyLabel);
        return box;
    }

    /**
     * Creates the warnings section.
     */
    private VBox createWarningsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Warnings & Validation");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 18px;");

        List<String> warnings = calculation.getWarnings();

        if (warnings.isEmpty()) {
            Label noWarnings = new Label("✓ No warnings - all validations passed");
            noWarnings.setStyle("-fx-font-size: 13px; -fx-text-fill: #43A047;");
            section.getChildren().addAll(title, noWarnings);
            return section;
        }

        VBox warningsList = new VBox(8);
        for (String warning : warnings) {
            HBox warningBox = new HBox(10);
            warningBox.setPadding(new Insets(8));
            warningBox.getStyleClass().add("warning-item");

            Label icon = new Label("⚠");
            icon.getStyleClass().add("warning-icon");

            Label warningText = new Label(warning);
            warningText.setWrapText(true);
            warningText.setMaxWidth(750);
            warningText.getStyleClass().add("warning-text");

            warningBox.getChildren().addAll(icon, warningText);
            warningsList.getChildren().add(warningBox);
        }

        section.getChildren().addAll(title, warningsList);
        return section;
    }

    /**
     * Creates action buttons.
     */
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button recalculateButton = new Button("Recalculate");
        recalculateButton.getStyleClass().add("secondary");
        recalculateButton.setOnAction(e -> {
            if (borrower != null) {
                BigDecimal rateOrSalary = borrower.getEmploymentType() == EmploymentType.HOURLY
                    ? borrower.getHourlyRate()
                    : borrower.getSalary();
                performCalculation(borrower.getPaystubs(), borrower.getName(),
                    borrower.getEmploymentType(), borrower.getPayFrequency(), rateOrSalary);
            }
        });

        viewResultsButton = new Button("View Results");
        viewResultsButton.getStyleClass().add("success");
        viewResultsButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        viewResultsButton.setOnAction(e -> {
            if (onViewResults != null) {
                onViewResults.run();
            }
        });

        buttonBox.getChildren().addAll(recalculateButton, viewResultsButton);
        return buttonBox;
    }

    /**
     * Creates a calculation display box.
     */
    private VBox createCalculationBox(String name, BigDecimal value, TextFlow formula) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("calculation-box");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("calculation-box-label");

        Label valueLabel = new Label(String.format("$%,.2f", value));
        valueLabel.getStyleClass().add("calculation-box-value");

        box.getChildren().addAll(nameLabel, valueLabel, formula);
        return box;
    }

    /**
     * Creates a label.
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    /**
     * Creates a value label.
     */
    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px;");
        return label;
    }

    /**
     * Shows an error dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sets the callback for viewing results.
     */
    public void setOnViewResults(Runnable callback) {
        this.onViewResults = callback;
    }

    /**
     * Gets the current calculation.
     */
    public IncomeCalculation getCalculation() {
        return calculation;
    }

    /**
     * Gets the current borrower.
     */
    public Borrower getBorrower() {
        return borrower;
    }
}
