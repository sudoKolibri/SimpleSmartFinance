package myProject.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;

import java.time.LocalDate;
import java.sql.Date;

public class GlobalTransactionsView {

    private final TransactionController transactionController;
    private TableView<Transaction> transactionsTable;
    private final String loggedInUserId;
    private VBox formLayout;
    private TextField descriptionField, amountField;
    private DatePicker datePicker;
    private ComboBox<Category> categoryDropdown;
    private ComboBox<Account> accountDropdown;
    private Button saveButton;
    private Transaction selectedTransaction;  // To hold the transaction being edited

    public GlobalTransactionsView(TransactionController transactionController, String loggedInUserId) {
        this.transactionController = transactionController;
        this.loggedInUserId = loggedInUserId;
    }

    // Load the GlobalTransactionsView into the center of the BorderPane
    public void loadIntoPane(BorderPane root) {
        VBox vbox = new VBox(20);  // Adjust spacing
        vbox.setPadding(new Insets(20));

        // Add Transaction Form
        formLayout = createAddTransactionForm();

        // Transactions Table
        transactionsTable = new TableView<>();
        setupTransactionTable();  // Setup table columns

        // Populate the table with existing transactions
        refreshTransactionsTable();

        // Layout setup
        vbox.getChildren().addAll(formLayout, transactionsTable);
        root.setCenter(vbox);  // Load everything into the dynamic content area
    }

    // Create the Add/Edit Transaction Form (embedded in the view)
    private VBox createAddTransactionForm() {
        formLayout = new VBox(10);
        formLayout.setPadding(new Insets(10));
        formLayout.getStyleClass().add("root");  // Apply Dracula theme background

        // Form fields
        descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.getStyleClass().add("text-field");

        amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.getStyleClass().add("text-field");

        datePicker = new DatePicker(LocalDate.now());

        // Category dropdown
        categoryDropdown = createCategoryDropdown(loggedInUserId);

        // Account dropdown
        accountDropdown = createAccountDropdown(loggedInUserId);

        // Save button
        saveButton = new Button("Save Transaction");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> {
            if (selectedTransaction != null) {
                updateTransaction(descriptionField.getText(),
                        Double.parseDouble(amountField.getText()),
                        datePicker.getValue(),
                        categoryDropdown.getValue(),
                        accountDropdown.getValue());
            } else {
                createTransaction(descriptionField.getText(),
                        Double.parseDouble(amountField.getText()),
                        datePicker.getValue(),
                        categoryDropdown.getValue(),
                        accountDropdown.getValue());
            }
        });

        // Add fields and button to form layout
        formLayout.getChildren().addAll(
                new Label("Add New Transaction:"),
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField,
                new Label("Date:"), datePicker,
                new Label("Category:"), categoryDropdown,
                new Label("Account:"), accountDropdown,
                saveButton
        );

        return formLayout;
    }

    // Set up the transactions table with right-click context menu
    private void setupTransactionTable() {
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

        // Add right-click-context menu for editing or deleting a transaction
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem editMenuItem = new MenuItem("Edit Transaction");
            editMenuItem.setOnAction(e -> editTransaction(row.getItem()));

            MenuItem deleteMenuItem = new MenuItem("Delete Transaction");
            deleteMenuItem.setOnAction(e -> deleteTransaction(row.getItem()));

            contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);

            row.setContextMenu(contextMenu);
            return row;
        });
    }

    // Method to create and add a new transaction
    private void createTransaction(String description, double amount, LocalDate date, Category category, Account account) {
        // Log to check if Category and Account are being set
        System.out.println("Creating transaction with category: " + (category != null ? category.getName() : "null"));
        System.out.println("Creating transaction with account: " + (account != null ? account.getName() : "null"));

        // Convert LocalDate to java.sql.Date before saving
        Transaction newTransaction = new Transaction(description, amount, "expense", null, account, category, Date.valueOf(date));
        transactionController.createTransaction(newTransaction);

        refreshTransactionsTable();  // Refresh the table after adding a new transaction
        clearForm();  // Reset the form after saving
    }


    private void editTransaction(Transaction transaction) {
        // Populate the form with transaction data for editing
        selectedTransaction = transaction;
        descriptionField.setText(transaction.getDescription());
        amountField.setText(String.valueOf(transaction.getAmount()));

        // Safely handle date conversion for the DatePicker
        if (transaction.getDate() != null) {
            // Assuming transaction.getDate() is java.sql.Date
            datePicker.setValue(((java.sql.Date) transaction.getDate()).toLocalDate());
        }

        categoryDropdown.setValue(transaction.getCategory());
        accountDropdown.setValue(transaction.getAccount());

        // Change the button text to "Update Transaction"
        saveButton.setText("Update Transaction");
    }



    // Method to update an existing transaction
    private void updateTransaction(String description, double amount, LocalDate date, Category category, Account account) {
        selectedTransaction.setDescription(description);
        selectedTransaction.setAmount(amount);

        // Convert LocalDate back to java.sql.Date
        selectedTransaction.setDate(Date.valueOf(date));

        selectedTransaction.setCategory(category);
        selectedTransaction.setAccount(account);

        transactionController.updateTransaction(selectedTransaction);  // Persist changes using update
        refreshTransactionsTable();  // Refresh the table
        clearForm();  // Reset the form after updating
    }

    // Method to delete an existing transaction
    private void deleteTransaction(Transaction transaction) {
        System.out.println("Deleting transaction with ID: " + transaction.getId());  // Log the transaction ID before deletion

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to permanently delete this transaction?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            transactionController.deleteTransaction(transaction);
            refreshTransactionsTable();  // Refresh the table after deletion
            clearForm();
        }
    }


    // Refresh the transactions table after creating/updating/deleting a transaction
    private void refreshTransactionsTable() {
        transactionsTable.getItems().clear();  // Clear current items
        ObservableList<Transaction> transactions = transactionController.getAllTransactions();
        transactionsTable.setItems(transactions);  // Re-fetch and populate the table
    }

    // Clear the form after adding/updating a transaction
    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        categoryDropdown.getSelectionModel().clearSelection();
        accountDropdown.getSelectionModel().clearSelection();
        selectedTransaction = null;
        saveButton.setText("Save Transaction");  // Reset button text
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
                setText(empty ? "" : item.getName());  // Display the name of the category
            }
        });

        // Set custom button cell to display selected category name
        categoryDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());  // Display the name of the selected category
            }
        });

        return categoryDropdown;
    }


    // Create Account Dropdown for the logged-in user
    private ComboBox<Account> createAccountDropdown(String userId) {
        ComboBox<Account> accountDropdown = new ComboBox<>();
        accountDropdown.setItems(transactionController.getAccountsForUser(userId));  // Fetch accounts for user
        accountDropdown.setPromptText("Choose Account");

        // Set custom cell factory to display the account name
        accountDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());  // Display the name of the account
            }
        });

        // Set custom button cell to display selected account name
        accountDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());  // Display the name of the selected account
            }
        });

        return accountDropdown;
    }

}
