package myProject.view.detail;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import myProject.controller.AccountController;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.util.LoggerUtils;
import myProject.view.util.ViewUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Die Klasse AccountDetailView stellt die Benutzeroberfläche für die detaillierte Ansicht
 * eines Kontos bereit. Sie ermöglicht dem Benutzer das Verwalten von Transaktionen,
 * das Ausführen von Überweisungen und das Bearbeiten von wiederkehrenden Transaktionen
 * für ein bestimmtes Konto.
 * <p>
 * Wichtige Funktionen umfassen:
 * - Anzeigen von Konto- und Bilanzdetails
 * - Hinzufügen, Bearbeiten und Löschen von Transaktionen
 * - Überweisungen zwischen Konten
 * - Verwaltung von wiederkehrenden Transaktionen
 * <p>
 * Diese Klasse nutzt die Controller AccountController und TransactionController zur
 * Interaktion mit der zugrunde liegenden Logik.
 */

public class AccountDetailView {
    private final AccountController accountController;
    private final TransactionController transactionController;
    private final BorderPane root;
    private Account account;
    private Label balanceLabel;
    private TableView<Transaction> transactionsTable;

    // Konstruktor zum Initialisieren der Controller und des Layouts
    public AccountDetailView(AccountController accountController, TransactionController transactionController, BorderPane root) {
        this.accountController = accountController;
        this.transactionController = transactionController;
        if (root == null) {
            throw new IllegalArgumentException("Root layout cannot be null.");
        }
        this.root = root;
    }

    // Methode zum Anzeigen der Kontodetails
    // Methode zum Anzeigen der Detailansicht eines Kontos
    public void showAccountDetailView(Account account) {
        this.account = account;

        // Erstelle Label zur Anzeige der Bilanz
        balanceLabel = new Label("Balance: " + String.format("%.2f", account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8be9fd;");

        // Aktualisiere den Status von ausstehenden Transaktionen und die Kontobilanz
        updateAccountBalance();

        // Obere Sektion: Anzeige des Kontonamens und der Bilanz
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label("Account: " + account.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        topSection.getChildren().addAll(nameLabel, balanceLabel);

        // Hauptsektion: Tabelle zur Anzeige von Transaktionen
        setupTransactionTable(new GridPane());

        StackPane tableContainer = new StackPane(transactionsTable);
        tableContainer.setAlignment(Pos.CENTER);

        // Untere Sektion: Buttons für Aktionen (Einnahme, Ausgabe, Überweisung)
        HBox buttonBox = new HBox(20);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setAlignment(Pos.CENTER);

        Button incomeButton = createRoundIconButton("/icons/icons8-add-dollar-50.png");
        incomeButton.setOnAction(e -> showTransactionForm("income"));

        Button expenseButton = createRoundIconButton("/icons/icons8-delete-dollar-50.png");
        expenseButton.setOnAction(e -> showTransactionForm("expense"));
        Button transferButton = createRoundIconButton("/icons/icons8-exchange-48.png");
        transferButton.setOnAction(e -> showTransferForm());

        buttonBox.getChildren().addAll(incomeButton, expenseButton, transferButton);

        VBox mainLayout = new VBox(20);
        mainLayout.getChildren().addAll(topSection, tableContainer, buttonBox);

        root.setCenter(mainLayout);
    }

    // Methode zum Anzeigen des Transferformulars
    private void showTransferForm() {
        // Log-Information über die Anzeige des Transferformulars
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Zeige Transferformular für Konto: " + account.getName());

        // Erstelle ein vertikales Layout für das Formular
        VBox transferForm = new VBox(15);
        transferForm.setPadding(new Insets(20));

        // Anzeige des verfügbaren Kontostands
        Label availableBalanceLabel = new Label("Available Balance: " + ViewUtils.formatCurrency(account.getBalance()));

        // Eingabefeld für den Betrag
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        // Dropdown zur Auswahl des Zielkontos
        ComboBox<Account> targetAccountDropdown = createAccountDropdown(account.getUserId());
        targetAccountDropdown.setPromptText("Select Target Account");

        // Slider für die Auswahl des Transferbetrags (min: 0, max: aktueller Kontostand)
        Slider amountSlider = new Slider(0, account.getBalance(), 0);
        amountSlider.setShowTickLabels(true);
        amountSlider.setShowTickMarks(true);
        amountSlider.setMajorTickUnit(account.getBalance() / 4); // Tick-Mark basierend auf dem Kontostand

        // Flag, um rekursive Updates zwischen Textfeld und Slider zu vermeiden
        final boolean[] isUpdating = {false};

        // Listener, um den Wert des Textfelds zu aktualisieren, wenn der Slider bewegt wird
        amountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                isUpdating[0] = true; // Verhindere rekursive Updates
                amountField.setText(String.format("%.2f", newVal.doubleValue()));
                isUpdating[0] = false; // Setze das Flag nach dem Update zurück
            }
        });

        // Listener, um den Slider anzupassen, wenn das Textfeld geändert wird
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                try {
                    double value = Double.parseDouble(newVal);
                    if (value >= 0 && value <= account.getBalance()) {
                        isUpdating[0] = true; // Verhindere rekursive Updates
                        amountSlider.setValue(value);
                        isUpdating[0] = false; // Setze das Flag nach dem Update zurück
                    } else {
                        // Setze den alten Wert zurück, wenn der Betrag ungültig ist
                        amountField.setText(oldVal);
                    }
                } catch (NumberFormatException e) {
                    // Logge die Fehlermeldung bei einer ungültigen Eingabe
                    LoggerUtils.logError(AccountDetailView.class.getName(), "Ungültiger Betrag eingegeben: " + newVal, e);
                    // Setze den alten Wert zurück, wenn die Eingabe kein gültiger Betrag ist
                    amountField.setText(oldVal);
                }
            }
        });

        // Button für den Transfer des gesamten verfügbaren Betrags
        Button transferAllButton = new Button("Transfer All");
        transferAllButton.setOnAction(e -> amountSlider.setValue(account.getBalance())); // Setze den Slider auf den maximalen Kontostand

        // Button zum Ausführen des Transfers
        Button transferButton = new Button("Execute Transfer");
        transferButton.setOnAction(e -> executeTransfer(amountField, targetAccountDropdown));

        // Füge alle Elemente dem Transferformular hinzu
        transferForm.getChildren().addAll(
                new Label("Transfer Funds from " + account.getName()),
                availableBalanceLabel,
                new Label("Amount:"), amountField, amountSlider, transferAllButton,
                new Label("To Account:"), targetAccountDropdown,
                transferButton
        );

        // Setze das Transferformular in den zentralen Bereich des Root-Panes
        root.setCenter(transferForm);
    }


    // Methode zum Erstellen eines Konto-Dropdowns für den eingeloggten Benutzer
    private ComboBox<Account> createAccountDropdown(String userId) {
        // Log-Information über das Erstellen des Dropdowns für die Konten des Benutzers
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Erstelle Konto-Dropdown für Benutzer: " + userId);

        // Erstelle ein neues ComboBox-Element für die Konten
        ComboBox<Account> accountDropdown = new ComboBox<>();

        // Lade alle Konten des Benutzers, aber filtere das aktuelle Konto heraus
        accountDropdown.setItems(transactionController.getAccountsForUser(userId)
                .filtered(acc -> !acc.getId().equals(account.getId())));
        accountDropdown.setPromptText("Choose Account");  // GUI-Text auf Englisch

        // Setze eine benutzerdefinierte Cell Factory, um die Kontonamen im Dropdown anzuzeigen
        accountDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                // Wenn leer, keinen Text anzeigen, ansonsten den Kontonamen
                setText(empty ? "" : item.getName());
            }
        });

        // Setze die Schaltflächenzelle, um den ausgewählten Kontonamen im Dropdown anzuzeigen
        accountDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                // Wenn leer, keinen Text anzeigen, ansonsten den Kontonamen
                setText(empty ? "" : item.getName());
            }
        });

        // Rückgabe des erstellten Dropdowns
        return accountDropdown;
    }


    // Methode zum Ausführen des Transfers zwischen Konten
    private void executeTransfer(TextField amountField, ComboBox<Account> targetAccountDropdown) {
        try {
            // Konvertiere den Betrag aus dem Textfeld in eine Zahl
            double amount = Double.parseDouble(amountField.getText());
            // Hole das ausgewählte Zielkonto aus dem Dropdown
            Account targetAccount = targetAccountDropdown.getValue();

            // Überprüfe, ob genügend Guthaben vorhanden ist oder der Betrag ungültig ist
            if (account.getBalance() < amount || amount <= 0) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Insufficient funds or invalid amount.");
                return;
            }

            // Erstelle zwei Transaktionen: Eine Ausgabe für das Quellkonto und eine Einnahme für das Zielkonto
            Transaction expense = new Transaction("Transfer to " + targetAccount.getName(), -amount, "expense", null, account, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()));
            Transaction income = new Transaction("Transfer from " + account.getName(), amount, "income", null, targetAccount, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()));

            // Speichere beide Transaktionen (Ausgabe und Einnahme)
            transactionController.createTransaction(expense);
            transactionController.createTransaction(income);

            // Aktualisiere die Bilanzen für beide Konten
            accountController.updateAccountBalance(account);         // Für das Quellkonto
            accountController.updateAccountBalance(targetAccount);   // Für das Zielkonto
            LoggerUtils.logInfo(AccountDetailView.class.getName(), "Transfer erfolgreich: Von " + account.getName() + " zu " + targetAccount.getName());

            // Zeige die aktualisierten Kontodetails für das Quellkonto an
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            // Fehler beim Konvertieren des Betrags, ungültige Eingabe
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount.");
            LoggerUtils.logError(AccountDetailView.class.getName(), "Ungültiger Betrag: " + e.getMessage(), e);
        } catch (Exception e) {
            // Allgemeiner Fehler beim Ausführen des Transfers
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to execute transfer: " + e.getMessage());
            LoggerUtils.logError(AccountDetailView.class.getName(), "Fehler beim Transfer: " + e.getMessage(), e);
        }
    }


    // Methode zum Anzeigen des Formulars für Einnahmen oder Ausgaben
    private void showTransactionForm(String type) {
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Zeige Transaktionsformular für: " + type);

        // Erstelle das Formularlayout
        VBox formView = new VBox(15);
        formView.setPadding(new Insets(20));
        formView.getStyleClass().add("form-view");

        Label formLabel = new Label("Add " + (type.equals("income") ? "Income" : "Expense") + " to " + account.getName());
        formLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        formView.getChildren().add(formLabel);

        // Textfelder für Beschreibung und Betrag
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        // Datums- und Zeitauswahl
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                // Disable future dates
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        // Zeit-Eingabefeld mit Standardformatierung und Validierung
        TextField timeField = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        // Add listener to restrict time if today's date is selected
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(LocalDate.now())) {
                LocalTime now = LocalTime.now();
                if (LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm")).isAfter(now)) {
                    timeField.setText(now.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });

        // Dropdown for category selection
        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());

        // Button zum Speichern der Transaktion
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveTransaction(type, descriptionField, amountField, datePicker, timeField, categoryDropdown));

        // Button zum Abbrechen und Zurückkehren zur Detailansicht des Kontos
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        // Speichern- und Abbrechen-Buttons in einer horizontalen Box
        HBox buttonBox = new HBox(10, saveButton, cancelButton);

        // Füge alle Elemente zum Formular hinzu
        formView.getChildren().addAll(descriptionField, amountField, new HBox(10, datePicker, timeField),
                new Label("Category:"), categoryDropdown, buttonBox);

        // Setze das Formular in die Mitte des Root-Pane
        root.setCenter(formView);
    }


    // Methode zum Speichern einer Transaktion (Einnahme oder Ausgabe)
    private void saveTransaction(String type, TextField descriptionField, TextField amountField, DatePicker datePicker, TextField timeField, ComboBox<Category> categoryDropdown) {
        try {
            LoggerUtils.logInfo(AccountDetailView.class.getName(), "Speichere neue Transaktion - Typ: " + type);

            // Erfasse die Beschreibung, den Betrag, das Datum und die Zeit
            String description = descriptionField.getText();
            double amount = Math.abs(Double.parseDouble(amountField.getText()));

            LocalDate date = datePicker.getValue();
            LocalTime time;

            // Überprüfe und parse die Zeit
            try {
                time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid time format. Please enter time as HH:mm.");
                return;
            }

            // Erfasse die Kategorie und Wiederholungseinstellungen
            Category category = categoryDropdown.getValue();

            // Erstelle eine neue Transaktion mit den eingegebenen Daten
            Transaction transaction = new Transaction(
                    description,
                    type.equals("income") ? amount : -amount,  // Negativer Betrag für Ausgaben
                    type,
                    null, // Darf User hier null sein?
                    account,
                    category,
                    Date.valueOf(date),
                    Time.valueOf(time)

            );

            // Speichere die Transaktion über den TransactionController
            transactionController.createTransaction(transaction);
            LoggerUtils.logInfo(AccountDetailView.class.getName(), "Transaktion erfolgreich gespeichert - " + transaction);

            // Aktualisiere die Kontobilanz
            updateAccountBalance();

            // Zeige die aktualisierten Kontodetails an
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.");
            LoggerUtils.logError(AccountDetailView.class.getName(), "Fehler - Ungültiger Betrag eingegeben.", e);
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to save transaction: " + e.getMessage());
            LoggerUtils.logError(AccountDetailView.class.getName(), "Fehler beim Speichern der Transaktion", e);
        }
    }


    private void editTransaction(Transaction transaction) {
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Bearbeite Transaktion - " + transaction);

        VBox formView = new VBox(15);
        formView.setPadding(new Insets(20));
        formView.getStyleClass().add("form-view");

        Label formLabel = new Label("Edit Transaction for " + account.getName());
        formLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        formView.getChildren().add(formLabel);

        TextField descriptionField = new TextField(transaction.getDescription());
        TextField amountField = new TextField(String.valueOf(Math.abs(transaction.getAmount())));
        DatePicker datePicker = new DatePicker(transaction.getDate().toLocalDate());
        datePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                // Disable future dates
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        TextField timeField = new TextField(transaction.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Add listener to restrict time if today's date is selected
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(LocalDate.now())) {
                LocalTime now = LocalTime.now();
                if (LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm")).isAfter(now)) {
                    timeField.setText(now.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });

        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());
        categoryDropdown.setValue(transaction.getCategory());

        Button saveButton = new Button("Update Transaction");
        saveButton.setOnAction(e -> {
            try {
                String description = descriptionField.getText();
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                Category category = categoryDropdown.getValue();

                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setDate(Date.valueOf(date));
                transaction.setTime(Time.valueOf(time));
                transaction.setCategory(category);

                transactionController.updateTransaction(transaction);  // Update the transaction
                LoggerUtils.logInfo(AccountDetailView.class.getName(), "Transaktion erfolgreich aktualisiert - " + transaction);

                refreshTransactionTable(); // Refresh the transaction table
                updateAccountBalance(); // Update balance after editing
                showAccountDetailView(account); // Refresh the account view

            } catch (NumberFormatException | SQLException ex) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.");
                LoggerUtils.logError(AccountDetailView.class.getName(), "Fehler - Ungültiger Betrag eingegeben.", ex);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        formView.getChildren().addAll(descriptionField, amountField, new HBox(10, datePicker, timeField), new Label("Category:"), categoryDropdown, buttonBox);

        root.setCenter(formView);
    }


    // Methode zum Erstellen eines Kategorie-Dropdowns für den eingeloggten Benutzer
    private ComboBox<Category> createCategoryDropdown(String userId) {
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Erstelle Kategorie-Dropdown für Benutzer - " + userId);

        ComboBox<Category> categoryDropdown = new ComboBox<>();

        try {
            // Hole die Kategorien des Benutzers und füge sie dem Dropdown hinzu
            categoryDropdown.setItems(transactionController.getAllCategoriesForUser(userId));
            categoryDropdown.setPromptText("Choose Category");

            // Setze eine benutzerdefinierte Cell Factory, um die Kategorie-Namen anzuzeigen
            categoryDropdown.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });

            // Setze die Schaltflächenzelle, um den ausgewählten Kategorienamen anzuzeigen
            categoryDropdown.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });

        } catch (Exception e) {
            // Logge Fehler, falls das Erstellen des Dropdowns fehlschlägt
            LoggerUtils.logError(AccountDetailView.class.getName(), "Fehler beim Erstellen des Kategorie-Dropdowns für Benutzer: " + userId, e);
        }

        return categoryDropdown;
    }

    private void deleteTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Do you want to permanently delete this transaction?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                transactionController.deleteTransaction(transaction);  // Delete transaction
                LoggerUtils.logInfo(AccountDetailView.class.getName(), "Einmalige Transaktion gelöscht.");
                refreshTransactionTable(); // Refresh the transaction table
                updateAccountBalance(); // Update balance after deletion
            }
        });
    }


    // Methode zur Erstellung runder Buttons mit Icons
    private Button createRoundIconButton(String iconPath) {
        ImageView iconView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        iconView.setFitHeight(30); // Größere Icons für bessere Sichtbarkeit
        iconView.setFitWidth(30);

        // Erstelle einen Button nur mit Icon
        Button button = new Button("", iconView);
        button.setStyle("-fx-background-color: #50fa7b; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;");

        // Ändere die Hintergrundfarbe des Buttons beim Hovern
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #ff79c6; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #50fa7b; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;"));

        return button;
    }

    // Methode zur Aktualisierung der Kontobilanz
    private void updateAccountBalance() {
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Aktualisiere Kontobilanz für Konto - " + account.getName());

        // Rufe alle abgeschlossenen Transaktionen für das Konto ab
        List<Transaction> completedTransactions = transactionController.getCompletedTransactionsByAccount(account.getName());

        // Berechne die Bilanz basierend auf abgeschlossenen Transaktionen
        double newBalance = completedTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Setze die neue Bilanz
        account.setBalance(newBalance);
        accountController.updateAccount(account);

        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Neue Bilanz berechnet: " + newBalance);

        // Aktualisiere die Bilanzanzeige in der UI
        if (balanceLabel != null) {
            balanceLabel.setText("Balance: " + String.format("%.2f", newBalance));
        }
    }

    // Methode zum Einrichten der Transaktionstabelle mit einem Rechtsklick-Kontextmenü
    private void setupTransactionTable(GridPane detailView) {
        transactionsTable = new TableView<>();

        List<TableColumn<Transaction, String>> columns = getTableColumns();
        transactionsTable.getColumns().addAll(columns);

        // Füge ein Kontextmenü für jede Zeile in der Tabelle hinzu
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(event -> {
                Transaction selectedTransaction = row.getItem();
                if (selectedTransaction != null) {
                    editTransaction(selectedTransaction); // Bearbeiten der ausgewählten Transaktion
                }
            });

            MenuItem deleteItem = getMenuItem(row);


            contextMenu.getItems().addAll(editItem, deleteItem);

            // Zeige das Kontextmenü nur für nicht-leere Zeilen an
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

            return row;
        });

        refreshTransactionTable();
        detailView.add(transactionsTable, 0, 3, 3, 1); // Positioniere die Tabelle im Grid, über alle Spalten hinweg
    }

    private MenuItem getMenuItem(TableRow<Transaction> row) {
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Transaction selectedTransaction = row.getItem();
            if (selectedTransaction != null) {
                deleteTransaction(selectedTransaction); // Use deleteTransaction() method for deletion
            }
        });
        return deleteItem;
    }


    // Erstellung der Tabellenspalten für die Transaktionstabelle
    private static List<TableColumn<Transaction, String>> getTableColumns() {
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asString());

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty().asString());

        TableColumn<Transaction, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data -> {
            if (data.getValue().getCategory() != null) {
                return data.getValue().getCategory().nameProperty();
            } else {
                return new SimpleStringProperty("No Category");
            }
        });

        TableColumn<Transaction, String> accountColumn = new TableColumn<>("Account");
        accountColumn.setCellValueFactory(data -> {
            if (data.getValue().getAccount() != null) {
                return data.getValue().getAccount().nameProperty();
            } else {
                return new SimpleStringProperty("No Account");
            }
        });

        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());

        // Füge alle Spalten zur Tabelle hinzu
        List<TableColumn<Transaction, String>> columns = new ArrayList<>();
        columns.add(descriptionColumn);
        columns.add(amountColumn);
        columns.add(dateColumn);
        columns.add(timeColumn);
        columns.add(categoryColumn);
        columns.add(accountColumn);
        columns.add(typeColumn);
        return columns;
    }

    private void refreshTransactionTable() {
        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Aktualisiere Transaktionstabelle für Konto - " + account.getName());

        // Hole nur einmalige und abgeschlossene Transaktionen für das Konto
        ObservableList<Transaction> regularTransactions = transactionController.getTransactionsByAccount(account.getName());

        // Filtere die "Initial Balance"-Transaktion heraus und sortiere nach Datum
        List<Transaction> filteredTransactions = regularTransactions.stream()
                .filter(t -> !t.getDescription().equals("Initial Balance"))  // Entferne "Initial Balance"
                .sorted(Comparator.comparing(Transaction::getDate))  // Sortiere nach Datum
                .collect(Collectors.toList());

        // Setze die gefilterten Transaktionen in der Tabelle
        transactionsTable.setItems(FXCollections.observableArrayList(filteredTransactions));

        LoggerUtils.logInfo(AccountDetailView.class.getName(), "Transaktionstabelle aktualisiert und sortiert für Konto: " + account.getName());
    }

}