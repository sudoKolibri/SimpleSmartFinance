package myProject.view.detail;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.AccountController;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.view.util.ViewUtils;

import java.time.LocalDate;

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

    // Method to show the account details
    public void showAccountDetailView(Account account) {
        this.account = account;

        VBox detailView = new VBox(20);
        detailView.setPadding(new Insets(20));
        detailView.getStyleClass().add("detail-view");

        // Account Name
        Label nameLabel = new Label("Account: " + account.getName());
        nameLabel.getStyleClass().add("detail-label");
        detailView.getChildren().add(nameLabel);

        // Account Balance
        balanceLabel = new Label("Balance: " + ViewUtils.formatCurrency(account.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");
        detailView.getChildren().add(balanceLabel);

        // Buttons for Income, Expense, and Transfer
        Button incomeButton = new Button("Add Income");
        incomeButton.setOnAction(e -> showTransactionForm("income"));

        Button expenseButton = new Button("Add Expense");
        expenseButton.setOnAction(e -> showTransactionForm("expense"));

        Button transferButton = new Button("Transfer");
        transferButton.setOnAction(e -> showTransferForm());

        HBox buttonBox = new HBox(10, incomeButton, expenseButton, transferButton);
        detailView.getChildren().add(buttonBox);

        // Set up and display transactions table
        setupTransactionTable(detailView);

        root.setCenter(detailView);
    }

    // Method to show the transaction form for income or expense
    private void showTransactionForm(String type) {
        VBox formView = new VBox(15);
        formView.setPadding(new Insets(20));
        formView.getStyleClass().add("form-view");

        Label formLabel = new Label("Add " + (type.equals("income") ? "Income to " + account.getName() + " with current Balance: " + ViewUtils.formatCurrency(account.getBalance()) + " $" : "Expense to " + account.getName() + " with current Balance: " + ViewUtils.formatCurrency(account.getBalance()) + " $" ));
        formLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        formView.getChildren().add(formLabel);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        // Category dropdown
        ComboBox<Category> categoryDropdown = createCategoryDropdown(account.getUserId());

        // Recurrence options
        ComboBox<String> recurrenceDropdown = new ComboBox<>();
        recurrenceDropdown.getItems().addAll("None", "Daily", "Weekly", "Monthly");
        recurrenceDropdown.setValue("None");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date (Optional)");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveTransaction(type, descriptionField, amountField, datePicker, categoryDropdown, recurrenceDropdown, endDatePicker));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> showAccountDetailView(account));

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        formView.getChildren().addAll(descriptionField, amountField, datePicker, new Label("Category:"), categoryDropdown, new Label("Recurrence:"), recurrenceDropdown, endDatePicker, buttonBox);

        root.setCenter(formView);
    }

    // Method to save the transaction and update account balance
    private void saveTransaction(String type, TextField descriptionField, TextField amountField, DatePicker datePicker, ComboBox<Category> categoryDropdown, ComboBox<String> recurrenceDropdown, DatePicker endDatePicker) {
        try {
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            Category category = categoryDropdown.getValue();
            String recurrence = recurrenceDropdown.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Create transaction based on type
            Transaction transaction = new Transaction(
                    description,
                    type.equals("income") ? amount : -amount,
                    type,
                    null, // Assuming current user is set correctly elsewhere
                    account,
                    category,
                    java.sql.Date.valueOf(date)
            );

            // Set recurring properties if applicable
            if (!recurrence.equals("None")) {
                transaction.markAsRecurring(recurrence.toLowerCase(), endDate != null ? java.sql.Date.valueOf(endDate) : null);
            }

            // Save transaction and update balance
            transactionController.createTransaction(transaction);
            updateAccountBalance(type.equals("income") ? amount : -amount);

            // Show updated details
            showAccountDetailView(account);

        } catch (NumberFormatException e) {
            System.err.println("Invalid amount. Please enter a valid number.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to save transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to update the account balance
    private void updateAccountBalance(double amount) {
        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        accountController.updateAccount(account); // Ensure that the account update is persisted
        balanceLabel.setText("Balance: " + ViewUtils.formatCurrency(account.getBalance()));
    }

    // Set up the transactions table
    private void setupTransactionTable(VBox detailView) {
        transactionsTable = new TableView<>();

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asString());

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty().asString());

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

        transactionsTable.getColumns().addAll(descriptionColumn, amountColumn, dateColumn, categoryColumn, accountColumn);
        refreshTransactionTable();

        detailView.getChildren().add(transactionsTable);
    }

    private void refreshTransactionTable() {
        transactionsTable.setItems(transactionController.getTransactionsByAccount(account.getName()));
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
            Transaction expense = new Transaction("Transfer to " + targetAccount.getName(), -amount, "expense", null, account, null, new java.sql.Date(System.currentTimeMillis()));
            Transaction income = new Transaction("Transfer from " + account.getName(), amount, "income", null, targetAccount, null, new java.sql.Date(System.currentTimeMillis()));

            // Save both transactions
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
                // Fetch accounts for user
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
