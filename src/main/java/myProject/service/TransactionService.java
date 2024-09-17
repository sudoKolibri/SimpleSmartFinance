package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.repository.TransactionRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Add a new transaction
    public void addTransaction(Transaction transaction) throws SQLException {
        transactionRepository.saveTransaction(transaction);
    }

    // Update an existing transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        transactionRepository.updateTransaction(transaction);
    }

    // Delete a transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        System.out.println("Attempting to delete transaction with ID: " + transaction.getId());
        transactionRepository.deleteTransaction(transaction);
        // You could query the database again to check if the row was actually deleted.
    }


    // Get all transactions
    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionRepository.getAllTransactions();
    }

    // Get transactions filtered by account
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        return transactionRepository.getTransactionsByAccount(accountName);
    }

    // Fetch transactions for a specific category
    public List<Transaction> getTransactionsByCategory(Category category) throws SQLException {
        return transactionRepository.getTransactionsByCategory(category.getId());
    }


    // Get all account names for filtering
    public List<String> getAllAccountNames() throws SQLException {
        return transactionRepository.getAllAccountNames();
    }

    // Get all categories (custom for the user + standard)
    public List<Category> getAllCategoriesForUser(String userId) throws SQLException {
        List<Category> allCategories = new ArrayList<>();
        List<Category> customCategories = transactionRepository.getCustomCategoriesForUser(userId);
        List<Category> standardCategories = transactionRepository.getStandardCategories();

        allCategories.addAll(customCategories);
        allCategories.addAll(standardCategories);

        return allCategories;
    }

    // Get all accounts for a specific user
    public List<Account> getAccountsForUser(String userId) throws SQLException {
        return transactionRepository.getAllAccountsForUser(userId);
    }
}
