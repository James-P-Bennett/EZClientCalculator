package com.mortgage.paystub.gui.tabs;

import com.mortgage.paystub.gui.StatusBar;
import com.mortgage.paystub.gui.components.CopyableField;
import com.mortgage.paystub.model.*;
import com.mortgage.paystub.utils.ClipboardUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Results tab for displaying final calculated income values with clipboard copy functionality.
 * All fields are clickable and copy their values to the clipboard.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ResultsTab extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(ResultsTab.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final StatusBar statusBar;
    private VBox contentSection;

    // Calculation data
    private Borrower borrower;
    private IncomeCalculation calculation;

    /**
     * Creates a new ResultsTab.
     *
     * @param statusBar the status bar for displaying messages
     */
    public ResultsTab(StatusBar statusBar) {
        super(20);
        this.statusBar = statusBar;

        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);

        // Create empty state
        contentSection = createEmptyState();
        this.getChildren().add(contentSection);

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
    }

    /**
     * Creates the empty state shown when no results are available.
     */
    private VBox createEmptyState() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(40));

        Label icon = new Label("\u2705");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Results & Export");
        title.getStyleClass().add("section-header");

        Label description = new Label("Complete the calculation to view copyable results here");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        section.getChildren().addAll(icon, title, description);
        return section;
    }

    /**
     * Displays results from the calculation.
     *
     * @param borrower the borrower
     * @param calculation the income calculation
     */
    public void displayResults(Borrower borrower, IncomeCalculation calculation) {
        if (borrower == null || calculation == null) {
            logger.warn("Cannot display results: borrower or calculation is null");
            return;
        }

        this.borrower = borrower;
        this.calculation = calculation;

        logger.info("Displaying results for borrower: {}", borrower.getName());

        // Clear existing content
        this.getChildren().clear();

        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(20);
        content.setMaxWidth(900);
        content.setAlignment(Pos.TOP_CENTER);

        // Add all sections
        content.getChildren().addAll(
            createTitleSection(),
            createBorrowerInfoSection(),
            createBaseIncomeSection(),
            createPayTypeBreakdownSection(),
            createVariableIncomeSection(),
            createSummarySection(),
            createWarningsSection(),
            createActionButtons()
        );

        scrollPane.setContent(content);
        this.getChildren().add(scrollPane);

        statusBar.setStatus("Results displayed - Click any field to copy");
    }

    /**
     * Creates the title section.
     */
    private VBox createTitleSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("Income Calculation Results");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Click any field to copy to clipboard");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-style: italic;");

        section.getChildren().addAll(title, subtitle);
        return section;
    }

    /**
     * Creates the borrower information section.
     */
    private VBox createBorrowerInfoSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Section 1: Borrower Information");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        VBox fields = new VBox(8);

        // Get most recent paystub for details
        Paystub mostRecent = borrower.getMostRecentPaystub();

        // Borrower name
        fields.getChildren().add(new CopyableField(
            "Borrower Name",
            borrower.getName(),
            statusBar
        ));

        // Employer
        if (mostRecent != null && mostRecent.getEmployerName() != null) {
            fields.getChildren().add(new CopyableField(
                "Employer",
                mostRecent.getEmployerName(),
                statusBar
            ));
        }

        // Pay frequency
        if (mostRecent != null && mostRecent.getPayFrequency() != null) {
            String freqText = mostRecent.getPayFrequency().getDisplayName() +
                            " (" + mostRecent.getPayFrequency().getPeriodsPerYear() + " pay periods/year)";
            fields.getChildren().add(new CopyableField(
                "Pay Frequency",
                freqText,
                statusBar
            ));
        }

        // Number of paystubs analyzed
        fields.getChildren().add(new CopyableField(
            "Paystubs Analyzed",
            String.valueOf(borrower.getPaystubs().size()),
            statusBar
        ));

        // Most recent pay date
        if (mostRecent != null && mostRecent.getPayDate() != null) {
            fields.getChildren().add(new CopyableField(
                "Most Recent Pay Date",
                mostRecent.getPayDate().format(DATE_FORMATTER),
                statusBar
            ));
        }

        section.getChildren().addAll(title, fields);
        return section;
    }

    /**
     * Creates the base income section.
     */
    private VBox createBaseIncomeSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Section 2: Base Income Analysis");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        VBox fields = new VBox(8);

        // Expected Monthly Income
        fields.getChildren().add(new CopyableField(
            "Expected Monthly Income",
            String.format("$%,.2f", calculation.getExpectedMonthlyIncome()),
            statusBar
        ));

        // YTD Monthly Pacing
        fields.getChildren().add(new CopyableField(
            "YTD Monthly Pacing",
            String.format("$%,.2f", calculation.getYtdMonthlyPacing()),
            statusBar
        ));

        // Number of paychecks YTD
        fields.getChildren().add(new CopyableField(
            "Paychecks Year-to-Date",
            String.valueOf(calculation.getNumberOfPaychecksYtd()),
            statusBar
        ));

        // Variance percentage
        BigDecimal variance = calculation.getVariancePercentage();
        fields.getChildren().add(new CopyableField(
            "Variance Percentage",
            String.format("%.2f%%", variance),
            statusBar
        ));

        // Recommended Usable Base Income (prominent)
        CopyableField recommendedField = new CopyableField(
            "RECOMMENDED USABLE BASE INCOME",
            String.format("$%,.2f", calculation.getRecommendedUsableBaseIncome()),
            statusBar,
            true
        );
        recommendedField.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 12; " +
                                 "-fx-border-color: #43A047; -fx-border-width: 2; -fx-border-radius: 4;");
        fields.getChildren().add(recommendedField);

        // Explanation
        String explanation = getIncomeExplanation();
        Label explanationLabel = new Label(explanation);
        explanationLabel.setWrapText(true);
        explanationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #616161; -fx-font-style: italic;");
        fields.getChildren().add(explanationLabel);

        section.getChildren().addAll(title, fields);
        return section;
    }

    /**
     * Creates the pay type breakdown section.
     */
    private VBox createPayTypeBreakdownSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Section 3: Pay Type Breakdown");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent == null || mostRecent.getEarnings().isEmpty()) {
            Label noData = new Label("No pay type data available");
            noData.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-style: italic;");
            section.getChildren().addAll(title, noData);
            return section;
        }

        VBox earningsBox = new VBox(5);

        for (Earning earning : mostRecent.getEarnings()) {
            HBox row = createPayTypeRow(earning);
            earningsBox.getChildren().add(row);
        }

        section.getChildren().addAll(title, earningsBox);
        return section;
    }

    /**
     * Creates a pay type row with copyable cells.
     */
    private HBox createPayTypeRow(Earning earning) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 4;");

        // Pay type name
        Label nameLabel = new Label(earning.getPayTypeName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-min-width: 150px;");

        // Category
        Label categoryLabel = new Label(earning.getCategory().getDisplayName());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575; -fx-min-width: 100px;");

        // Current amount (copyable)
        CopyableField currentField = new CopyableField(
            "Current",
            String.format("$%,.2f", earning.getCurrentAmount()),
            statusBar
        );
        currentField.setStyle("-fx-background-color: transparent; -fx-padding: 4;");

        // YTD amount (copyable)
        CopyableField ytdField = new CopyableField(
            "YTD",
            String.format("$%,.2f", earning.getYtdAmount()),
            statusBar
        );
        ytdField.setStyle("-fx-background-color: transparent; -fx-padding: 4;");

        row.getChildren().addAll(nameLabel, categoryLabel, currentField, ytdField);
        return row;
    }

    /**
     * Creates the variable income section.
     */
    private VBox createVariableIncomeSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Section 4: Variable Income Analysis");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        BigDecimal variableIncome = calculation.getTotalVariableIncome();

        if (variableIncome.compareTo(BigDecimal.ZERO) == 0) {
            Label noVariable = new Label("No variable income detected");
            noVariable.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-style: italic;");
            section.getChildren().addAll(title, noVariable);
            return section;
        }

        VBox fields = new VBox(8);

        // Get variable earnings from most recent paystub
        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent != null) {
            for (Earning earning : mostRecent.getEarnings()) {
                if (earning.getCategory() == PayCategory.VARIABLE) {
                    VBox earningBox = createVariableEarningDisplay(earning);
                    fields.getChildren().add(earningBox);
                }
            }
        }

        // Total variable income (prominent)
        CopyableField totalField = new CopyableField(
            "Total Monthly Variable Income",
            String.format("$%,.2f", variableIncome),
            statusBar,
            true
        );
        totalField.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 12; " +
                           "-fx-border-color: #1E88E5; -fx-border-width: 2; -fx-border-radius: 4;");
        fields.getChildren().add(totalField);

        section.getChildren().addAll(title, fields);
        return section;
    }

    /**
     * Creates a variable earning display box.
     */
    private VBox createVariableEarningDisplay(Earning earning) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 4;");

        Label nameLabel = new Label(earning.getPayTypeName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        CopyableField ytdField = new CopyableField(
            "YTD Total",
            String.format("$%,.2f", earning.getYtdAmount()),
            statusBar
        );
        ytdField.setStyle("-fx-background-color: transparent; -fx-padding: 4;");

        // Calculate monthly average
        Paystub mostRecent = borrower.getMostRecentPaystub();
        BigDecimal monthly = BigDecimal.ZERO;
        if (mostRecent != null && mostRecent.getPayFrequency() != null) {
            int paychecksYtd = calculation.getNumberOfPaychecksYtd();
            if (paychecksYtd > 0) {
                BigDecimal perPaycheck = earning.getYtdAmount().divide(
                    BigDecimal.valueOf(paychecksYtd), 2, BigDecimal.ROUND_HALF_UP);
                monthly = perPaycheck.multiply(BigDecimal.valueOf(mostRecent.getPayFrequency().getPeriodsPerYear()))
                                    .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            }
        }

        CopyableField monthlyField = new CopyableField(
            "Monthly Average",
            String.format("$%,.2f", monthly),
            statusBar
        );
        monthlyField.setStyle("-fx-background-color: transparent; -fx-padding: 4;");

        box.getChildren().addAll(nameLabel, ytdField, monthlyField);
        return box;
    }

    /**
     * Creates the summary section with total qualified income.
     */
    private VBox createSummarySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("card");
        section.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #43A047; " +
                        "-fx-border-width: 3; -fx-border-radius: 6;");

        Label title = new Label("Section 5: INCOME SUMMARY");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        VBox fields = new VBox(12);

        // Base income subtotal
        CopyableField baseIncomeField = new CopyableField(
            "Base Income (Monthly)",
            String.format("$%,.2f", calculation.getRecommendedUsableBaseIncome()),
            statusBar,
            true
        );
        baseIncomeField.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4;");

        // Variable income subtotal
        CopyableField variableIncomeField = new CopyableField(
            "Variable Income (Monthly)",
            String.format("$%,.2f", calculation.getTotalVariableIncome()),
            statusBar,
            true
        );
        variableIncomeField.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4;");

        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #43A047;");

        // Total qualified monthly income (very prominent)
        BigDecimal totalIncome = calculation.getRecommendedUsableBaseIncome()
                                           .add(calculation.getTotalVariableIncome());

        VBox totalBox = new VBox(5);
        totalBox.setAlignment(Pos.CENTER);
        totalBox.setPadding(new Insets(15));
        totalBox.setStyle("-fx-background-color: white; -fx-background-radius: 4;");

        Label totalLabel = new Label("TOTAL QUALIFIED MONTHLY INCOME");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");

        Label totalValue = new Label(String.format("$%,.2f", totalIncome));
        totalValue.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        totalBox.getChildren().addAll(totalLabel, totalValue);

        // Make total box copyable
        totalBox.setCursor(javafx.scene.Cursor.HAND);
        Tooltip tooltip = new Tooltip("Click to copy");
        Tooltip.install(totalBox, tooltip);
        totalBox.setOnMouseClicked(e -> {
            ClipboardUtil.copyToClipboard(String.format("Total Qualified Monthly Income: $%,.2f", totalIncome));
            statusBar.setStatus(String.format("Copied: Total Qualified Monthly Income - $%,.2f", totalIncome));
        });

        fields.getChildren().addAll(baseIncomeField, variableIncomeField, separator, totalBox);

        section.getChildren().addAll(title, fields);
        return section;
    }

    /**
     * Creates the warnings section.
     */
    private VBox createWarningsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        Label title = new Label("Section 6: Warnings & Documentation");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        List<String> warnings = calculation.getWarnings();

        if (warnings.isEmpty()) {
            Label noWarnings = new Label("\u2713 No warnings - all validations passed");
            noWarnings.setStyle("-fx-font-size: 13px; -fx-text-fill: #43A047;");
            section.getChildren().addAll(title, noWarnings);
            return section;
        }

        VBox warningsList = new VBox(8);
        for (String warning : warnings) {
            HBox warningBox = createCopyableWarning(warning);
            warningsList.getChildren().add(warningBox);
        }

        section.getChildren().addAll(title, warningsList);
        return section;
    }

    /**
     * Creates a copyable warning box.
     */
    private HBox createCopyableWarning(String warning) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 4; -fx-border-color: transparent; " +
                    "-fx-border-width: 1; -fx-border-radius: 4;");
        box.setCursor(javafx.scene.Cursor.HAND);
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("\u26A0");
        icon.setStyle("-fx-font-size: 16px; -fx-text-fill: #FB8C00;");

        Label warningText = new Label(warning);
        warningText.setWrapText(true);
        warningText.setStyle("-fx-font-size: 13px;");

        box.getChildren().addAll(icon, warningText);

        // Tooltip
        Tooltip tooltip = new Tooltip("Click to copy");
        Tooltip.install(box, tooltip);

        // Hover effect
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FFE0B2; -fx-background-radius: 4; -fx-border-color: #FB8C00; " +
                        "-fx-border-width: 1; -fx-border-radius: 4;");
        });

        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 4; -fx-border-color: transparent; " +
                        "-fx-border-width: 1; -fx-border-radius: 4;");
        });

        // Click to copy
        box.setOnMouseClicked(e -> {
            ClipboardUtil.copyToClipboard(warning);
            statusBar.setStatus("Copied: " + warning);
        });

        return box;
    }

    /**
     * Creates action buttons.
     */
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button copyAllButton = new Button("Copy All Results");
        copyAllButton.getStyleClass().add("primary");
        copyAllButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        copyAllButton.setOnAction(e -> copyAllResults());

        buttonBox.getChildren().add(copyAllButton);
        return buttonBox;
    }

    /**
     * Copies all results to clipboard in formatted text.
     */
    private void copyAllResults() {
        if (borrower == null || calculation == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INCOME CALCULATION RESULTS\n");
        sb.append("=".repeat(50)).append("\n\n");

        // Borrower Information
        sb.append("BORROWER INFORMATION\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append("Borrower Name: ").append(borrower.getName()).append("\n");

        Paystub mostRecent = borrower.getMostRecentPaystub();
        if (mostRecent != null) {
            if (mostRecent.getEmployerName() != null) {
                sb.append("Employer: ").append(mostRecent.getEmployerName()).append("\n");
            }
            if (mostRecent.getPayFrequency() != null) {
                sb.append("Pay Frequency: ").append(mostRecent.getPayFrequency().getDisplayName())
                  .append(" (").append(mostRecent.getPayFrequency().getPeriodsPerYear())
                  .append(" periods/year)\n");
            }
        }
        sb.append("Paystubs Analyzed: ").append(borrower.getPaystubs().size()).append("\n\n");

        // Base Income
        sb.append("BASE INCOME ANALYSIS\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Expected Monthly Income: $%,.2f\n", calculation.getExpectedMonthlyIncome()));
        sb.append(String.format("YTD Monthly Pacing: $%,.2f\n", calculation.getYtdMonthlyPacing()));
        sb.append(String.format("Paychecks YTD: %d\n", calculation.getNumberOfPaychecksYtd()));
        sb.append(String.format("Variance: %.2f%%\n", calculation.getVariancePercentage()));
        sb.append(String.format("RECOMMENDED USABLE BASE INCOME: $%,.2f\n\n",
                               calculation.getRecommendedUsableBaseIncome()));

        // Variable Income
        BigDecimal variableIncome = calculation.getTotalVariableIncome();
        if (variableIncome.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("VARIABLE INCOME ANALYSIS\n");
            sb.append("-".repeat(50)).append("\n");
            if (mostRecent != null) {
                for (Earning earning : mostRecent.getEarnings()) {
                    if (earning.getCategory() == PayCategory.VARIABLE) {
                        sb.append(earning.getPayTypeName()).append(":\n");
                        sb.append(String.format("  YTD Total: $%,.2f\n", earning.getYtdAmount()));
                    }
                }
            }
            sb.append(String.format("Total Monthly Variable Income: $%,.2f\n\n", variableIncome));
        }

        // Summary
        BigDecimal totalIncome = calculation.getRecommendedUsableBaseIncome()
                                           .add(calculation.getTotalVariableIncome());
        sb.append("INCOME SUMMARY\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("Base Income (Monthly): $%,.2f\n", calculation.getRecommendedUsableBaseIncome()));
        sb.append(String.format("Variable Income (Monthly): $%,.2f\n", calculation.getTotalVariableIncome()));
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("TOTAL QUALIFIED MONTHLY INCOME: $%,.2f\n", totalIncome));
        sb.append("=".repeat(50)).append("\n\n");

        // Warnings
        List<String> warnings = calculation.getWarnings();
        if (!warnings.isEmpty()) {
            sb.append("WARNINGS & DOCUMENTATION NEEDED\n");
            sb.append("-".repeat(50)).append("\n");
            for (String warning : warnings) {
                sb.append("- ").append(warning).append("\n");
            }
        }

        boolean success = ClipboardUtil.copyToClipboard(sb.toString());
        if (success) {
            statusBar.setStatus("Copied: Complete results to clipboard");
            logger.info("Copied all results to clipboard");
        }
    }

    /**
     * Gets the explanation for the recommended income.
     */
    private String getIncomeExplanation() {
        if (calculation.isWithinAcceptableRange()) {
            return "Income is on track. Expected monthly income is used (variance within 0-5% acceptable range).";
        } else if (!calculation.hasSignificantVariance()) {
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
     * Sets up keyboard shortcuts for clipboard operations.
     */
    private void setupKeyboardShortcuts() {
        // Ctrl+Shift+C to copy all results
        KeyCombination copyAllCombo = new KeyCodeCombination(KeyCode.C,
                                                            KeyCombination.CONTROL_DOWN,
                                                            KeyCombination.SHIFT_DOWN);

        this.setOnKeyPressed(event -> {
            if (copyAllCombo.match(event)) {
                copyAllResults();
                event.consume();
            }
        });

        // Make this node focusable so it can receive key events
        this.setFocusTraversable(true);
    }
}
