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
    public void showAccountDetailView(Account account) {
        this.account = account;

        // Initialisiere balanceLabel und rufe dann updateAccountBalance auf
        balanceLabel = new Label("Balance: " + String.format("%.2f", account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8be9fd;");

        // Transaktionsstatus vor der Anzeige der Kontodetails aktualisieren
        updateTransactionStatuses();
        updateAccountBalance();


        // Obere Sektion: Kontoname und Bilanz
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label("Account: " + account.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        topSection.getChildren().addAll(nameLabel, balanceLabel);

        // Hauptsektion: Tabelle für Transaktionen zentriert anzeigen
        setupTransactionTable(new GridPane());

        // Zentriere die Tabelle
        StackPane tableContainer = new StackPane(transactionsTable);
        tableContainer.setAlignment(Pos.CENTER);

        // Untere Sektion: Icon-basierte Aktionsbuttons
        HBox buttonBox = new HBox(20);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setAlignment(Pos.CENTER);

        // Buttons für Income, Expense und Transfer
        Button incomeButton = createRoundIconButton("/icons/icons8-add-dollar-50.png");
        incomeButton.setOnAction(e -> showTransactionForm("income"));

        Button expenseButton = createRoundIconButton("/icons/icons8-delete-dollar-50.png");
        expenseButton.setOnAction(e -> showTransactionForm("expense"));

        Button transferButton = createRoundIconButton("/icons/icons8-exchange-48.png");
        transferButton.setOnAction(e -> showTransferForm());

        buttonBox.getChildren().addAll(incomeButton, expenseButton, transferButton);

        // Hauptlayout: Stapele die Sektionen vertikal
        VBox mainLayout = new VBox(20);
        mainLayout.getChildren().addAll(topSection, tableContainer, buttonBox);

        // Setze das Layout ins Zentrum des Root-Pane
        root.setCenter(mainLayout);
    }

    // Methode zum Anzeigen des Transferformulars
    private void showTransferForm() {
        System.out.println("AccountDetailView.showTransferForm: Displaying transfer form for account - " + account.getName());

        VBox transferForm = new VBox(15);
        transferForm.setPadding(new Insets(20));

        Label availableBalanceLabel = new Label("Available Balance: " + ViewUtils.formatCurrency(account.getBalance()));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<Account> targetAccountDropdown = createAccountDropdown(account.getUserId());
        targetAccountDropdown.setPromptText("Select Target Account");

        // Slider für den Transferbetrag
        Slider amountSlider = new Slider(0, account.getBalance(), 0); // Min: 0, Max: aktueller Kontostand, Startwert: 0
        amountSlider.setShowTickLabels(true);
        amountSlider.setShowTickMarks(true);
        amountSlider.setMajorTickUnit(account.getBalance() / 4); // Setze die Tick-Marken entsprechend dem Kontostand

        // Flag, um rekursive Updates zu vermeiden
        final boolean[] isUpdating = {false};

        // Aktualisiere das Textfeld, wenn der Slider bewegt wird
        amountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                isUpdating[0] = true; // Setze das Flag auf true, um ein Update anzuzeigen
                amountField.setText(String.format("%.2f", newVal.doubleValue()));
                isUpdating[0] = false; // Setze das Flag nach dem Update zurück
            }
        });

        // Aktualisiere den Slider, wenn sich das Textfeld ändert
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                try {
                    double value = Double.parseDouble(newVal);
                    if (value >= 0 && value <= account.getBalance()) {
                        isUpdating[0] = true; // Setze das Flag auf true, um ein Update anzuzeigen
                        amountSlider.setValue(value);
                        isUpdating[0] = false; // Setze das Flag nach dem Update zurück
                    } else {
                        // Setze den alten Wert zurück, wenn der eingegebene Betrag ungültig ist
                        amountField.setText(oldVal);
                    }
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Zahlen und behalte den vorherigen Wert bei
                    amountField.setText(oldVal);
                }
            }
        });

        // Button für den Transfer des gesamten Betrags
        Button transferAllButton = new Button("Transfer All");
        transferAllButton.setOnAction(e -> amountSlider.setValue(account.getBalance())); // Setze den Slider auf den maximalen Kontostand

        Button transferButton = new Button("Execute Transfer");
        transferButton.setOnAction(e -> executeTransfer(amountField, targetAccountDropdown));

        transferForm.getChildren().addAll(
                new Label("Transfer Funds from " + account.getName()),
                availableBalanceLabel,
                new Label("Amount:"), amountField, amountSlider, transferAllButton,
                new Label("To Account:"), targetAccountDropdown,
                transferButton
        );

        root.setCenter(transferForm);
    }

    // Methode zum Erstellen eines Konto-Dropdowns für den eingeloggten Benutzer
    private ComboBox<Account> createAccountDropdown(String userId) {
        System.out.println("AccountDetailView.createAccountDropdown: Creating account dropdown for user - " + userId);

        ComboBox<Account> accountDropdown = new ComboBox<>();

        // Hole die Konten des Benutzers und filtere das aktuelle Konto heraus
        accountDropdown.setItems(transactionController.getAccountsForUser(userId)
                .filtered(acc -> !acc.getId().equals(account.getId())));
        accountDropdown.setPromptText("Choose Account");

        // Setze eine benutzerdefinierte Cell Factory, um die Kontonamen anzuzeigen
        accountDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        // Setze die Schaltflächenzelle, um den ausgewählten Kontonamen anzuzeigen
        accountDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        return accountDropdown;
    }

    // Methode zum Ausführen des Transfers zwischen Konten
    private void executeTransfer(TextField amountField, ComboBox<Account> targetAccountDropdown) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            Account targetAccount = targetAccountDropdown.getValue();

            if (account.getBalance() < amount || amount <= 0) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Insufficient funds or invalid amount.");
                System.err.println("AccountDetailView.executeTransfer: Insufficient funds or invalid amount.");
                return;
            }

            // Erstelle die Überweisungstransaktionen (eine Ausgabe für das Quellkonto, eine Einnahme für das Zielkonto)
            Transaction expense = new Transaction("Transfer to " + targetAccount.getName(), -amount, "expense", null, account, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");
            Transaction income = new Transaction("Transfer from " + account.getName(), amount, "income", null, targetAccount, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");

            // Speichere die Transaktionen
            transactionController.createTransaction(expense);
            transactionController.createTransaction(income);

            // Aktualisiere die Bilanzen beider Konten
            accountController.updateAccountBalance(account);         // Für das Quellkonto
            accountController.updateAccountBalance(targetAccount);   // Für das Zielkonto
            System.out.println("AccountDetailView.executeTransfer: Transfer completed successfully from " + account.getName() + " to " + targetAccount.getName());

            // Zeige die aktualisierten Kontodetails an
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount.");
            System.err.println("AccountDetailView.executeTransfer: Invalid amount entered.");
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to execute transfer: " + e.getMessage());
            System.err.println("AccountDetailView.executeTransfer: Error executing transfer - " + e.getMessage());
        }
    }

    // Methode zum Anzeigen des Formulars für Einnahmen oder Ausgaben
    private void showTransactionForm(String type) {
        System.out.println("AccountDetailView.showTransactionForm: Displaying transaction form for - " + type);

        VBox formView = new VBox(15);
        formView.setPadding(new Insets(20));
        formView.getStyleClass().add("form-view");

        Label formLabel = new Label("Add " + (type.equals("income") ? "Income" : "Expense") + " to " + account.getName());
        formLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        formView.getChildren().add(formLabel);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        // Zeit-Eingabefeld mit Validierung
        TextField timeField = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        // Dropdown für Kategorien
        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());

        // Dropdown für Wiederholungsoptionen
        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue("None");

        // Optionales Enddatum für wiederkehrende Transaktionen
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date (Optional)");
        endDatePicker.setVisible(false); // Anfangs ausgeblendet

        // Zeige das Enddatum an, wenn die Wiederholung nicht auf "None" gesetzt ist
        recurrenceDropdown.setOnAction(e -> {
            boolean isRecurring = !recurrenceDropdown.getValue().equals("None");
            endDatePicker.setVisible(isRecurring);
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveTransaction(type, descriptionField, amountField, datePicker, timeField, categoryDropdown, recurrenceDropdown, endDatePicker));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);

        formView.getChildren().addAll(descriptionField, amountField, new HBox(10, datePicker, timeField), new HBox(10, new Label("Recurrence:"), recurrenceDropdown, endDatePicker), new Label("Category:"), categoryDropdown, buttonBox);

        root.setCenter(formView);
    }

    // Methode zum Speichern einer Transaktion (Einnahme oder Ausgabe)
    private void saveTransaction(String type, TextField descriptionField, TextField amountField, DatePicker datePicker, TextField timeField, ComboBox<Category> categoryDropdown, ComboBox<String> recurrenceDropdown, DatePicker endDatePicker) {
        try {
            System.out.println("AccountDetailView.saveTransaction: Saving new transaction - " + type);

            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            LocalTime time;

            // Überprüfe und parse die Zeit
            try {
                time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid time format. Please enter time as HH:mm.");
                return;
            }

            Category category = categoryDropdown.getValue();
            String recurrence = recurrenceDropdown.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String status = date.isAfter(LocalDate.now()) ? "pending" : "completed";

            // Erstelle ein neues Transaktionsobjekt mit den eingegebenen Daten
            Transaction transaction = new Transaction(description, type.equals("income") ? amount : -amount,  // Negative Beträge für Ausgaben
                    type, null, account, category, Date.valueOf(date), Time.valueOf(time), status);

            // Wenn die Transaktion wiederkehrend ist, setze die entsprechenden Eigenschaften
            if (!recurrence.equals("None")) {
                transaction.markAsRecurring(recurrence.toLowerCase(), endDate != null ? Date.valueOf(endDate) : null);
            }

            // Speichere die Transaktion über den TransactionController
            transactionController.createTransaction(transaction);
            System.out.println("AccountDetailView.saveTransaction: Transaction saved successfully - " + transaction);

            // Aktualisiere die Kontobilanz
            updateAccountBalance();

            // Zeige die aktualisierten Kontodetails an
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.");
            System.err.println("AccountDetailView.saveTransaction: Error - Invalid amount entered.");
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to save transaction: " + e.getMessage());
            System.err.println("AccountDetailView.saveTransaction: Error saving transaction - " + e.getMessage());
        }
    }

    // Methode zum Bearbeiten der ausgewählten Transaktion
    private void editTransaction(Transaction transaction) {
        System.out.println("AccountDetailView.editTransaction: Editing transaction - " + transaction);

        VBox formView = new VBox(15);
        formView.setPadding(new Insets(20));
        formView.getStyleClass().add("form-view");

        Label formLabel = new Label("Edit Transaction for " + account.getName());
        formLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        formView.getChildren().add(formLabel);

        TextField descriptionField = new TextField(transaction.getDescription());
        descriptionField.setPromptText("Description");

        TextField amountField = new TextField(String.valueOf(Math.abs(transaction.getAmount())));
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(((java.sql.Date) transaction.getDate()).toLocalDate());

        // Zeit-Eingabefeld mit Validierung
        TextField timeField = new TextField(transaction.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        // ComboBox für Transaktionstyp (Einnahme/Ausgabe)
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("income", "expense");
        typeComboBox.setValue(transaction.getType().toLowerCase());

        // Dropdown für Wiederholung
        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue(transaction.isRecurring() ? capitalizeFirstLetter(transaction.getRecurrenceInterval()) : "None");

        DatePicker endDatePicker = new DatePicker();
        if (transaction.getEndDate() != null) {
            endDatePicker.setValue(((java.sql.Date) transaction.getEndDate()).toLocalDate());
        }

        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());
        categoryDropdown.setValue(transaction.getCategory());

        Button saveButton = new Button("Update Transaction");
        saveButton.setOnAction(e -> {
            try {
                // Validierung der Eingaben und Aktualisierung der Transaktion
                String description = descriptionField.getText();
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getValue();
                LocalTime time;

                try {
                    time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid time format. Please enter time as HH:mm.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }

                Category category = categoryDropdown.getValue();
                String type = typeComboBox.getValue();
                String recurrence = recurrenceDropdown.getValue();
                LocalDate endDate = endDatePicker.getValue();

                // Bestimme den Status basierend auf Datum und Zeit
                String status = date.isAfter(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isAfter(LocalTime.now())) ? "pending" : "completed";

                // Setze aktualisierte Werte in die Transaktion
                transaction.setDescription(description);
                transaction.setAmount(type.equals("income") ? amount : -amount);
                transaction.setDate(Date.valueOf(date));
                transaction.setTime(Time.valueOf(time));
                transaction.setCategory(category);
                transaction.setType(type);
                transaction.setStatus(status);

                // Aktualisiere Wiederholungseinstellungen
                if (!recurrence.equals("None")) {
                    transaction.markAsRecurring(recurrence.toLowerCase(), endDate != null ? Date.valueOf(endDate) : null);
                } else {
                    transaction.setRecurring(false);
                    transaction.setRecurrenceInterval("");
                    transaction.setEndDate(null);
                }

                // Aktualisiere die Transaktion über den TransactionController
                transactionController.updateTransaction(transaction);
                System.out.println("AccountDetailView.editTransaction: Transaction updated successfully - " + transaction);

                // Bilanz aktualisieren
                updateAccountBalance();

                // Zeige die aktualisierten Kontodetails
                showAccountDetailView(account);

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        formView.getChildren().addAll(descriptionField, amountField, new HBox(10, datePicker, timeField), new HBox(10, new Label("Type:"), typeComboBox), new Label("Category:"), categoryDropdown, new Label("Recurrence:"), recurrenceDropdown, new Label("End Date (Optional):"), endDatePicker, buttonBox);

        root.setCenter(formView);
    }

    // Methode zum Kapitalisieren des ersten Buchstabens eines Strings
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // Methode zum Erstellen eines Kategorie-Dropdowns für den eingeloggten Benutzer
    private ComboBox<Category> createCategoryDropdown(String userId) {
        System.out.println("AccountDetailView.createCategoryDropdown: Creating category dropdown for user - " + userId);

        ComboBox<Category> categoryDropdown = new ComboBox<>();

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

        return categoryDropdown;
    }

    // Methode zum Löschen der ausgewählten Transaktion
    private void deleteTransaction(Transaction transaction) {
        System.out.println("AccountDetailView.deleteTransaction: Deleting transaction - " + transaction);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        if (transaction.isRecurring()) {
            alert.setHeaderText("This is a recurring transaction.");
            alert.setContentText("Do you want to delete this occurrence or all future occurrences?");
            ButtonType deleteOne = new ButtonType("This Occurrence");
            ButtonType deleteAll = new ButtonType("All Occurrences");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(deleteOne, deleteAll, cancel);
            alert.showAndWait().ifPresent(response -> {
                if (response == deleteOne) {
                    // Lösche nur dieses einzelne Vorkommnis
                    transactionController.deleteTransaction(transaction);
                    System.out.println("AccountDetailView.deleteTransaction: Single occurrence deleted.");
                } else if (response == deleteAll) {
                    // Lösche die wiederkehrende Transaktion und alle ausstehenden Vorkommnisse
                    transactionController.deleteRecurringTransaction(transaction);
                    transactionController.deletePendingTransactionsByRecurringId(transaction.getId());
                    System.out.println("AccountDetailView.deleteTransaction: Recurring transaction and pending occurrences deleted.");
                }
            });
        } else {
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Do you want to permanently delete this transaction?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    transactionController.deleteTransaction(transaction);
                    System.out.println("AccountDetailView.deleteTransaction: Transaction deleted.");
                }
            });
        }


        // Inkludiert neue account balance?
        showAccountDetailView(account);
    }

    // Methode zur Erstellung runder Buttons mit Icons
    private Button createRoundIconButton(String iconPath) {
        ImageView iconView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        iconView.setFitHeight(30); // Größere Icons für bessere Sichtbarkeit
        iconView.setFitWidth(30);

        Button button = new Button("", iconView); // Leeren Text für nur Icon
        button.setStyle("-fx-background-color: #50fa7b; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;");

        // Ändere Button-Hintergrundfarbe beim Mouse-Over
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #ff79c6; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #50fa7b; " + "-fx-padding: 15px; " + "-fx-border-radius: 50%; " + "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + "-fx-max-width: 50px; " + "-fx-max-height: 50px;"));

        return button;
    }

    // Methode zur Aktualisierung der Kontobilanz
    private void updateAccountBalance() {
        System.out.println("AccountDetailView.updateAccountBalance: Updating balance for account - " + account.getName());

        // Rufe alle abgeschlossenen Transaktionen für das Konto ab
        List<Transaction> completedTransactions = transactionController.getCompletedTransactionsByAccount(account.getName());

        // Berechne die Bilanz basierend auf abgeschlossenen Transaktionen
        double newBalance = completedTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Setze die neue Bilanz - ACHTUNG: Überprüfe, ob du die Bilanz nicht auf den bestehenden Kontostand addierst
        account.setBalance(newBalance);
        accountController.updateAccount(account);

        System.out.println("AccountDetailView.updateAccountBalance: New balance calculated: " + newBalance);

        // Aktualisiere die Bilanzanzeige in der UI
        if (balanceLabel != null) {
            balanceLabel.setText("Balance: " + String.format("%.2f", newBalance));
        }

    }





    // Methode zum Aktualisieren des Transaktionsstatus (z.B. von 'pending' zu 'completed')
    private void updateTransactionStatuses() {
        System.out.println("AccountDetailView.updateTransactionStatuses: Checking pending transactions for account - " + account.getName());

        ObservableList<Transaction> pendingTransactions = transactionController.getTransactionsByAccount(account.getName()).filtered(t -> !((java.sql.Date) t.getDate()).toLocalDate().isAfter(LocalDate.now()) && t.getStatus().equalsIgnoreCase("pending"));

        for (Transaction transaction : pendingTransactions) {
            transaction.setStatus("completed");
            transactionController.updateTransaction(transaction);
            System.out.println("AccountDetailView.updateTransactionStatuses: Transaction marked as completed - " + transaction);
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
                    editTransaction(selectedTransaction); // Rufe die Bearbeitungsmethode für die ausgewählte Transaktion auf
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                Transaction selectedTransaction = row.getItem();
                if (selectedTransaction != null) {
                    deleteTransaction(selectedTransaction); // Rufe die Löschmethode für die ausgewählte Transaktion auf
                }
            });

            contextMenu.getItems().addAll(editItem, deleteItem);

            // Zeige das Kontextmenü nur für nicht-leere Zeilen an
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

            return row;
        });

        refreshTransactionTable();
        detailView.add(transactionsTable, 0, 3, 3, 1); // Positioniere die Tabelle im Grid, über alle Spalten hinweg
    }

    // Erstellung der Tabellenspalten für Methode SetupTransactionTable
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

        TableColumn<Transaction, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        // Füge alle Spalten zur Tabelle hinzu
        List<TableColumn<Transaction, String>> columns = new ArrayList<>();
        columns.add(descriptionColumn);
        columns.add(amountColumn);
        columns.add(dateColumn);
        columns.add(timeColumn);
        columns.add(categoryColumn);
        columns.add(accountColumn);
        columns.add(typeColumn);
        columns.add(statusColumn);
        return columns;
    }

    // Methode zur Aktualisierung der Transaktionstabelle, einschließlich regulärer und wiederkehrender Transaktionen
    private void refreshTransactionTable() {
        // Alle regulären Transaktionen für das Konto abrufen
        ObservableList<Transaction> regularTransactions = transactionController.getTransactionsByAccount(account.getName());

        // Nächste Vorkommen der wiederkehrenden Transaktionen abrufen
        ObservableList<Transaction> recurringTransactions = transactionController.getNextRecurringTransactionsByAccount(account.getId());

        // Kombiniere beide Listen, um alle Transaktionen anzuzeigen
        ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
        allTransactions.addAll(regularTransactions);
        allTransactions.addAll(recurringTransactions);

        // Filtere die "Initial Balance"-Transaktion heraus
        ObservableList<Transaction> filteredTransactions = allTransactions.filtered(t -> !t.getDescription().equals("Initial Balance"));

        // Sortiere die Transaktionen nach Datum, falls erforderlich
        filteredTransactions.sort(Comparator.comparing(Transaction::getDate));

        // Setze die gefilterten Transaktionen in der Tabelle
        transactionsTable.setItems(filteredTransactions);

        System.out.println("AccountDetailView.refreshTransactionTable: Transactions loaded for account - " + account.getName() + ". Filtered initial balance.");
    }



}

