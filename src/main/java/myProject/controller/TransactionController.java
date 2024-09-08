package myProject.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import myProject.model.Transaction;
import myProject.service.TransactionService;

import java.sql.SQLException;
import java.util.List;

public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Handle request to create a new regular transaction
    public void createTransaction(Transaction transaction) {
        try {
            transactionService.addTransaction(transaction);
            System.out.println("Transaction created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle request to create a new recurring transaction
    public void createRecurringTransaction(Transaction transaction) {
        try {
            transactionService.addRecurringTransaction(transaction);
            System.out.println("Recurring Transaction created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating recurring transaction: " + e.getMessage());
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

    // Handle request to update an existing recurring transaction
    public void updateRecurringTransaction(Transaction transaction) {
        try {
            transactionService.updateRecurringTransaction(transaction);
            System.out.println("Recurring Transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle request to delete a transaction (regular or recurring)
    public void deleteTransaction(Transaction transaction) {
        try {
            if (transaction.isRecurring()) {
                deleteRecurringTransaction(transaction);
            } else {
                transactionService.deleteTransaction(transaction);
                System.out.println("Transaction deleted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle request to delete a recurring transaction
    public void deleteRecurringTransaction(Transaction transaction) {
        try {
            transactionService.deleteRecurringTransaction(transaction);
            System.out.println("Recurring transaction deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convert a recurring transaction to a regular transaction
    public void convertRecurringToRegular(Transaction transaction) {
        try {
            transaction.setRecurring(false);  // Set the transaction to non-recurring
            transaction.setRecurrenceInterval(null);  // Remove the recurrence interval
            transactionService.updateTransaction(transaction);  // Update it as a regular transaction
            System.out.println("Recurring transaction converted to regular transaction.");
        } catch (SQLException e) {
            System.err.println("Error converting recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get all transactions (regular and recurring) for a specific user
    public ObservableList<Transaction> getAllTransactionsForUser(String userId) {
        try {
            // Fetch regular transactions for the user
            List<Transaction> regularTransactions = transactionService.getAllTransactionsForUser(userId);
            // Fetch recurring transactions for the user
            List<Transaction> recurringTransactions = transactionService.getRecurringTransactionsForUser(userId);

            // Combine both lists
            regularTransactions.addAll(recurringTransactions);

            return FXCollections.observableArrayList(regularTransactions);  // Convert List to ObservableList
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for user: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();  // Return empty ObservableList on error
        }
    }
}
