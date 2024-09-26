package myProject.view.detail;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

        // Create a new GridPane to structure the layout neatly
        GridPane detailView = new GridPane();
        detailView.setPadding(new Insets(20));
        detailView.setHgap(20);
        detailView.setVgap(15);

        // Account Name Label
        Label nameLabel = new Label("Account: " + account.getName());
        nameLabel.getStyleClass().add("detail-label");
        GridPane.setConstraints(nameLabel, 0, 0, 2, 1); // Spanning two columns

        // Account Balance Label
        balanceLabel = new Label("Balance: " + ViewUtils.formatCurrency(account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");
        GridPane.setConstraints(balanceLabel, 0, 1, 2, 1); // Spanning two columns

        // Buttons for actions (Income, Expense, Transfer)
        Button incomeButton = new Button("Add Income");
        incomeButton.setOnAction(e -> showTransactionForm("income"));

        Button expenseButton = new Button("Add Expense");
        expenseButton.setOnAction(e -> showTransactionForm("expense"));

        Button transferButton = new Button("Transfer");
        transferButton.setOnAction(e -> showTransferForm());

        HBox buttonBox = new HBox(10, incomeButton, expenseButton, transferButton);
        buttonBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(buttonBox, 0, 2, 2, 1); // Spanning two columns

        // PieChart for spending by category
        PieChart spendingChart = new PieChart();
        spendingChart.setTitle("Spending by Category");

        // Fetch data from the TransactionController
        ObservableList<PieChart.Data> pieChartData = transactionController.getSpendingByCategoryForAccount(account);

        // Ensure pie chart data is properly set
        if (pieChartData == null || pieChartData.isEmpty()) {
            pieChartData = FXCollections.observableArrayList(new PieChart.Data("No Data Available", 1));
        }
        spendingChart.setData(pieChartData);
        spendingChart.setLabelsVisible(true);
        spendingChart.setLegendVisible(true);
        spendingChart.setLegendSide(Side.RIGHT); // Place the legend on the right

        // Add style for a more appealing legend
        spendingChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        GridPane.setConstraints(spendingChart, 2, 0, 1, 3); // Positioning chart, spanning 3 rows

        // Initialize and set up the transaction table
        setupTransactionTable(detailView);
        GridPane.setConstraints(transactionsTable, 0, 3, 3, 1); // Position table below, spanning 3 columns

        // Add all components to the GridPane in order
        detailView.getChildren().clear(); // Clear any existing children to avoid duplicates
        detailView.getChildren().addAll(nameLabel, balanceLabel, spendingChart, transactionsTable,buttonBox);

        // Set the GridPane as the center content of the root layout
        root.setCenter(detailView);
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
            endDatePicker.setVisible(!recurrenceDropdown.getValue().equals("None"));
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
                if (timeField.getText().isEmpty()) {
                    time = LocalTime.now(); // Set to current time if empty
                } else {
                    time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                }
            } catch (DateTimeParseException ex) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid time format. Please enter time as HH:mm.");
                return;
            }

            Category category = categoryDropdown.getValue();
            String recurrence = recurrenceDropdown.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String status = date.isAfter(LocalDate.now()) ? "pending" : "completed";

            // Create the transaction object
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
            updateAccountBalance(type.equals("income") ? amount : -amount);

            // Refresh account details view
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.");
        } catch (Exception e) {
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to save transaction: " + e.getMessage());
        }
    }

    // Method to update the account balance
    private void updateAccountBalance(double amount) {
        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        accountController.updateAccount(account); // Ensure that the account update is persisted
        balanceLabel.setText("Balance: " + ViewUtils.formatCurrency(account.getBalance()));
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


    // Method to refresh the transactions table
    private void refreshTransactionTable() {
        transactionsTable.setItems(transactionController.getTransactionsByAccount(account.getName()));
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

        TextField amountField = new TextField(String.valueOf(transaction.getAmount()));
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(((java.sql.Date) transaction.getDate()).toLocalDate());

        // Time input field with validation
        TextField timeField = new TextField(transaction.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setPromptText("Time (HH:mm)");

        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue(transaction.isRecurring() ? transaction.getRecurrenceInterval() : "None");

        DatePicker endDatePicker = new DatePicker();
        if (transaction.getEndDate() != null) {
            endDatePicker.setValue(((java.sql.Date) transaction.getEndDate()).toLocalDate());
        }

        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());
        categoryDropdown.setValue(transaction.getCategory());

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
                String recurrence = recurrenceDropdown.getValue();
                LocalDate endDate = endDatePicker.getValue();

                // Determine status based on date and time
                String status = date.isAfter(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isAfter(LocalTime.now())) ? "pending" : "completed";

                // Set updated values in the transaction
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setDate(Date.valueOf(date));
                transaction.setTime(Time.valueOf(time));
                transaction.setCategory(category);
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
                datePicker,
                new Label("Time:"), timeField,
                new Label("Category:"),
                categoryDropdown,
                new Label("Recurrence:"),
                recurrenceDropdown,
                new Label("End Date (Optional):"),
                endDatePicker,
                buttonBox
        );

        root.setCenter(formView);
    }


    // Method to delete the selected transaction
    private void deleteTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to permanently delete this transaction?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            transactionController.deleteTransaction(transaction); // Delete transaction
            updateAccountBalance(transaction.getType().equals("income") ? -transaction.getAmount() : transaction.getAmount()); // Adjust balance
            showAccountDetailView(account); // Refresh view
        }
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

            // Validation: Check if the account has enough balance
            if (account.getBalance() < amount || amount <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Insufficient funds or invalid amount.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Create transactions for the transfer
            Transaction expense = new Transaction("Transfer to " + targetAccount.getName(), -amount, "expense", null, account, null, new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");
            Transaction income = new Transaction("Transfer from " + account.getName(), amount, "income", null, targetAccount, null, new java.sql.Date(System.currentTimeMillis()), Time.valueOf(LocalTime.now()), "completed");

            // Save both transactions using TransactionController
            transactionController.createTransaction(expense);
            transactionController.createTransaction(income);

            // Update balances
            updateAccountBalance(-amount);
            targetAccount.setBalance(targetAccount.getBalance() + amount);
            accountController.updateAccount(targetAccount);

            // Show updated details
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid amount. Please enter a valid number.", ButtonType.OK);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to execute transfer: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
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
