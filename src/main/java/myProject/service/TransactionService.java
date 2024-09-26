package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.repository.TransactionRepository;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    // Constructor with both dependencies
    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        System.out.println("TransactionService initialized with dependencies.");
    }

    // Add a new transaction
    public void addTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.addTransaction: Adding transaction - " + transaction);
        transactionRepository.saveTransaction(transaction);
        System.out.println("TransactionService.addTransaction: Transaction added successfully.");
    }

    // Add a recurring transaction
    public void addRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.addRecurringTransaction: Adding recurring transaction - " + transaction);
        transactionRepository.saveTransaction(transaction);
        transactionRepository.saveRecurringTransaction(transaction);
        scheduleNextRecurringTransaction(transaction);
        System.out.println("TransactionService.addRecurringTransaction: Recurring transaction added successfully.");
    }

    // Update an existing transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.updateTransaction: Updating transaction - " + transaction);
        transactionRepository.updateTransaction(transaction);
        System.out.println("TransactionService.updateTransaction: Transaction updated successfully.");
    }

    // Update a recurring transaction
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.updateRecurringTransaction: Updating recurring transaction - " + transaction);
        transactionRepository.updateTransaction(transaction);
        transactionRepository.updateRecurringTransaction(transaction);
        System.out.println("TransactionService.updateRecurringTransaction: Recurring transaction updated successfully.");
    }

    // Delete a transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.deleteTransaction: Deleting transaction - " + transaction);
        transactionRepository.deleteTransaction(transaction);
        System.out.println("TransactionService.deleteTransaction: Transaction deleted successfully.");
    }

    // Schedule the next occurrence of a recurring transaction
    private void scheduleNextRecurringTransaction(Transaction transaction) {
        System.out.println("TransactionService.scheduleNextRecurringTransaction: Scheduling next occurrence for recurring transaction - " + transaction);
        // Implement scheduling logic based on recurrence interval
    }

    // Fetch next occurrences of recurring transactions for a specific account
    public List<Transaction> getNextRecurringTransactionsByAccount(String accountId) throws SQLException {
        System.out.println("TransactionService.getNextRecurringTransactionsByAccount: Fetching next occurrences of recurring transactions for account ID - " + accountId);
        List<Transaction> recurringTransactions = transactionRepository.getRecurringTransactionsByAccount(accountId);
        List<Transaction> nextOccurrences = new ArrayList<>();

        for (Transaction recurring : recurringTransactions) {
            LocalDate nextDate = calculateNextRecurringDate(recurring);
            if (nextDate != null) {
                Transaction nextTransaction = new Transaction(
                        recurring.getDescription(),
                        recurring.getAmount(),
                        recurring.getType(),
                        null,
                        recurring.getAccount(),
                        recurring.getCategory(),
                        Date.valueOf(nextDate),
                        Time.valueOf(recurring.getTime().toLocalTime()),
                        "pending"
                );
                nextTransaction.setId(recurring.getId());
                nextTransaction.setRecurring(true);
                nextTransaction.setRecurrenceInterval(recurring.getRecurrenceInterval());
                nextOccurrences.add(nextTransaction);
            }
        }
        System.out.println("TransactionService.getNextRecurringTransactionsByAccount: Next occurrences fetched - " + nextOccurrences);
        return nextOccurrences;
    }

    // Calculate the next date of a recurring transaction
    private LocalDate calculateNextRecurringDate(Transaction transaction) {
        System.out.println("TransactionService.calculateNextRecurringDate: Calculating next date for transaction - " + transaction);
        LocalDate startDate = ((java.sql.Date) transaction.getDate()).toLocalDate();
        LocalDate today = LocalDate.now();
        String interval = transaction.getRecurrenceInterval().toLowerCase();

        switch (interval) {
            case "daily":
                while (!startDate.isAfter(today)) {
                    startDate = startDate.plusDays(1);
                }
                break;
            case "weekly":
                while (!startDate.isAfter(today)) {
                    startDate = startDate.plusWeeks(1);
                }
                break;
            case "monthly":
                while (!startDate.isAfter(today)) {
                    startDate = startDate.plusMonths(1);
                }
                break;
            default:
                return null;
        }
        System.out.println("TransactionService.calculateNextRecurringDate: Next date calculated - " + startDate);
        return startDate;
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() throws SQLException {
        System.out.println("TransactionService.getAllTransactions: Fetching all transactions.");
        List<Transaction> transactions = transactionRepository.getAllTransactions();
        System.out.println("TransactionService.getAllTransactions: Transactions fetched - " + transactions);
        return transactions;
    }

    // Get transactions filtered by account
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        System.out.println("TransactionService.getTransactionsByAccount: Fetching transactions for account - " + accountName);
        List<Transaction> transactions = transactionRepository.getTransactionsByAccount(accountName);
        System.out.println("TransactionService.getTransactionsByAccount: Transactions fetched - " + transactions);
        return transactions;
    }

    // Fetch transactions for a specific category
    public List<Transaction> getTransactionsByCategory(Category category) throws SQLException {
        System.out.println("TransactionService.getTransactionsByCategory: Fetching transactions for category - " + category);
        List<Transaction> transactions = transactionRepository.getTransactionsByCategory(category.getId());
        System.out.println("TransactionService.getTransactionsByCategory: Transactions fetched - " + transactions);
        return transactions;
    }

    // Fetch recurring transactions for a specific category
    public List<Transaction> getRecurringTransactionsByCategory(Category category) throws SQLException {
        System.out.println("TransactionService.getRecurringTransactionsByCategory: Fetching recurring transactions for category - " + category);
        List<Transaction> recurringTransactions = transactionRepository.getRecurringTransactionsByCategory(category.getId());
        System.out.println("TransactionService.getRecurringTransactionsByCategory: Recurring transactions fetched - " + recurringTransactions);
        return recurringTransactions;
    }


    // Get all account names for filtering
    public List<String> getAllAccountNames() throws SQLException {
        System.out.println("TransactionService.getAllAccountNames: Fetching all account names.");
        List<String> accountNames = transactionRepository.getAllAccountNames();
        System.out.println("TransactionService.getAllAccountNames: Account names fetched - " + accountNames);
        return accountNames;
    }

    // Fetch all categories for a specific user using CategoryService
    public List<Category> getAllCategoriesForUser(String userId) throws SQLException {
        System.out.println("TransactionService.getAllCategoriesForUser: Fetching categories for user ID - " + userId);
        List<Category> categories = categoryService.getAllCategoriesForUser(userId);
        System.out.println("TransactionService.getAllCategoriesForUser: Categories fetched - " + categories);
        return categories;
    }

    // Get all accounts for a specific user
    public List<Account> getAccountsForUser(String userId) throws SQLException {
        System.out.println("TransactionService.getAccountsForUser: Fetching accounts for user ID - " + userId);
        List<Account> accounts = transactionRepository.getAllAccountsForUser(userId);
        System.out.println("TransactionService.getAccountsForUser: Accounts fetched - " + accounts);
        return accounts;
    }
}