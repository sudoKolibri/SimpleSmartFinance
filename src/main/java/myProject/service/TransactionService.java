package myProject.service;

import myProject.model.Transaction;
import myProject.repository.TransactionRepository;

import java.sql.SQLException;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Add a new regular transaction
    public void addTransaction(Transaction transaction) throws SQLException {
        transactionRepository.saveTransaction(transaction);
    }

    // Add a new recurring transaction
    public void addRecurringTransaction(Transaction transaction) throws SQLException {
        transactionRepository.saveRecurringTransaction(transaction);
    }

    // Update an existing regular transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        transactionRepository.updateTransaction(transaction);
    }

    // Update an existing recurring transaction
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        transactionRepository.updateRecurringTransaction(transaction);
    }

    // Delete a regular transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        transactionRepository.deleteTransaction(transaction);
    }

    // Delete a recurring transaction
    public void deleteRecurringTransaction(Transaction transaction) throws SQLException {
        transactionRepository.deleteRecurringTransaction(transaction);
    }

    // Fetch all regular transactions for a specific user
    public List<Transaction> getAllTransactionsForUser(String userId) throws SQLException {
        return transactionRepository.getAllTransactionsForUser(userId);
    }

    // Fetch all recurring transactions for a specific user
    public List<Transaction> getRecurringTransactionsForUser(String userId) throws SQLException {
        return transactionRepository.getRecurringTransactionsForUser(userId);
    }
}
