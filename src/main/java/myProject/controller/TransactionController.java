package myProject.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.service.TransactionService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Handle request to create a new regular transaction
    public void createTransaction(Transaction transaction) {
        // Log the ID of the transaction before it's passed to the service layer
        System.out.println("Creating transaction with ID: " + transaction.getId());  // Log the ID

        try {
            // Pass the transaction to the service layer to be added to the database
            transactionService.addTransaction(transaction);
            System.out.println("Transaction created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Handle request to update an existing transaction
    public void updateTransaction(Transaction transaction) {
        try {
            transactionService.updateTransaction(transaction);
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


    // Get all transactions as an ObservableList for TableView
    public ObservableList<Transaction> getAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            return FXCollections.observableArrayList(transactions);  // Convert List to ObservableList
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();  // Return empty ObservableList on error
        }
    }

    // Get all transactions for a specific category
    public List<Transaction> getTransactionsForCategory(Category category) {
        try {
            return transactionService.getTransactionsByCategory(category);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for category: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();  // Return an empty list on error
        }
    }

    // Method to get total spent amount for a specific category
    public double getSpentAmountForCategory(Category category) {
        try {
            // Get transactions from the service
            List<Transaction> transactions = transactionService.getTransactionsByCategory(category);
            // Sum the amounts of all transactions in this category
            return transactions.stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for category: " + e.getMessage());
            e.printStackTrace();
            return 0.0;  // Return 0 if there's an error
        }
    }



    // Filter transactions by account and return as ObservableList for TableView
    public ObservableList<Transaction> getTransactionsByAccount(String accountName) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccount(accountName);
            return FXCollections.observableArrayList(transactions);  // Convert List to ObservableList
        } catch (SQLException e) {
            System.err.println("Error fetching transactions by account: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();  // Return empty ObservableList on error
        }
    }

    // Get all account names for filtering
    public List<String> getAllAccountNames() {
        try {
            return transactionService.getAllAccountNames();
        } catch (SQLException e) {
            System.err.println("Error fetching account names: " + e.getMessage());
            e.printStackTrace();
            return List.of();  // Return an empty List on error
        }
    }

    // Get all categories (custom + standard) for a specific user as an ObservableList
    public ObservableList<Category> getAllCategoriesForUser(String userId) {
        try {
            List<Category> categories = transactionService.getAllCategoriesForUser(userId);
            return FXCollections.observableArrayList(categories);  // Convert List to ObservableList
        } catch (SQLException e) {
            System.err.println("Error fetching categories for user: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();  // Return empty ObservableList on error
        }
    }

    // Fetch all accounts for the logged-in user as an ObservableList for ComboBox
    public ObservableList<Account> getAccountsForUser(String userId) {
        try {
            List<Account> accounts = transactionService.getAccountsForUser(userId);
            return FXCollections.observableArrayList(accounts);  // Convert List to ObservableList
        } catch (SQLException e) {
            System.err.println("Error fetching accounts for user: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();  // Return empty ObservableList on error
        }
    }
}
