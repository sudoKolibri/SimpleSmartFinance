package myProject.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.service.TransactionService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Handle request to create a new regular or recurring transaction
    public void createTransaction(Transaction transaction) {
        try {
            if (transaction.isRecurring()) {
                transactionService.addRecurringTransaction(transaction);
            } else {
                transactionService.addTransaction(transaction);
            }
            System.out.println("Transaction created successfully.");
        } catch (Exception e) {

            e.printStackTrace(); // Log full stack trace for debugging
        }
    }


    // TransactionController.java
    public void updateTransaction(Transaction transaction) {
        try {
            if (transaction.isRecurring()) {
                transactionService.updateRecurringTransaction(transaction);
            } else {
                transactionService.updateTransaction(transaction);
            }
            System.out.println("Transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Handle request to delete a transaction
    public void deleteTransaction(Transaction transaction) {
        try {
            transactionService.deleteTransaction(transaction);
            System.out.println("Transaction deleted successfully. ID: " + transaction.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteRecurringTransaction(Transaction transaction) {
        try {
            // Lösche die Originaltransaktion und konvertiere sie in eine einmalige Transaktion
            transactionService.deleteRecurringTransaction(transaction);
            System.out.println("Recurring transaction deleted and converted to single transaction. ID: " + transaction.getId());
        } catch (SQLException e) {
            System.err.println("Error deleting recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void deletePendingTransactionsByRecurringId(String recurringTransactionId) {
        try {
            transactionService.deletePendingTransactionsByRecurringId(recurringTransactionId);
            System.out.println("Pending transactions deleted successfully for recurring ID: " + recurringTransactionId);
        } catch (SQLException e) {
            System.err.println("Error deleting pending transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methode zum Löschen einer ausstehenden (pending) Transaktion
    public void deletePendingTransaction(Transaction transaction) {
        try {
            if ("pending".equalsIgnoreCase(transaction.getStatus())) {
                transactionService.deletePendingTransaction(transaction);
                System.out.println("Pending transaction deleted successfully. ID: " + transaction.getId());
            } else {
                System.out.println("Transaction is not pending. Cannot delete.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting pending transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public LocalDate getNextRecurringDate(Transaction transaction) {
        return transactionService.calculateNextRecurringDate(transaction);
    }






    // Fetch next occurrence of recurring transactions for a specific account
    public ObservableList<Transaction> getNextRecurringTransactionsByAccount(String accountId) {
        try {
            List<Transaction> recurringTransactions = transactionService.getNextRecurringTransactionsByAccount(accountId);
            return FXCollections.observableArrayList(recurringTransactions);
        } catch (SQLException e) {
            System.err.println("Error fetching next occurrences of recurring transactions: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }


    // Handle money transfer between accounts
    public void transferBetweenAccounts(Transaction transferOut, Transaction transferIn) {
        try {
            transactionService.addTransaction(transferOut);
            transactionService.addTransaction(transferIn);
            System.out.println("Transfer completed successfully.");
        } catch (SQLException e) {
            System.err.println("Error during transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get all transactions as an ObservableList for TableView
    public ObservableList<Transaction> getAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            return FXCollections.observableArrayList(transactions);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    // Get all transactions for a specific category
    public List<Transaction> getTransactionsForCategory(Category category) {
        try {
            return transactionService.getTransactionsByCategory(category);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for category: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Method to get spending by category for a specific account with default empty data
    public ObservableList<PieChart.Data> getSpendingByCategoryForAccount(Account account) {
        try {
            // Fetch all categories for the user associated with the account
            List<Category> categories = transactionService.getAllCategoriesForUser(account.getUserId());
            Map<String, Double> categorySpendingMap = new HashMap<>();

            for (Category category : categories) {
                List<Transaction> transactions = getTransactionsForCategory(category);
                // Calculate total spent and convert to positive values for chart display
                double totalSpent = transactions.stream()
                        .filter(t -> t.getAccount() != null && t.getAccount().getId().equals(account.getId()) &&
                                t.getType().equalsIgnoreCase("expense"))
                        .mapToDouble(t -> Math.abs(t.getAmount())) // Use Math.abs() to ensure positive values for expenses
                        .sum();

                // Log each category's spending
                System.out.println("Category: " + category.getName() + ", Total Spent: " + totalSpent);

                // Add to map regardless of whether the amount is zero or non-zero
                categorySpendingMap.put(category.getName(), totalSpent);
            }

            // Check if the spending map is empty
            if (categorySpendingMap.isEmpty() || categorySpendingMap.values().stream().allMatch(v -> v == 0)) {
                System.out.println("No significant spending data found. Displaying empty categories.");
                // Display empty categories with zero values for the user to see
                return FXCollections.observableArrayList(
                        categories.stream()
                                .map(cat -> new PieChart.Data(cat.getName(), 0))
                                .collect(Collectors.toList())
                );
            }

            System.out.println("Category Spending Map: " + categorySpendingMap);
            return FXCollections.observableArrayList(
                    categorySpendingMap.entrySet().stream()
                            .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList())
            );
        } catch (SQLException e) {
            System.err.println("Error fetching spending by category: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }





    // Method to get total spent amount for a specific category, including recurring transactions
    public double getSpentAmountForCategory(Category category) {
        try {
            // Fetch regular transactions for the category
            List<Transaction> regularTransactions = transactionService.getTransactionsByCategory(category);
            // Fetch recurring transactions for the category
            List<Transaction> recurringTransactions = transactionService.getRecurringTransactionsByCategory(category);

            // Combine both lists
            List<Transaction> allTransactions = new ArrayList<>();
            allTransactions.addAll(regularTransactions);
            allTransactions.addAll(recurringTransactions);

            // Calculate the total amount spent
            return allTransactions.stream().mapToDouble(Transaction::getAmount).sum();
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for category: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }


    // Filter transactions by account and return as ObservableList for TableView
    public ObservableList<Transaction> getTransactionsByAccount(String accountName) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccount(accountName);
            return FXCollections.observableArrayList(transactions);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions by account: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    // Get all account names for filtering
    public List<String> getAllAccountNames() {
        try {
            return transactionService.getAllAccountNames();
        } catch (SQLException e) {
            System.err.println("Error fetching account names: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get all categories (custom + standard) for a specific user as an ObservableList
    public ObservableList<Category> getAllCategoriesForUser(String userId) {
        try {
            List<Category> categories = transactionService.getAllCategoriesForUser(userId);
            return FXCollections.observableArrayList(categories);
        } catch (SQLException e) {
            System.err.println("Error fetching categories for user: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    // Fetch all accounts for the logged-in user as an ObservableList for ComboBox
    public ObservableList<Account> getAccountsForUser(String userId) {
        try {
            List<Account> accounts = transactionService.getAccountsForUser(userId);
            return FXCollections.observableArrayList(accounts);
        } catch (SQLException e) {
            System.err.println("Error fetching accounts for user: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    // Method to get completed transactions by account
    public List<Transaction> getCompletedTransactionsByAccount(String accountName) {
        try {
            System.out.println("TransactionController.getCompletedTransactionsByAccount: Fetching completed transactions for account - " + accountName);
            return transactionService.getCompletedTransactionsByAccount(accountName);
        } catch (SQLException e) {
            System.err.println("TransactionController.getCompletedTransactionsByAccount: Error fetching completed transactions - " + e.getMessage());
            return new ArrayList<>(); // Return empty list in case of error
        }
    }
}
