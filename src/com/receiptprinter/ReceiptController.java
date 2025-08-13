package com.receiptprinter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.transform.Scale;
import javafx.print.PrinterJob;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.DayOfWeek;

import javafx.print.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReceiptController implements Initializable {

        @FXML
        private ComboBox<String> headerTypeCombo;
        @FXML
        private VBox customHeaderGroup;
        @FXML
        private TextField customHeaderField;
        @FXML
        private TextField subjectTitleField;
        @FXML
        private ComboBox<String> priorityCombo;
        @FXML
        private TextField dueDateField;
        @FXML
        private TextArea mainContentArea;
        @FXML
        private TextArea previewArea;
        @FXML
        private Label statusLabel;

        @FXML
        private Button todayBtn;
        @FXML
        private Button tomorrowBtn;
        @FXML
        private Button threeDaysBtn;
        @FXML
        private Button weekBtn;
        @FXML
        private Button clearDaysBtn;
        @FXML
        private Button saveBtn;
        @FXML
        private Button printBtn;
        @FXML
        private Button clearBtn;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
                setupComboBoxes();
                setupEventListeners();

                statusLabel.setText("JavaFX Application loaded!");

                // Note: Initial preview setup
                String[] initialHeader = getPredefinedAsciiHeader("NOTE");
                previewArea.appendText(String.join("\n", initialHeader) + "\n");
                // Creation Date
                creationDate();
        }

        private void setupComboBoxes() {
                headerTypeCombo.getItems().addAll("NOTE", "TASK", "REMINDER", "BIBLE", "QUOTE", "GOAL", "IDEA",
                                "CUSTOM");
                headerTypeCombo.setValue("NOTE");

                priorityCombo.getItems().addAll("No Priority", "HIGH", "MEDIUM", "LOW");
        }

        private void setupEventListeners() {
                // Header type change listener
                headerTypeCombo.setOnAction(e -> {
                        boolean isCustom = "CUSTOM".equals(headerTypeCombo.getValue());
                        customHeaderGroup.setVisible(isCustom);
                        customHeaderGroup.setManaged(isCustom);
                        updatePreview();
                });

                // Real-time preview updates
                customHeaderField.textProperty().addListener((obs, old, newText) -> updatePreview());
                subjectTitleField.textProperty().addListener((obs, old, newText) -> updatePreview());
                priorityCombo.setOnAction(e -> updatePreview());
                dueDateField.textProperty().addListener((obs, old, newText) -> updatePreview());
                mainContentArea.textProperty().addListener((obs, old, newText) -> updatePreview());

                // Quick date buttons
                todayBtn.setOnAction(e -> setQuickDate("today"));
                tomorrowBtn.setOnAction(e -> setQuickDate("tomorrow"));
                threeDaysBtn.setOnAction(e -> setQuickDate("3 days"));
                weekBtn.setOnAction(e -> setQuickDate("7 days"));
                clearDaysBtn.setOnAction(e -> dueDateField.clear());

                // Action buttons
                // updateBtn.setOnAction(e -> updatePreview());
                saveBtn.setOnAction(e -> saveReceipt());
                printBtn.setOnAction(e -> printReceipt());
                clearBtn.setOnAction(e -> clearAll());
        }

        private void setQuickDate(String dateStr) {
                dueDateField.setText(dateStr);
                updatePreview();
        }

        private void updatePreview() {
                previewArea.clear();

                // ASCII Art
                String headerType = headerTypeCombo.getValue();
                if ("CUSTOM".equals(headerType)) {
                        String headerText = customHeaderField.getText().toUpperCase();

                        if (!headerText.isEmpty()) {

                                if (headerText.length() > 5) {
                                        headerText = headerText.substring(0, 5);
                                }

                                String[] lines = { "", "", "", "", "" }; // 5 lines for ASCII height

                                // Loop through each character
                                for (int i = 0; i < headerText.length(); i++) {
                                        char c = headerText.charAt(i);
                                        String[] charArt = getCharAscii(c);

                                        // Add each line of this character's ASCII art
                                        for (int lineIndex = 0; lineIndex < 5; lineIndex++) {
                                                lines[lineIndex] += charArt[lineIndex];

                                                // Add spacing between characters (except last one)
                                                if (i < headerText.length() - 1) {
                                                        lines[lineIndex] += " ";
                                                }
                                        }
                                }

                                previewArea.appendText(String.join("\n", lines) + "\n");

                        } else {
                                String[] asciiHeader = getPredefinedAsciiHeader(headerType);
                                previewArea.appendText(String.join("\n", asciiHeader) + "\n");
                        }

                } else {
                        String[] asciiHeader = getPredefinedAsciiHeader(headerType);
                        previewArea.appendText(String.join("\n", asciiHeader) + "\n");
                }

                // Header Field
                if (!subjectTitleField.getText().isEmpty()) {
                        previewArea.appendText(repeat("=", 43));
                        previewArea.appendText(centeredText(subjectTitleField.getText().toUpperCase(), 43) + "\n");
                        previewArea.appendText(repeat("=", 43));
                }

                // Priority
                String priority = priorityCombo.getValue();
                if (!"No Priority".equals(priority) && priority != null) {
                        previewArea.appendText("\n" + centeredText(priorityCombo.getValue(), 43));
                }

                // Dates
                creationDate();

                if (!dueDateField.getText().isEmpty()) {
                        dueDateDaysAdder();
                }

                statusLabel.setText("Preview updated!");

        }

        private void saveReceipt() {
                try {
                        String content = previewArea.getText();

                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save Receipt");
                        fileChooser.setInitialFileName(
                                        "Receipt.txt");

                        fileChooser.getExtensionFilters().addAll(
                                        new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                                        new FileChooser.ExtensionFilter("All Files", "*.*"));

                        // Show save dialog
                        File file = fileChooser.showSaveDialog(saveBtn.getScene().getWindow());

                        if (file != null) {
                                // Write content to file
                                FileWriter writer = new FileWriter(file);
                                writer.write(content);
                                writer.close();

                                statusLabel.setText("Receipt saved to: " + file.getName());
                        } else {
                                statusLabel.setText("Save cancelled");
                        }
                } catch (Exception e) {
                        statusLabel.setText("Save error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void printReceipt() {
                try {
                        String content = previewArea.getText();

                        if (content == null || content.trim().isEmpty()) {
                                statusLabel.setText("Nothing to print!");
                                return;
                        }

                        PrinterJob printerJob = PrinterJob.createPrinterJob();

                        if (printerJob != null) {
                                // Configure page settings for better layout
                                PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
                                Paper paper = pageLayout.getPaper();

                                // Create proper page setup with minimal margins
                                PageLayout newLayout = printerJob.getPrinter().createPageLayout(
                                                paper,
                                                PageOrientation.PORTRAIT,
                                                Printer.MarginType.HARDWARE_MINIMUM // Use minimum margins
                                );
                                printerJob.getJobSettings().setPageLayout(newLayout);

                                // Show print dialog
                                if (printerJob.showPrintDialog(null)) {

                                        // Create a simple Text node (works better than TextArea for printing)
                                        Text printText = new Text(content);
                                        printText.setFont(Font.font("Courier New", 8));
                                        printText.setFill(Color.BLACK);

                                        // Create container with proper sizing
                                        VBox printContainer = new VBox(printText);
                                        printContainer.setPrefWidth(newLayout.getPrintableWidth());
                                        printContainer.setPrefHeight(newLayout.getPrintableHeight());

                                        boolean success = printerJob.printPage(printContainer);

                                        if (success) {
                                                printerJob.endJob();
                                                statusLabel.setText("Receipt printed successfully!");
                                        } else {
                                                statusLabel.setText("Print failed!");
                                        }
                                }
                        }

                } catch (Exception e) {
                        statusLabel.setText("Print error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void clearAll() {
                headerTypeCombo.setValue("NOTE");
                customHeaderField.clear();
                subjectTitleField.clear();
                priorityCombo.setValue("");
                dueDateField.clear();
                mainContentArea.clear();
                customHeaderGroup.setVisible(false);
                customHeaderGroup.setManaged(false);
                updatePreview();
                statusLabel.setText("All fields cleared!");
        }

        private String[] getCharAscii(char c) {
                // Create a Map to store ASCII characters (like JavaScript object)
                Map<Character, String[]> asciiChars = new HashMap<>();

                // Add all the ASCII art characters
                asciiChars.put('A', new String[] {
                                " █████ ",
                                "██   ██",
                                "██   ██",
                                "███████",
                                "██   ██"
                });

                asciiChars.put('B', new String[] {
                                "██████ ",
                                "██   ██",
                                "██████ ",
                                "██   ██",
                                "██████ "
                });

                asciiChars.put('C', new String[] {
                                " ██████",
                                "██     ",
                                "██     ",
                                "██     ",
                                " ██████"
                });

                asciiChars.put('D', new String[] {
                                "██████ ",
                                "██   ██",
                                "██   ██",
                                "██   ██",
                                "██████ "
                });

                asciiChars.put('E', new String[] {
                                "███████",
                                "██     ",
                                "█████  ",
                                "██     ",
                                "███████"
                });

                asciiChars.put('F', new String[] {
                                "███████",
                                "██     ",
                                "█████  ",
                                "██     ",
                                "██     "
                });

                asciiChars.put('G', new String[] {
                                " ██████",
                                "██     ",
                                "██  ███",
                                "██   ██",
                                " ██████"
                });

                asciiChars.put('H', new String[] {
                                "██   ██",
                                "██   ██",
                                "███████",
                                "██   ██",
                                "██   ██"
                });

                asciiChars.put('I', new String[] {
                                "███████",
                                "   ██  ",
                                "   ██  ",
                                "   ██  ",
                                "███████"
                });

                asciiChars.put('J', new String[] {
                                "███████",
                                "     ██",
                                "     ██",
                                "██   ██",
                                " ██████"
                });

                asciiChars.put('K', new String[] {
                                "██   ██",
                                "██  ██ ",
                                "█████  ",
                                "██  ██ ",
                                "██   ██"
                });

                asciiChars.put('L', new String[] {
                                "██     ",
                                "██     ",
                                "██     ",
                                "██     ",
                                "███████"
                });

                asciiChars.put('M', new String[] {
                                "██   ██",
                                "███ ███",
                                "██ █ ██",
                                "██   ██",
                                "██   ██"
                });

                asciiChars.put('N', new String[] {
                                "██   ██",
                                "███  ██",
                                "██ █ ██",
                                "██  ███",
                                "██   ██"
                });

                asciiChars.put('O', new String[] {
                                " ██████ ",
                                "██    ██",
                                "██    ██",
                                "██    ██",
                                " ██████ "
                });

                asciiChars.put('P', new String[] {
                                "██████ ",
                                "██   ██",
                                "██████ ",
                                "██     ",
                                "██     "
                });

                asciiChars.put('Q', new String[] {
                                " ██████ ",
                                "██    ██",
                                "██ █  ██",
                                "██  █ ██",
                                " ██████ "
                });

                asciiChars.put('R', new String[] {
                                "██████ ",
                                "██   ██",
                                "██████ ",
                                "██   ██",
                                "██   ██"
                });

                asciiChars.put('S', new String[] {
                                " ██████ ",
                                "██      ",
                                " ██████ ",
                                "      ██",
                                " ██████ "
                });

                asciiChars.put('T', new String[] {
                                "███████",
                                "   ██  ",
                                "   ██  ",
                                "   ██  ",
                                "   ██  "
                });

                asciiChars.put('U', new String[] {
                                "██   ██",
                                "██   ██",
                                "██   ██",
                                "██   ██",
                                " █████ "
                });

                asciiChars.put('V', new String[] {
                                "██   ██",
                                "██   ██",
                                "██   ██",
                                " ██ ██ ",
                                "  ███  "
                });

                asciiChars.put('W', new String[] {
                                "██   ██",
                                "██   ██",
                                "██ █ ██",
                                "███ ███",
                                "██   ██"
                });

                asciiChars.put('X', new String[] {
                                "██   ██",
                                " ██ ██ ",
                                "  ███  ",
                                " ██ ██ ",
                                "██   ██"
                });

                asciiChars.put('Y', new String[] {
                                "██   ██",
                                " ██ ██ ",
                                "  ███  ",
                                "   ██  ",
                                "   ██  "
                });

                asciiChars.put('Z', new String[] {
                                "███████",
                                "    ██ ",
                                "   ██  ",
                                "  ██   ",
                                "███████"
                });

                asciiChars.put(' ', new String[] {
                                "        ",
                                "        ",
                                "        ",
                                "        ",
                                "        "
                });

                // Return the character's ASCII art, or default if not found
                return asciiChars.getOrDefault(c, new String[] {
                                "██████  ",
                                "██  ██  ",
                                "██████  ",
                                "██  ██  ",
                                "██████  "
                });
        }

        // Add this method to your controller class
        private String[] getPredefinedAsciiHeader(String headerType) {
                switch (headerType) {
                        case "NOTE":
                                return new String[] {
                                                " ██   ██  ██████  ████████ ███████ ",
                                                " ███  ██ ██    ██    ██    ██      ",
                                                " ████ ██ ██    ██    ██    █████   ",
                                                " ██ ████ ██    ██    ██    ██      ",
                                                " ██  ███  ██████     ██    ███████ "
                                };

                        case "TASK":
                                return new String[] {
                                                " ████████  █████   ███████ ██   ██ ",
                                                "    ██    ██   ██  ██      ██  ██  ",
                                                "    ██    ███████  ███████ █████   ",
                                                "    ██    ██   ██       ██ ██  ██  ",
                                                "    ██    ██   ██  ███████ ██   ██ "
                                };

                        case "REMINDER":
                                return new String[] {
                                                " ██████  ███    ███ ",
                                                " ██   ██ ████  ████ ",
                                                " ██████  ██ ████ ██ ",
                                                " ██   ██ ██  ██  ██ ",
                                                " ██   ██ ██      ██ "
                                };

                        case "BIBLE":
                                return new String[] {
                                                " ██████  ██ ██████  ██      ███████ ",
                                                " ██   ██ ██ ██   ██ ██      ██      ",
                                                " ██████  ██ ██████  ██      █████   ",
                                                " ██   ██ ██ ██   ██ ██      ██      ",
                                                " ██████  ██ ██████  ███████ ███████ "
                                };

                        case "QUOTE":
                                return new String[] {
                                                "  ██████  ██    ██  ██████  ████████ ███████ ",
                                                " ██    ██ ██    ██ ██    ██    ██    ██      ",
                                                " ██    ██ ██    ██ ██    ██    ██    █████   ",
                                                " ██ ▄▄ ██ ██    ██ ██    ██    ██    ██      ",
                                                "  ██████   ██████   ██████     ██    ███████ "
                                };

                        case "GOAL":
                                return new String[] {
                                                "  ██████   ██████   █████  ██      ",
                                                " ██       ██    ██ ██   ██ ██      ",
                                                " ██   ███ ██    ██ ███████ ██      ",
                                                " ██    ██ ██    ██ ██   ██ ██      ",
                                                "  ██████   ██████  ██   ██ ███████ "
                                };

                        case "IDEA":
                                return new String[] {
                                                " ██ ██████  ███████  █████  ",
                                                " ██ ██   ██ ██      ██   ██ ",
                                                " ██ ██   ██ █████   ███████ ",
                                                " ██ ██   ██ ██      ██   ██ ",
                                                " ██ ██████  ███████ ██   ██ "
                                };

                        default:
                                return new String[] {
                                                " ██   ██  ██████  ████████ ███████ ",
                                                " ███  ██ ██    ██    ██    ██      ",
                                                " ████ ██ ██    ██    ██    █████   ",
                                                " ██ ████ ██    ██    ██    ██      ",
                                                " ██  ███  ██████     ██    ███████ "
                                };
                }
        }

        // Shortcut Functions

        private void creationDate() {
                // previewArea.appendText("\n\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                previewArea.appendText("\nCreated: " + LocalDateTime.now().format(formatter) + "\n");
        }

        private String repeat(String str, int repetitions) {
                String finalString = "";
                for (int i = 0; i < repetitions; i++) {
                        finalString += str;
                }
                return finalString + "\n";
        }

        private String centeredText(String text, int totalWidth) {

                if (text.length() >= totalWidth) {
                        return text;
                }
                int padding = (totalWidth - text.length()) / 2;
                String centeredText = String.format("%" + padding + "s%s%" + padding + "s", "", text, "");
                return centeredText;
        }

        private void dueDateDaysAdder() {

                // Indicate which value enteres
                int check = -1;
                String dueDate = dueDateField.getText().trim().toLowerCase();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime todayDate = LocalDateTime.now();

                int daysTillDue = 0;
                String dueMessage = "";
                String formattedDate = "";

                // Basic keywords
                if (dueDate.equals("today")) {
                        daysTillDue = 0;
                        formattedDate = todayDate.format(formatter);
                        check = 1;
                        dueMessage = "DUE TODAY!";
                } else if (dueDate.equals("tomorrow") || dueDate.equals("tmr")) {
                        daysTillDue = 1;
                        formattedDate = todayDate.plusDays(1).format(formatter);
                        check = 1;
                        dueMessage = "Due tomorrow";
                }

                // Days ie. Monday, Tuesday
                if (check != 1) {
                        HashMap<String, Integer> dayNames = new HashMap<>();
                        dayNames.put("sunday", 0);
                        dayNames.put("sun", 0);

                        dayNames.put("monday", 1);
                        dayNames.put("mon", 1);

                        dayNames.put("tuesday", 2);
                        dayNames.put("tue", 2);
                        dayNames.put("tues", 2);

                        dayNames.put("wednesday", 3);
                        dayNames.put("wed", 3);

                        dayNames.put("thursday", 4);
                        dayNames.put("thur", 4);
                        dayNames.put("thu", 4);
                        dayNames.put("thurs", 4);

                        dayNames.put("friday", 5);
                        dayNames.put("fri", 5);

                        dayNames.put("saturday", 6);
                        dayNames.put("sat", 6);

                        // Math to figure out how far away
                        Boolean hasNext = false;
                        if (dueDate.contains("next")) {
                                hasNext = true;
                                dueDate = dueDate.replaceAll("\\bnext\\b", "").trim();
                        }

                        if (dayNames.containsKey(dueDate)) {
                                DayOfWeek todayDay = todayDate.getDayOfWeek();
                                String todayDayString = todayDay.toString().toLowerCase();
                                int todayDayInt = dayNames.get(todayDayString);
                                int dueDayInt = dayNames.get(dueDate);

                                if (dueDayInt > todayDayInt) {
                                        daysTillDue = dueDayInt - todayDayInt;
                                } else if (dueDayInt == todayDayInt) {
                                        daysTillDue = 7;
                                } else {
                                        daysTillDue = 7 - (todayDayInt - dueDayInt);
                                }
                                if (hasNext) {
                                        daysTillDue += 7;
                                }
                                formattedDate = todayDate.plusDays(daysTillDue).format(formatter);
                                dueMessage = "Due in " + daysTillDue + " days";
                                check = 1;

                        }

                }

                // Relative days ie. 3 days, 2 weeks
                if (check != 1) {
                        if (dueDate.contains("day")) {
                                String numberPart = dueDate.replaceAll("[^0-9]", "");
                                daysTillDue = Integer.parseInt(numberPart);
                                dueDate = todayDate.plusDays(daysTillDue).format(formatter);

                                dueMessage = "Due in " + daysTillDue + " days";
                        } else if (dueDate.contains("week")) {
                                String numberPart = dueDate.replaceAll("[^0-9]", "");
                                daysTillDue = Integer.parseInt(numberPart) * 7;
                                dueDate = todayDate.plusDays(daysTillDue).format(formatter);

                                dueMessage = "Due in " + daysTillDue + " days";
                        }
                }

                previewArea.appendText("Due Date: " + formattedDate + "\n");
                previewArea.appendText(dueMessage);

        }

        // Commented code
        // Test

}