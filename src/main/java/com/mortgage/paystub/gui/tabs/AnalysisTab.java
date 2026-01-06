package com.mortgage.paystub.gui.tabs;

import com.mortgage.paystub.gui.StatusBar;
import com.mortgage.paystub.model.*;
import com.mortgage.paystub.parser.ParsingResult;
import com.mortgage.paystub.parser.PaystubParser;
import com.mortgage.paystub.parser.PDFPaystubParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Analysis tab for reviewing and editing extracted paystub data.
 * Allows manual correction of OCR errors and categorization of pay types.
 *
 * @author James Bennett
 * @version 1.0
 */
public class AnalysisTab extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisTab.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final StatusBar statusBar;
    private final PaystubParser parser;
    private final List<ParsingResult> parsingResults;
    private int currentPaystubIndex = 0;
    private CalculationTab calculationTab;
    private Runnable onCalculateComplete;

    // UI Components
    private ProgressBar progressBar;
    private Label progressLabel;
    private VBox processingSection;
    private VBox dataSection;
    private Button prevButton;
    private Button nextButton;
    private Label paystubCountLabel;
    private Button calculateButton;

    // Form fields
    private TextField employeeNameField;
    private TextField employerNameField;
    private DatePicker payDatePicker;
    private DatePicker periodStartPicker;
    private DatePicker periodEndPicker;
    private ComboBox<PayFrequency> payFrequencyCombo;
    private RadioButton hourlyRadio;
    private RadioButton salaryRadio;
    private TextField rateField;

    // Tables
    private TableView<EarningRow> earningsTable;
    private ObservableList<EarningRow> earningsData;
    private TableView<DeductionRow> deductionsTable;
    private ObservableList<DeductionRow> deductionsData;

    /**
     * Creates a new AnalysisTab.
     *
     * @param statusBar the status bar for displaying messages
     */
    public AnalysisTab(StatusBar statusBar) {
        super(20);
        this.statusBar = statusBar;
        this.parser = new PDFPaystubParser();
        this.parsingResults = new ArrayList<>();

        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);

        // Create processing section (initially visible)
        processingSection = createProcessingSection();

        // Create data section (initially hidden)
        dataSection = createDataSection();
        dataSection.setVisible(false);
        dataSection.setManaged(false);

        this.getChildren().addAll(processingSection, dataSection);
    }

    /**
     * Creates the processing section with progress indicator.
     */
    private VBox createProcessingSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(40));

        Label icon = new Label("\uD83D\uDD0D");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Data Analysis");
        title.getStyleClass().add("section-header");

        progressLabel = new Label("No files processed yet");
        progressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);

        Label hint = new Label("Import files from the Import tab and click 'Process Files' to begin");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999; -fx-font-style: italic;");

        section.getChildren().addAll(icon, title, progressLabel, progressBar, hint);
        return section;
    }

    /**
     * Creates the data review section.
     */
    private VBox createDataSection() {
        VBox section = new VBox(20);
        section.setMaxWidth(900);

        // Navigation header
        HBox navHeader = createNavigationHeader();

        // Paystub data form
        GridPane dataForm = createDataForm();

        // Earnings section
        VBox earningsSection = createEarningsSection();

        // Deductions section
        VBox deductionsSection = createDeductionsSection();

        // Calculate button
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        calculateButton = new Button("Calculate Income");
        calculateButton.getStyleClass().add("success");
        calculateButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        calculateButton.setOnAction(e -> handleCalculate());

        buttonBox.getChildren().add(calculateButton);

        section.getChildren().addAll(navHeader, dataForm, earningsSection, deductionsSection, buttonBox);
        return section;
    }

    /**
     * Creates the navigation header for switching between paystubs.
     */
    private HBox createNavigationHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("card");

        prevButton = new Button("< Previous");
        prevButton.setOnAction(e -> navigateToPrevious());

        paystubCountLabel = new Label("No paystubs");
        paystubCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        nextButton = new Button("Next >");
        nextButton.setOnAction(e -> navigateToNext());

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        header.getChildren().addAll(spacer1, prevButton, paystubCountLabel, nextButton, spacer2);
        return header;
    }

    /**
     * Creates the main data entry form.
     */
    private GridPane createDataForm() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        grid.getStyleClass().add("card");

        int row = 0;

        // Section title
        Label formTitle = new Label("Paystub Information");
        formTitle.getStyleClass().add("section-header");
        formTitle.setStyle("-fx-font-size: 16px;");
        GridPane.setColumnSpan(formTitle, 2);
        grid.add(formTitle, 0, row++);

        // Employee name
        grid.add(createFieldLabel("Employee Name:"), 0, row);
        employeeNameField = new TextField();
        employeeNameField.setPrefWidth(300);
        grid.add(employeeNameField, 1, row++);

        // Employer name
        grid.add(createFieldLabel("Employer Name:"), 0, row);
        employerNameField = new TextField();
        employerNameField.setPrefWidth(300);
        grid.add(employerNameField, 1, row++);

        // Pay date
        grid.add(createFieldLabel("Pay Date:"), 0, row);
        payDatePicker = new DatePicker();
        payDatePicker.setPrefWidth(300);
        grid.add(payDatePicker, 1, row++);

        // Pay period start
        grid.add(createFieldLabel("Period Start:"), 0, row);
        periodStartPicker = new DatePicker();
        periodStartPicker.setPrefWidth(300);
        grid.add(periodStartPicker, 1, row++);

        // Pay period end
        grid.add(createFieldLabel("Period End:"), 0, row);
        periodEndPicker = new DatePicker();
        periodEndPicker.setPrefWidth(300);
        grid.add(periodEndPicker, 1, row++);

        // Pay frequency
        grid.add(createFieldLabel("Pay Frequency:"), 0, row);
        payFrequencyCombo = new ComboBox<>(FXCollections.observableArrayList(PayFrequency.values()));
        payFrequencyCombo.setPrefWidth(300);
        grid.add(payFrequencyCombo, 1, row++);

        // Employment type
        grid.add(createFieldLabel("Employment Type:"), 0, row);
        HBox employmentTypeBox = new HBox(15);
        ToggleGroup employmentTypeGroup = new ToggleGroup();
        hourlyRadio = new RadioButton("Hourly");
        hourlyRadio.setToggleGroup(employmentTypeGroup);
        salaryRadio = new RadioButton("Salary");
        salaryRadio.setToggleGroup(employmentTypeGroup);
        employmentTypeBox.getChildren().addAll(hourlyRadio, salaryRadio);
        grid.add(employmentTypeBox, 1, row++);

        // Rate/Salary
        grid.add(createFieldLabel("Rate/Salary:"), 0, row);
        rateField = new TextField();
        rateField.setPromptText("Enter hourly rate or annual salary");
        rateField.setPrefWidth(300);
        grid.add(rateField, 1, row++);

        return grid;
    }

    /**
     * Creates the earnings section with table.
     */
    private VBox createEarningsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        // Header with add button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Earnings");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addEarningButton = new Button("+ Add Earning");
        addEarningButton.setOnAction(e -> addEarningRow());

        header.getChildren().addAll(title, spacer, addEarningButton);

        // Earnings table
        earningsData = FXCollections.observableArrayList();
        earningsTable = createEarningsTable();

        section.getChildren().addAll(header, earningsTable);
        return section;
    }

    /**
     * Creates the earnings table.
     */
    @SuppressWarnings("unchecked")
    private TableView<EarningRow> createEarningsTable() {
        TableView<EarningRow> table = new TableView<>(earningsData);
        table.setEditable(true);
        table.setPrefHeight(250);

        // Pay Type column
        TableColumn<EarningRow, String> payTypeCol = new TableColumn<>("Pay Type");
        payTypeCol.setCellValueFactory(data -> data.getValue().payTypeProperty());
        payTypeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        payTypeCol.setOnEditCommit(event -> {
            event.getRowValue().setPayType(event.getNewValue());
        });
        payTypeCol.setPrefWidth(150);

        // Category column
        TableColumn<EarningRow, PayCategory> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(PayCategory.values()));
        categoryCol.setOnEditCommit(event -> {
            event.getRowValue().setCategory(event.getNewValue());
        });
        categoryCol.setPrefWidth(120);

        // Current Amount column
        TableColumn<EarningRow, String> currentCol = new TableColumn<>("Current Amount");
        currentCol.setCellValueFactory(data -> data.getValue().currentAmountProperty());
        currentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        currentCol.setOnEditCommit(event -> {
            event.getRowValue().setCurrentAmount(event.getNewValue());
        });
        currentCol.setPrefWidth(130);

        // YTD Amount column
        TableColumn<EarningRow, String> ytdCol = new TableColumn<>("YTD Amount");
        ytdCol.setCellValueFactory(data -> data.getValue().ytdAmountProperty());
        ytdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        ytdCol.setOnEditCommit(event -> {
            event.getRowValue().setYtdAmount(event.getNewValue());
        });
        ytdCol.setPrefWidth(130);

        // Actions column
        TableColumn<EarningRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Remove");

            {
                deleteBtn.getStyleClass().add("danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                deleteBtn.setOnAction(e -> {
                    EarningRow row = getTableView().getItems().get(getIndex());
                    earningsData.remove(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionsCol.setPrefWidth(100);

        table.getColumns().addAll(payTypeCol, categoryCol, currentCol, ytdCol, actionsCol);
        return table;
    }

    /**
     * Creates the deductions section with table.
     */
    private VBox createDeductionsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.getStyleClass().add("card");

        // Header with add button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Deductions");
        title.getStyleClass().add("section-header");
        title.setStyle("-fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addDeductionButton = new Button("+ Add Deduction");
        addDeductionButton.setOnAction(e -> addDeductionRow());

        header.getChildren().addAll(title, spacer, addDeductionButton);

        // Deductions table
        deductionsData = FXCollections.observableArrayList();
        deductionsTable = createDeductionsTable();

        section.getChildren().addAll(header, deductionsTable);
        return section;
    }

    /**
     * Creates the deductions table.
     */
    @SuppressWarnings("unchecked")
    private TableView<DeductionRow> createDeductionsTable() {
        TableView<DeductionRow> table = new TableView<>(deductionsData);
        table.setEditable(true);
        table.setPrefHeight(200);

        // Deduction Name column
        TableColumn<DeductionRow, String> nameCol = new TableColumn<>("Deduction Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            event.getRowValue().setName(event.getNewValue());
        });
        nameCol.setPrefWidth(250);

        // Current Amount column
        TableColumn<DeductionRow, String> currentCol = new TableColumn<>("Current Amount");
        currentCol.setCellValueFactory(data -> data.getValue().currentAmountProperty());
        currentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        currentCol.setOnEditCommit(event -> {
            event.getRowValue().setCurrentAmount(event.getNewValue());
        });
        currentCol.setPrefWidth(150);

        // YTD Amount column
        TableColumn<DeductionRow, String> ytdCol = new TableColumn<>("YTD Amount");
        ytdCol.setCellValueFactory(data -> data.getValue().ytdAmountProperty());
        ytdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        ytdCol.setOnEditCommit(event -> {
            event.getRowValue().setYtdAmount(event.getNewValue());
        });
        ytdCol.setPrefWidth(150);

        // Actions column
        TableColumn<DeductionRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Remove");

            {
                deleteBtn.getStyleClass().add("danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                deleteBtn.setOnAction(e -> {
                    DeductionRow row = getTableView().getItems().get(getIndex());
                    deductionsData.remove(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionsCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, currentCol, ytdCol, actionsCol);
        return table;
    }

    /**
     * Processes files and parses paystub data.
     */
    public void processFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        logger.info("Processing {} files", files.size());
        parsingResults.clear();

        // Show processing section
        processingSection.setVisible(true);
        processingSection.setManaged(true);
        dataSection.setVisible(false);
        dataSection.setManaged(false);

        progressBar.setVisible(true);
        progressBar.setProgress(0);
        progressLabel.setText("Processing files...");

        // Create background task for parsing
        Task<Void> parseTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    updateMessage("Processing: " + file.getName());
                    updateProgress(i, files.size());

                    try {
                        ParsingResult result = parser.parse(file);
                        parsingResults.add(result);
                        logger.info("Parsed file: {} with confidence: {}",
                                  file.getName(), result.getConfidenceLevel());
                    } catch (IOException e) {
                        logger.error("Error parsing file: {}", file.getName(), e);
                        ParsingResult errorResult = new ParsingResult();
                        errorResult.addError("Failed to parse file: " + e.getMessage());
                        errorResult.setConfidenceLevel(ParsingResult.ConfidenceLevel.FAILED);
                        parsingResults.add(errorResult);
                    }
                }

                updateProgress(files.size(), files.size());
                return null;
            }
        };

        parseTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            Platform.runLater(() -> progressLabel.setText(newMsg));
        });

        parseTask.progressProperty().addListener((obs, oldProg, newProg) -> {
            Platform.runLater(() -> progressBar.setProgress(newProg.doubleValue()));
        });

        parseTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                progressLabel.setText("Processing complete!");
                statusBar.setStatus("Parsed " + files.size() + " file(s)");

                // Show data section
                currentPaystubIndex = 0;
                loadPaystub(0);
                showDataSection();
            });
        });

        parseTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressLabel.setText("Processing failed!");
                statusBar.setStatus("Error processing files");
                logger.error("Parse task failed", parseTask.getException());
            });
        });

        // Run in background thread
        Thread thread = new Thread(parseTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Shows the data section and hides processing.
     */
    private void showDataSection() {
        processingSection.setVisible(false);
        processingSection.setManaged(false);
        dataSection.setVisible(true);
        dataSection.setManaged(true);
        updateNavigation();
    }

    /**
     * Loads a paystub at the specified index.
     */
    private void loadPaystub(int index) {
        if (index < 0 || index >= parsingResults.size()) {
            return;
        }

        currentPaystubIndex = index;
        ParsingResult result = parsingResults.get(index);
        Paystub paystub = result.getPaystub();

        // Load basic info
        employeeNameField.setText(paystub.getEmployeeName() != null ? paystub.getEmployeeName() : "");
        employerNameField.setText(paystub.getEmployerName() != null ? paystub.getEmployerName() : "");
        payDatePicker.setValue(paystub.getPayDate());
        periodStartPicker.setValue(paystub.getPayPeriodStartDate());
        periodEndPicker.setValue(paystub.getPayPeriodEndDate());
        payFrequencyCombo.setValue(paystub.getPayFrequency());

        // Employment type (default to hourly if not set)
        hourlyRadio.setSelected(true);
        rateField.setText("");

        // Load earnings
        earningsData.clear();
        for (Earning earning : paystub.getEarnings()) {
            earningsData.add(new EarningRow(
                earning.getPayTypeName(),
                earning.getCategory(),
                earning.getCurrentAmount().toString(),
                earning.getYtdAmount().toString()
            ));
        }

        // Load deductions
        deductionsData.clear();
        for (Deduction deduction : paystub.getDeductions()) {
            deductionsData.add(new DeductionRow(
                deduction.getDeductionName(),
                deduction.getCurrentAmount().toString(),
                deduction.getYtdAmount().toString()
            ));
        }

        updateNavigation();
        statusBar.setStatus("Viewing paystub " + (index + 1) + " of " + parsingResults.size());
    }

    /**
     * Updates navigation buttons and label.
     */
    private void updateNavigation() {
        if (parsingResults.isEmpty()) {
            paystubCountLabel.setText("No paystubs");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            return;
        }

        paystubCountLabel.setText(String.format("Paystub %d of %d",
                                                currentPaystubIndex + 1,
                                                parsingResults.size()));
        prevButton.setDisable(currentPaystubIndex == 0);
        nextButton.setDisable(currentPaystubIndex >= parsingResults.size() - 1);
    }

    /**
     * Navigates to the previous paystub.
     */
    private void navigateToPrevious() {
        if (currentPaystubIndex > 0) {
            saveCurrentPaystub();
            loadPaystub(currentPaystubIndex - 1);
        }
    }

    /**
     * Navigates to the next paystub.
     */
    private void navigateToNext() {
        if (currentPaystubIndex < parsingResults.size() - 1) {
            saveCurrentPaystub();
            loadPaystub(currentPaystubIndex + 1);
        }
    }

    /**
     * Saves the current paystub data from the form.
     */
    private void saveCurrentPaystub() {
        if (currentPaystubIndex < 0 || currentPaystubIndex >= parsingResults.size()) {
            return;
        }

        ParsingResult result = parsingResults.get(currentPaystubIndex);
        Paystub paystub = result.getPaystub();

        // Save basic info
        paystub.setEmployeeName(employeeNameField.getText());
        paystub.setEmployerName(employerNameField.getText());
        paystub.setPayDate(payDatePicker.getValue());
        paystub.setPayPeriodStartDate(periodStartPicker.getValue());
        paystub.setPayPeriodEndDate(periodEndPicker.getValue());
        paystub.setPayFrequency(payFrequencyCombo.getValue());

        // Save earnings
        paystub.getEarnings().clear();
        for (EarningRow row : earningsData) {
            try {
                BigDecimal current = new BigDecimal(row.getCurrentAmount());
                BigDecimal ytd = new BigDecimal(row.getYtdAmount());
                paystub.addEarning(new Earning(row.getPayType(), row.getCategory(), current, ytd));
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format in earnings: {}", e.getMessage());
            }
        }

        // Save deductions
        paystub.getDeductions().clear();
        for (DeductionRow row : deductionsData) {
            try {
                BigDecimal current = new BigDecimal(row.getCurrentAmount());
                BigDecimal ytd = new BigDecimal(row.getYtdAmount());
                paystub.addDeduction(new Deduction(row.getName(), current, ytd));
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format in deductions: {}", e.getMessage());
            }
        }
    }

    /**
     * Adds a new earning row.
     */
    private void addEarningRow() {
        earningsData.add(new EarningRow("", PayCategory.BASE_WAGE, "0.00", "0.00"));
    }

    /**
     * Adds a new deduction row.
     */
    private void addDeductionRow() {
        deductionsData.add(new DeductionRow("", "0.00", "0.00"));
    }

    /**
     * Handles the calculate button action.
     */
    private void handleCalculate() {
        saveCurrentPaystub();

        if (parsingResults.isEmpty()) {
            showInfo("No Data", "Please process paystub files first.");
            return;
        }

        // Collect all paystubs
        List<Paystub> allPaystubs = new ArrayList<>();
        for (ParsingResult result : parsingResults) {
            allPaystubs.add(result.getPaystub());
        }

        // Get borrower name from first paystub
        String borrowerName = "Unknown";
        if (!allPaystubs.isEmpty() && allPaystubs.get(0).getEmployeeName() != null) {
            borrowerName = allPaystubs.get(0).getEmployeeName();
        }

        logger.info("Triggering calculation for {} paystubs", allPaystubs.size());
        statusBar.setStatus("Calculating income...");

        // Trigger calculation in CalculationTab
        if (calculationTab != null) {
            calculationTab.performCalculation(allPaystubs, borrowerName);

            // Switch to calculation tab
            if (onCalculateComplete != null) {
                onCalculateComplete.run();
            }

            statusBar.setStatus("Calculation complete");
        } else {
            showInfo("Error", "Calculation tab not available. Please restart the application.");
        }
    }

    /**
     * Creates a field label with consistent styling.
     */
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    /**
     * Shows an info dialog.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets the parsing results.
     */
    public List<ParsingResult> getParsingResults() {
        return parsingResults;
    }

    /**
     * Sets the calculation tab reference.
     *
     * @param calculationTab the calculation tab
     */
    public void setCalculationTab(CalculationTab calculationTab) {
        this.calculationTab = calculationTab;
    }

    /**
     * Sets the callback to run when calculation is triggered.
     *
     * @param callback the callback to run
     */
    public void setOnCalculateComplete(Runnable callback) {
        this.onCalculateComplete = callback;
    }

    /**
     * Row wrapper for earnings table.
     */
    public static class EarningRow {
        private final SimpleStringProperty payType;
        private final SimpleObjectProperty<PayCategory> category;
        private final SimpleStringProperty currentAmount;
        private final SimpleStringProperty ytdAmount;

        public EarningRow(String payType, PayCategory category, String currentAmount, String ytdAmount) {
            this.payType = new SimpleStringProperty(payType);
            this.category = new SimpleObjectProperty<>(category);
            this.currentAmount = new SimpleStringProperty(currentAmount);
            this.ytdAmount = new SimpleStringProperty(ytdAmount);
        }

        public String getPayType() { return payType.get(); }
        public void setPayType(String value) { payType.set(value); }
        public SimpleStringProperty payTypeProperty() { return payType; }

        public PayCategory getCategory() { return category.get(); }
        public void setCategory(PayCategory value) { category.set(value); }
        public SimpleObjectProperty<PayCategory> categoryProperty() { return category; }

        public String getCurrentAmount() { return currentAmount.get(); }
        public void setCurrentAmount(String value) { currentAmount.set(value); }
        public SimpleStringProperty currentAmountProperty() { return currentAmount; }

        public String getYtdAmount() { return ytdAmount.get(); }
        public void setYtdAmount(String value) { ytdAmount.set(value); }
        public SimpleStringProperty ytdAmountProperty() { return ytdAmount; }
    }

    /**
     * Row wrapper for deductions table.
     */
    public static class DeductionRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty currentAmount;
        private final SimpleStringProperty ytdAmount;

        public DeductionRow(String name, String currentAmount, String ytdAmount) {
            this.name = new SimpleStringProperty(name);
            this.currentAmount = new SimpleStringProperty(currentAmount);
            this.ytdAmount = new SimpleStringProperty(ytdAmount);
        }

        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getCurrentAmount() { return currentAmount.get(); }
        public void setCurrentAmount(String value) { currentAmount.set(value); }
        public SimpleStringProperty currentAmountProperty() { return currentAmount; }

        public String getYtdAmount() { return ytdAmount.get(); }
        public void setYtdAmount(String value) { ytdAmount.set(value); }
        public SimpleStringProperty ytdAmountProperty() { return ytdAmount; }
    }
}
