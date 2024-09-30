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
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class AccountDetailView {
    private final AccountController accountController;
    private final TransactionController transactionController;
    private final BorderPane root;
    private Account account;
    private Label balanceLabel;
    private TableView<Transaction> transactionsTable;

    // Constructor to initialize controllers and layout
    public AccountDetailView(AccountController accountController, TransactionController transactionController, BorderPane root) {
        this.accountController = accountController;
        this.transactionController = transactionController;
        if (root == null) {
            throw new IllegalArgumentException("Root layout cannot be null.");
        }
        this.root = root;
    }

    public void showAccountDetailView(Account account) {
        this.account = account;

        // Initialize balanceLabel before calling updateAccountBalance
        balanceLabel = new Label("Balance: " + String.format("%.2f", account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8be9fd;");

        // Update transaction statuses before displaying the account details
        updateTransactionStatuses();

        // Update the account balance after initializing the label
        updateAccountBalance();

        // Top Section: Account Name and Balance
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label("Account: " + account.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        topSection.getChildren().addAll(nameLabel, balanceLabel);

        // Main Section: TableView for Transactions centered horizontally
        setupTransactionTable(new GridPane());

        // Centering the table
        StackPane tableContainer = new StackPane(transactionsTable);
        tableContainer.setAlignment(Pos.CENTER);

        // Bottom Section: Icon-Based Action Buttons centered horizontally
        HBox buttonBox = new HBox(20); // Spacing between the buttons
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setAlignment(Pos.CENTER);

        // Buttons with round icons
        Button incomeButton = createRoundIconButton("/icons/icons8-add-dollar-50.png");
        incomeButton.setOnAction(e -> showTransactionForm("income"));

        Button expenseButton = createRoundIconButton("/icons/icons8-delete-dollar-50.png");
        expenseButton.setOnAction(e -> showTransactionForm("expense"));

        Button transferButton = createRoundIconButton("/icons/icons8-exchange-48.png");
        transferButton.setOnAction(e -> showTransferForm());

        buttonBox.getChildren().addAll(incomeButton, expenseButton, transferButton);

        // Main layout to stack sections vertically
        VBox mainLayout = new VBox(20); // Increase spacing to avoid crowding
        mainLayout.getChildren().addAll(topSection, tableContainer, buttonBox);

        // Set the layout to the root center
        root.setCenter(mainLayout);
    }



    // Helper method to create round buttons with larger icons
    private Button createRoundIconButton(String iconPath) {
        ImageView iconView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        iconView.setFitHeight(30); // Increase icon size for better visibility
        iconView.setFitWidth(30);

        Button button = new Button("", iconView); // Set empty text to use only the icon
        button.setStyle(
                "-fx-background-color: #50fa7b; " +
                        "-fx-padding: 15px; " + // Padding to create a round appearance around the icon
                        "-fx-border-radius: 50%; " + // Fully rounded shape
                        "-fx-min-width: 50px; " + // Set minimum width and height for consistency
                        "-fx-min-height: 50px; " +
                        "-fx-max-width: 50px; " +
                        "-fx-max-height: 50px;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #ff79c6; " +
                        "-fx-padding: 15px; " +
                        "-fx-border-radius: 50%; " +
                        "-fx-min-width: 50px; " +
                        "-fx-min-height: 50px; " +
                        "-fx-max-width: 50px; " +
                        "-fx-max-height: 50px;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #50fa7b; " +
                        "-fx-padding: 15px; " +
                        "-fx-border-radius: 50%; " +
                        "-fx-min-width: 50px; " +
                        "-fx-min-height: 50px; " +
                        "-fx-max-width: 50px; " +
                        "-fx-max-height: 50px;"
        ));

        return button;
    }

    // Method to show the transaction form for income or expense
    private void showTransactionForm(String type) {
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

        // Time input field with validation
        TextField timeField = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        // Category dropdown
        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());

        // Recurrence options
        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue("None");

        // Optional End Date Picker for Recurring Transactions
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date (Optional)");
        endDatePicker.setVisible(false); // Initially hidden

        // Show end date picker when recurrence is not "None"
        recurrenceDropdown.setOnAction(e -> {
            boolean isRecurring = !recurrenceDropdown.getValue().equals("None");
            endDatePicker.setVisible(isRecurring);
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveTransaction(type, descriptionField, amountField, datePicker, timeField, categoryDropdown, recurrenceDropdown, endDatePicker));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);

        formView.getChildren().addAll(
                descriptionField,
                amountField,
                new HBox(10, datePicker, timeField),
                new HBox(10, new Label("Recurrence:"), recurrenceDropdown, endDatePicker),
                new Label("Category:"), categoryDropdown,
                buttonBox
        );

        root.setCenter(formView);
    }

    // Save a transaction
    private void saveTransaction(String type, TextField descriptionField, TextField amountField, DatePicker datePicker, TextField timeField, ComboBox<Category> categoryDropdown, ComboBox<String> recurrenceDropdown, DatePicker endDatePicker) {
        try {
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            LocalTime time;

            // Ensure the time field is not empty, and set a default time if it is
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

            // Create the transaction object ensuring the time is never null
            Transaction transaction = new Transaction(
                    description,
                    type.equals("income") ? amount : -amount,
                    type,
                    null,
                    account,
                    category,
                    Date.valueOf(date),
                    Time.valueOf(time), // Ensure time is set correctly
                    status
            );

            // Handle recurring properties if applicable
            if (!recurrence.equals("None")) {
                transaction.markAsRecurring(recurrence.toLowerCase(), endDate != null ? Date.valueOf(endDate) : null);
            }

            // Save the transaction using TransactionController
            transactionController.createTransaction(transaction);

            // Update the account balance
            updateAccountBalance();

            // Refresh account details view
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.");
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to save transaction: " + e.getMessage());
        }
    }

    // Method to update the balance of any account
    private void updateAccountBalance() {
        // Call the controller to update the balance and reflect it in the UI
        double newBalance = accountController.calculateUpdatedBalanceForCompletedTransactions(account);

        // Update the balance label in the UI
        if (balanceLabel != null) {
            balanceLabel.setText("Balance: " + String.format("%.2f", newBalance));
        }
    }










    // Method to update transaction statuses (e.g., from 'pending' to 'completed')
    private void updateTransactionStatuses() {
        ObservableList<Transaction> pendingTransactions = transactionController.getTransactionsByAccount(account.getName())
                .filtered(t -> !((java.sql.Date) t.getDate()).toLocalDate().isAfter(LocalDate.now())
                        && t.getStatus().equalsIgnoreCase("pending"));

        for (Transaction transaction : pendingTransactions) {
            transaction.setStatus("completed");
            transactionController.updateTransaction(transaction);
        }
    }




    // Set up the transactions table with a right-click context menu
    private void setupTransactionTable(GridPane detailView) {
        transactionsTable = new TableView<>();

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

        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle(""); // Clear any style when the cell is empty
                } else {
                    setText(type);
                    if (type.equalsIgnoreCase("income")) {
                        setStyle("-fx-text-fill: green;"); // Green text for income
                    } else if (type.equalsIgnoreCase("expense")) {
                        setStyle("-fx-text-fill: red;"); // Red text for expense
                    } else {
                        setStyle("-fx-text-fill: black;"); // Default color for other types
                    }
                }
            }
        });

        // Add all columns to the table
        transactionsTable.getColumns().addAll(descriptionColumn, amountColumn, dateColumn, timeColumn, categoryColumn, accountColumn, typeColumn, statusColumn);

        // Add a context menu to each row in the table
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(event -> {
                Transaction selectedTransaction = row.getItem();
                if (selectedTransaction != null) {
                    editTransaction(selectedTransaction); // Call the edit method with the selected transaction
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                Transaction selectedTransaction = row.getItem();
                if (selectedTransaction != null) {
                    deleteTransaction(selectedTransaction); // Call the delete method with the selected transaction
                }
            });

            contextMenu.getItems().addAll(editItem, deleteItem);

            // Only show the context menu on non-empty rows
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });

        refreshTransactionTable();
        detailView.add(transactionsTable, 0, 3, 3, 1); // Position table in the grid, spanning all columns
    }

    // Method to refresh the transactions table, including regular and recurring transactions
    private void refreshTransactionTable() {
        // Fetch all regular transactions for the account
        ObservableList<Transaction> regularTransactions = transactionController.getTransactionsByAccount(account.getName());

        // Fetch all next occurrences of recurring transactions for the account
        ObservableList<Transaction> recurringTransactions = transactionController.getNextRecurringTransactionsByAccount(account.getId());

        // Combine both lists to ensure all transactions are displayed
        ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
        allTransactions.addAll(regularTransactions);
        allTransactions.addAll(recurringTransactions);

        // Optional: Sort transactions by date if needed
        allTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        // Set all transactions to be displayed in the table without filtering by status
        transactionsTable.setItems(allTransactions);
    }

    // Method to edit the selected transaction
    private void editTransaction(Transaction transaction) {
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

        // Time input field with validation
        TextField timeField = new TextField(transaction.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        // ComboBox for transaction type (Income/Expense)
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("income", "expense");
        typeComboBox.setValue(transaction.getType().toLowerCase()); // Set current type as default

        // Recurrence options
        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue(transaction.isRecurring() ? capitalizeFirstLetter(transaction.getRecurrenceInterval()) : "None");

        DatePicker endDatePicker = new DatePicker();
        if (transaction.getEndDate() != null) {
            endDatePicker.setValue(((java.sql.Date) transaction.getEndDate()).toLocalDate());
        }

        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());
        categoryDropdown.setValue(transaction.getCategory()); // Set default value to current category

        Button saveButton = new Button("Update Transaction");
        saveButton.setOnAction(e -> {
            try {
                // Validate inputs and update the transaction
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
                String type = typeComboBox.getValue(); // Get selected type from the ComboBox
                String recurrence = recurrenceDropdown.getValue();
                LocalDate endDate = endDatePicker.getValue();

                // Determine status based on date and time
                String status = date.isAfter(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isAfter(LocalTime.now())) ? "pending" : "completed";

                // Set updated values in the transaction
                transaction.setDescription(description);
                transaction.setAmount(type.equals("income") ? amount : -amount); // Adjust amount based on type
                transaction.setDate(Date.valueOf(date));
                transaction.setTime(Time.valueOf(time));
                transaction.setCategory(category);
                transaction.setType(type); // Update the transaction type
                transaction.setStatus(status); // Automatically set status based on date and time

                // Update recurrence settings
                if (!recurrence.equals("None")) {
                    transaction.markAsRecurring(recurrence.toLowerCase(), endDate != null ? Date.valueOf(endDate) : null);
                } else {
                    transaction.setRecurring(false);
                    transaction.setRecurrenceInterval("");
                    transaction.setEndDate(null);
                }

                // Update the transaction using TransactionController
                transactionController.updateTransaction(transaction);

                // Update the account balance
                updateAccountBalance();

                showAccountDetailView(account); // Refresh view

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        formView.getChildren().addAll(
                descriptionField,
                amountField,
                new HBox(10, datePicker, timeField),
                new HBox(10, new Label("Type:"), typeComboBox), // Add type ComboBox
                new Label("Category:"), categoryDropdown,
                new Label("Recurrence:"), recurrenceDropdown,
                new Label("End Date (Optional):"), endDatePicker,
                buttonBox
        );

        root.setCenter(formView);
    }

    // Helper method to capitalize the first letter
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // Method to delete the selected transaction
    private void deleteTransaction(Transaction transaction) {
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
                    // Delete only this occurrence
                    transactionController.deleteTransaction(transaction);
                } else if (response == deleteAll) {
                    // Delete the recurring transaction definition
                    transactionController.deleteRecurringTransaction(transaction);
                    // Also delete any pending occurrences of this transaction
                    transactionController.deletePendingTransactionsByRecurringId(transaction.getId());
                }
            });
        } else {
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Do you want to permanently delete this transaction?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    transactionController.deleteTransaction(transaction);
                }
            });
        }
        // Update the account balance
        updateAccountBalance();
        showAccountDetailView(account);
    }

    // Create Category Dropdown for the logged-in user
    private ComboBox<Category> createCategoryDropdown(String userId) {
        ComboBox<Category> categoryDropdown = new ComboBox<>();
        categoryDropdown.setItems(transactionController.getAllCategoriesForUser(userId));  // Fetch categories for user
        categoryDropdown.setPromptText("Choose Category");

        // Set custom cell factory to display the category name
        categoryDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        // Set custom button cell to display selected category name
        categoryDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        return categoryDropdown;
    }

    // Show the transfer form with validation and a slider for transfer amounts
    private void showTransferForm() {
        VBox transferForm = new VBox(15);
        transferForm.setPadding(new Insets(20));

        Label availableBalanceLabel = new Label("Available Balance: " + ViewUtils.formatCurrency(account.getBalance()));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<Account> targetAccountDropdown = createAccountDropdown(account.getUserId());
        targetAccountDropdown.setPromptText("Select Target Account");

        // Slider for transfer amount
        Slider amountSlider = new Slider(0, account.getBalance(), 0); // Min: 0, Max: current balance, Initial: 0
        amountSlider.setShowTickLabels(true);
        amountSlider.setShowTickMarks(true);
        amountSlider.setMajorTickUnit(account.getBalance() / 4); // Adjust tick units

        // Flag to avoid recursive updates
        final boolean[] isUpdating = {false};

        // Update the TextField when the Slider is moved
        amountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                isUpdating[0] = true; // Set flag to true to indicate update is happening
                amountField.setText(String.format("%.2f", newVal.doubleValue()));
                isUpdating[0] = false; // Reset flag after update
            }
        });

        // Update the Slider when the TextField changes
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdating[0]) {
                try {
                    double value = Double.parseDouble(newVal);
                    if (value >= 0 && value <= account.getBalance()) {
                        isUpdating[0] = true; // Set flag to true to indicate update is happening
                        amountSlider.setValue(value);
                        isUpdating[0] = false; // Reset flag after update
                    } else {
                        // Revert to old value if input is out of bounds
                        amountField.setText(oldVal);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers and keep the previous value
                    amountField.setText(oldVal);
                }
            }
        });

        // Transfer all button
        Button transferAllButton = new Button("Transfer All");
        transferAllButton.setOnAction(e -> amountSlider.setValue(account.getBalance())); // Set slider to maximum balance

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

    private void executeTransfer(TextField amountField, ComboBox<Account> targetAccountDropdown) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            Account targetAccount = targetAccountDropdown.getValue();

            if (account.getBalance() < amount || amount <= 0) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Insufficient funds or invalid amount.");
                return;
            }

            // Create the transfer transactions
            Transaction expense = new Transaction("Transfer to " + targetAccount.getName(), -amount, "expense", null, account, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");
            Transaction income = new Transaction("Transfer from " + account.getName(), amount, "income", null, targetAccount, null,
                    new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");

            // Save transactions
            transactionController.createTransaction(expense);
            transactionController.createTransaction(income);

            // Correctly update both account balances
            accountController.updateAccountBalance(account);         // For the source account
            accountController.updateAccountBalance(targetAccount);   // For the target account

            // Refresh the view
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount.");
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to execute transfer: " + e.getMessage());
        }
    }





    // Create Account Dropdown for the logged-in user
    private ComboBox<Account> createAccountDropdown(String userId) {
        ComboBox<Account> accountDropdown = new ComboBox<>();

        // Fetch accounts for user and filter out the current account
        accountDropdown.setItems(transactionController.getAccountsForUser(userId)
                .filtered(acc -> !acc.getId().equals(account.getId())));
        accountDropdown.setPromptText("Choose Account");

        // Set custom cell factory to display the account name
        accountDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        // Set custom button cell to display selected account name
        accountDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        return accountDropdown;
    }
}
