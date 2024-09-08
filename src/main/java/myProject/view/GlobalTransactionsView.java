package myProject.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.controller.TransactionController;
import myProject.controller.CategoryController;
import myProject.controller.AccountController;
import myProject.controller.UserController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.model.User;

import java.time.LocalDate;
import java.sql.Date;

public class GlobalTransactionsView {

    private final CategoryController categoryController;
    private final AccountController accountController;
    private final UserController userController;
    private final TransactionController transactionController;
    private TableView<Transaction> transactionsTable;
    private final String loggedInUserId;
    private VBox formLayout;
    private TextField descriptionField, amountField;
    private DatePicker datePicker;
    private ComboBox<Category> categoryDropdown;
    private ComboBox<Account> accountDropdown;
    private CheckBox recurringCheckBox;
    private ComboBox<String> recurrenceIntervalDropdown;
    private ComboBox<String> transactionTypeDropdown;
    private Button saveButton;
    private Transaction selectedTransaction;

    public GlobalTransactionsView(TransactionController transactionController,
                                  CategoryController categoryController,
                                  AccountController accountController,
                                  UserController userController,
                                  String loggedInUserId) {
        this.transactionController = transactionController;
        this.categoryController = categoryController;
        this.accountController = accountController;
        this.userController = userController;
        this.loggedInUserId = loggedInUserId;
    }

    public void loadIntoPane(BorderPane root) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));

        formLayout = createAddTransactionForm();
        transactionsTable = new TableView<>();
        setupTransactionTable();

        refreshTransactionsTable();

        vbox.getChildren().addAll(formLayout, transactionsTable);
        root.setCenter(vbox);
    }

    private void setupTransactionTable() {
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asString());

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty().asString());

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data -> {
            Category category = data.getValue().getCategory();
            return category != null ? category.nameProperty() : new SimpleStringProperty("No Category");
        });

        TableColumn<Transaction, String> accountColumn = new TableColumn<>("Account");
        accountColumn.setCellValueFactory(data -> {
            Account account = data.getValue().getAccount();
            return account != null ? account.nameProperty() : new SimpleStringProperty("No Account");
        });

        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    setStyle("-fx-text-fill: " + (type.equalsIgnoreCase("income") ? "green" : "red") + ";");
                }
            }
        });

        TableColumn<Transaction, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        transactionsTable.getColumns().addAll(descriptionColumn, amountColumn, dateColumn, categoryColumn, accountColumn, typeColumn, statusColumn);

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


    private VBox createAddTransactionForm() {
        formLayout = new VBox(10);
        formLayout.setPadding(new Insets(10));

        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        amountField = new TextField();
        amountField.setPromptText("Amount");

        datePicker = new DatePicker(LocalDate.now());

        categoryDropdown = createCategoryDropdown(loggedInUserId);
        accountDropdown = createAccountDropdown(loggedInUserId);

        transactionTypeDropdown = new ComboBox<>();
        transactionTypeDropdown.getItems().addAll("Income", "Expense");
        transactionTypeDropdown.setPromptText("Transaction Type");

        recurringCheckBox = new CheckBox("Recurring");
        recurrenceIntervalDropdown = new ComboBox<>();
        recurrenceIntervalDropdown.setDisable(true);

        saveButton = new Button("Save Transaction");
        saveButton.setOnAction(e -> {
            if (validateForm()) {
                if (selectedTransaction != null) {
                    updateTransaction(descriptionField.getText(),
                            Double.parseDouble(amountField.getText()),
                            datePicker.getValue(),
                            categoryDropdown.getValue(),
                            accountDropdown.getValue(),
                            transactionTypeDropdown.getValue());
                } else {
                    createTransaction(descriptionField.getText(),
                            Double.parseDouble(amountField.getText()),
                            datePicker.getValue(),
                            categoryDropdown.getValue(),
                            accountDropdown.getValue(),
                            transactionTypeDropdown.getValue());
                }
            }
        });

        formLayout.getChildren().addAll(
                new Label("Add New Transaction:"),
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField,
                new Label("Date:"), datePicker,
                new Label("Category:"), categoryDropdown,
                new Label("Account:"), accountDropdown,
                new Label("Transaction Type:"), transactionTypeDropdown,
                recurringCheckBox,
                new Label("Recurrence Interval:"), recurrenceIntervalDropdown,
                saveButton
        );

        return formLayout;
    }

    private void createTransaction(String description, double amount, LocalDate date, Category category, Account account, String type) {
        User loggedInUser = userController.getLoggedInUser();

        if (recurringCheckBox.isSelected()) {
            String recurrenceInterval = recurrenceIntervalDropdown.getValue();
            Transaction recurringTransaction = new Transaction(description, amount, type.toLowerCase(), loggedInUser, account, category, Date.valueOf(date), recurrenceInterval);
            transactionController.createRecurringTransaction(recurringTransaction);
        } else {
            Transaction newTransaction = new Transaction(description, amount, type.toLowerCase(), loggedInUser, account, category, Date.valueOf(date));
            transactionController.createTransaction(newTransaction);
        }

        refreshTransactionsTable();
        clearForm();
    }

    private boolean validateForm() {
        if (descriptionField.getText().isEmpty()) {
            showAlert("Validation Error", "Description cannot be empty.");
            return false;
        }

        if (amountField.getText().isEmpty() || !isNumeric(amountField.getText())) {
            showAlert("Validation Error", "Please enter a valid amount.");
            return false;
        }

        if (categoryDropdown.getValue() == null) {
            showAlert("Validation Error", "Please select a category.");
            return false;
        }

        if (accountDropdown.getValue() == null) {
            showAlert("Validation Error", "Please select an account.");
            return false;
        }

        if (transactionTypeDropdown.getValue() == null) {
            showAlert("Validation Error", "Please select the transaction type (Income/Expense).");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void editTransaction(Transaction transaction) {
        selectedTransaction = transaction;
        descriptionField.setText(transaction.getDescription());
        amountField.setText(String.valueOf(transaction.getAmount()));

        if (transaction.getDate() != null) {
            datePicker.setValue(((java.sql.Date) transaction.getDate()).toLocalDate());
        }

        categoryDropdown.setValue(transaction.getCategory());
        accountDropdown.setValue(transaction.getAccount());
        transactionTypeDropdown.setValue(transaction.getType().substring(0, 1).toUpperCase() + transaction.getType().substring(1));

        if (transaction.isRecurring()) {
            recurringCheckBox.setSelected(true);
            recurrenceIntervalDropdown.setValue(transaction.getRecurrenceInterval());
        } else {
            recurringCheckBox.setSelected(false);
            recurrenceIntervalDropdown.setDisable(true);
        }

        saveButton.setText("Update Transaction");
    }

    private void updateTransaction(String description, double amount, LocalDate date, Category category, Account account, String type) {
        if (selectedTransaction != null) {
            selectedTransaction.setDescription(description);
            selectedTransaction.setAmount(amount);
            selectedTransaction.setDate(Date.valueOf(date));
            selectedTransaction.setCategory(category);
            selectedTransaction.setAccount(account);
            selectedTransaction.setType(type.toLowerCase());

            if (recurringCheckBox.isSelected()) {
                selectedTransaction.setRecurrenceInterval(recurrenceIntervalDropdown.getValue());
                transactionController.updateRecurringTransaction(selectedTransaction);
            } else {
                transactionController.updateTransaction(selectedTransaction);
            }
            refreshTransactionsTable();
            clearForm();
        }
    }

    private void deleteTransaction(Transaction transaction) {
        if (transaction != null) {
            transactionController.deleteTransaction(transaction);
            refreshTransactionsTable();
        }
    }

    private void refreshTransactionsTable() {
        transactionsTable.getItems().clear();
        ObservableList<Transaction> allTransactions = transactionController.getAllTransactionsForUser(loggedInUserId);
        transactionsTable.setItems(allTransactions);
    }

    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        categoryDropdown.getSelectionModel().clearSelection();
        accountDropdown.getSelectionModel().clearSelection();
        transactionTypeDropdown.getSelectionModel().clearSelection();
        recurringCheckBox.setSelected(false);
        recurrenceIntervalDropdown.setDisable(true);
        recurrenceIntervalDropdown.setValue(null);
        selectedTransaction = null;
        saveButton.setText("Save Transaction");
    }

    private ComboBox<Category> createCategoryDropdown(String userId) {
        ComboBox<Category> categoryDropdown = new ComboBox<>();
        ObservableList<Category> categories = FXCollections.observableArrayList(categoryController.getAllCategoriesForUser(userId));
        categoryDropdown.setItems(categories);
        categoryDropdown.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? "" : category.getName());
            }
        });
        categoryDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? "" : category.getName());
            }
        });
        return categoryDropdown;
    }

    private ComboBox<Account> createAccountDropdown(String userId) {
        ComboBox<Account> accountDropdown = new ComboBox<>();
        ObservableList<Account> accounts = FXCollections.observableArrayList(accountController.getAllAccountsForUser(userId));
        accountDropdown.setItems(accounts);
        accountDropdown.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty ? "" : account.getName());
            }
        });
        accountDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty ? "" : account.getName());
            }
        });
        return accountDropdown;
    }
}
