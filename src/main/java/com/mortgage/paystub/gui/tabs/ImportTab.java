package com.mortgage.paystub.gui.tabs;

import com.mortgage.paystub.gui.StatusBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Import tab for selecting and managing paystub files.
 * Supports file chooser and drag-and-drop for PDF and image files.
 *
 * @author James Bennett
 * @version 1.0
 */
public class ImportTab extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(ImportTab.class);

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(
            ".pdf", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp"
    );

    private final ObservableList<File> selectedFiles;
    private final ListView<File> fileListView;
    private final Button selectFilesButton;
    private final Button clearAllButton;
    private final Button processFilesButton;
    private final VBox dropZone;
    private final Label fileCountLabel;
    private final StatusBar statusBar;
    private AnalysisTab analysisTab;
    private Runnable onProcessComplete;

    /**
     * Creates a new ImportTab.
     *
     * @param statusBar the status bar for displaying messages
     */
    public ImportTab(StatusBar statusBar) {
        super(20);
        this.statusBar = statusBar;
        this.selectedFiles = FXCollections.observableArrayList();

        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);

        // Title section
        Label title = new Label("Import Paystub Files");
        title.getStyleClass().add("section-header");

        Label description = new Label(
                "Select PDF or image files containing paystub data. " +
                "You can use the file chooser or drag and drop files below."
        );
        description.setWrapText(true);
        description.setMaxWidth(700);
        description.setStyle("-fx-text-fill: #757575; -fx-font-size: 13px;");

        // Drop zone
        dropZone = createDropZone();

        // File list section
        VBox fileListSection = createFileListSection();
        fileCountLabel = (Label) fileListSection.getChildren().get(0);

        // Initialize file list view
        fileListView = new ListView<>(selectedFiles);
        fileListView.setCellFactory(param -> new FileListCell());
        fileListView.setPrefHeight(200);
        fileListView.setPlaceholder(new Label("No files selected"));
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        fileListSection.getChildren().add(1, fileListView);

        // Create buttons (before creating layout)
        selectFilesButton = new Button("Select Files...");
        selectFilesButton.getStyleClass().add("secondary");
        selectFilesButton.setOnAction(e -> handleSelectFiles());

        clearAllButton = new Button("Clear All");
        clearAllButton.getStyleClass().add("secondary");
        clearAllButton.setOnAction(e -> handleClearAll());

        processFilesButton = new Button("Process Files");
        processFilesButton.getStyleClass().add("success");
        processFilesButton.setOnAction(e -> handleProcessFiles());

        // Action buttons layout
        HBox actionButtons = createActionButtons();

        this.getChildren().addAll(title, description, dropZone, fileListSection, actionButtons);

        // Set up listeners
        selectedFiles.addListener((javafx.collections.ListChangeListener<File>) c -> updateUI());
        updateUI();
    }

    /**
     * Creates the drag-and-drop zone.
     */
    private VBox createDropZone() {
        VBox zone = new VBox(15);
        zone.setAlignment(Pos.CENTER);
        zone.setPadding(new Insets(40));
        zone.setMaxWidth(600);
        zone.setMinHeight(150);
        zone.getStyleClass().addAll("card", "drop-zone");

        Label icon = new Label("\uD83D\uDCC1");
        icon.getStyleClass().add("drop-zone-icon");

        Label prompt = new Label("Drag and drop files here");
        prompt.getStyleClass().add("drop-zone-prompt");

        Label hint = new Label("or click the button below to browse");
        hint.getStyleClass().add("drop-zone-hint");

        zone.getChildren().addAll(icon, prompt, hint);

        // Set up drag-and-drop handlers
        setupDragAndDrop(zone);

        return zone;
    }

    /**
     * Sets up drag-and-drop event handlers for the drop zone.
     */
    private void setupDragAndDrop(VBox zone) {
        zone.setOnDragOver(event -> handleDragOver(event, zone));
        zone.setOnDragExited(event -> handleDragExited(event, zone));
        zone.setOnDragDropped(event -> handleDragDropped(event, zone));
    }

    /**
     * Handles drag over event.
     */
    private void handleDragOver(DragEvent event, VBox zone) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            // Check if at least one file is supported
            boolean hasSupported = db.getFiles().stream()
                    .anyMatch(this::isSupportedFile);

            if (hasSupported) {
                event.acceptTransferModes(TransferMode.COPY);
                zone.setStyle(
                        "-fx-border-style: dashed; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-color: #1E88E5; " +
                        "-fx-background-color: #E3F2FD;"
                );
            } else {
                zone.setStyle(
                        "-fx-border-style: dashed; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-color: #E53935; " +
                        "-fx-background-color: #FFEBEE;"
                );
            }
        }
        event.consume();
    }

    /**
     * Handles drag exited event.
     */
    private void handleDragExited(DragEvent event, VBox zone) {
        zone.setStyle(
                "-fx-border-style: dashed; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #BDBDBD; " +
                "-fx-background-color: #FAFAFA;"
        );
        event.consume();
    }

    /**
     * Handles drag dropped event.
     */
    private void handleDragDropped(DragEvent event, VBox zone) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            int added = 0;
            int rejected = 0;

            for (File file : files) {
                if (isSupportedFile(file)) {
                    if (!selectedFiles.contains(file)) {
                        selectedFiles.add(file);
                        added++;
                        logger.info("Added file: {}", file.getName());
                    }
                } else {
                    rejected++;
                    logger.warn("Rejected unsupported file: {}", file.getName());
                }
            }

            if (added > 0) {
                statusBar.setStatus(String.format("Added %d file%s", added, added == 1 ? "" : "s"));
                success = true;
            }

            if (rejected > 0) {
                showWarning(String.format(
                        "%d file%s rejected. Only PDF and image files are supported.",
                        rejected, rejected == 1 ? "" : "s"
                ));
            }
        }

        // Reset drop zone style
        zone.setStyle(
                "-fx-border-style: dashed; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #BDBDBD; " +
                "-fx-background-color: #FAFAFA;"
        );

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Creates the file list section.
     */
    private VBox createFileListSection() {
        VBox section = new VBox(10);
        section.setMaxWidth(800);

        Label fileCountLabel = new Label("Selected Files: 0");
        fileCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        section.getChildren().add(fileCountLabel);
        return section;
    }

    /**
     * Creates the action buttons layout.
     */
    private HBox createActionButtons() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(800);

        // Left side buttons
        HBox leftButtons = new HBox(10);
        leftButtons.setAlignment(Pos.CENTER_LEFT);
        leftButtons.getChildren().addAll(selectFilesButton, clearAllButton);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right side button
        HBox rightButtons = new HBox();
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        rightButtons.getChildren().add(processFilesButton);

        container.getChildren().addAll(leftButtons, spacer, rightButtons);
        return container;
    }

    /**
     * Handles the select files button action.
     */
    private void handleSelectFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Paystub Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported Files", "*.pdf", "*.png", "*.jpg", "*.jpeg", "*.tif", "*.tiff", "*.bmp"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.tif", "*.tiff", "*.bmp")
        );

        // Get window from this component
        Window window = this.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(window);

        if (files != null && !files.isEmpty()) {
            int added = 0;
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    added++;
                }
            }
            statusBar.setStatus(String.format("Added %d file%s", added, added == 1 ? "" : "s"));
            logger.info("Selected {} files via file chooser", added);
        }
    }

    /**
     * Handles the clear all button action.
     */
    private void handleClearAll() {
        if (!selectedFiles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear All Files");
            alert.setHeaderText("Remove all files?");
            alert.setContentText("Are you sure you want to remove all " + selectedFiles.size() + " file(s)?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    int count = selectedFiles.size();
                    selectedFiles.clear();
                    statusBar.setStatus("Cleared " + count + " files");
                    logger.info("Cleared all files");
                }
            });
        }
    }

    /**
     * Handles the process files button action.
     */
    private void handleProcessFiles() {
        if (selectedFiles.isEmpty()) {
            return;
        }

        logger.info("Processing {} files", selectedFiles.size());
        statusBar.setStatus("Processing " + selectedFiles.size() + " file(s)...");

        // Trigger parsing in AnalysisTab
        if (analysisTab != null) {
            List<File> filesToProcess = new ArrayList<>(selectedFiles);
            analysisTab.processFiles(filesToProcess);

            // Switch to analysis tab after processing starts
            if (onProcessComplete != null) {
                onProcessComplete.run();
            }
        } else {
            showInfo("Processing", "Analysis tab not available. Please restart the application.");
        }
    }

    /**
     * Removes a file from the list.
     */
    private void removeFile(File file) {
        selectedFiles.remove(file);
        statusBar.setStatus("Removed: " + file.getName());
        logger.info("Removed file: {}", file.getName());
    }

    /**
     * Updates the UI based on the current state.
     */
    private void updateUI() {
        fileCountLabel.setText("Selected Files: " + selectedFiles.size());
        clearAllButton.setDisable(selectedFiles.isEmpty());
        processFilesButton.setDisable(selectedFiles.isEmpty());
    }

    /**
     * Checks if a file is supported based on its extension.
     */
    private boolean isSupportedFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    /**
     * Gets the file type icon based on extension.
     */
    private String getFileIcon(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            return "\uD83D\uDCC4"; // Document icon
        } else {
            return "\uD83D\uDDBC"; // Picture icon
        }
    }

    /**
     * Formats file size in human-readable format.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return new DecimalFormat("#.#").format(kb) + " KB";
        }
        double mb = kb / 1024.0;
        return new DecimalFormat("#.##").format(mb) + " MB";
    }

    /**
     * Shows a warning dialog.
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
     * Gets the list of selected files.
     */
    public ObservableList<File> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * Sets the analysis tab reference for processing files.
     *
     * @param analysisTab the analysis tab
     */
    public void setAnalysisTab(AnalysisTab analysisTab) {
        this.analysisTab = analysisTab;
    }

    /**
     * Sets the callback to run when processing is triggered.
     *
     * @param callback the callback to run
     */
    public void setOnProcessComplete(Runnable callback) {
        this.onProcessComplete = callback;
    }

    /**
     * Custom list cell for displaying files.
     */
    private class FileListCell extends ListCell<File> {
        private final HBox content;
        private final Label iconLabel;
        private final Label nameLabel;
        private final Label sizeLabel;
        private final Button removeButton;

        public FileListCell() {
            super();

            content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(5));

            iconLabel = new Label();
            iconLabel.setStyle("-fx-font-size: 20px;");

            VBox textBox = new VBox(2);
            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            sizeLabel = new Label();
            sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

            textBox.getChildren().addAll(nameLabel, sizeLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            removeButton = new Button("Remove");
            removeButton.getStyleClass().add("danger");
            removeButton.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");

            content.getChildren().addAll(iconLabel, textBox, spacer, removeButton);
        }

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);

            if (empty || file == null) {
                setGraphic(null);
            } else {
                iconLabel.setText(getFileIcon(file));
                nameLabel.setText(file.getName());
                sizeLabel.setText(formatFileSize(file.length()) + " - " + file.getParent());
                removeButton.setOnAction(e -> removeFile(file));
                setGraphic(content);
            }
        }
    }
}
