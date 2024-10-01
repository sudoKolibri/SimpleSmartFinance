package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.repository.AccountRepository;
import myProject.repository.TransactionRepository;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountRepository accountRepository;

    // Konstruktor mit den benötigten Abhängigkeiten
    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.accountRepository = accountRepository;
        System.out.println("TransactionService initialized with dependencies.");
    }

    // Methode zum Hinzufügen einer neuen Transaktion
    public void addTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.addTransaction: Adding transaction - " + transaction);

        // Wenn die Transaktion eine Ausgabe (Expense) ist, stelle sicher, dass der Betrag immer negativ ist.
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        // Speichere die Transaktion
        transactionRepository.saveTransaction(transaction);
        System.out.println("TransactionService.addTransaction: Transaction added successfully.");
    }

    // Methode zum Hinzufügen einer neuen wiederkehrenden Transaktion
    public void addRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.addRecurringTransaction: Adding recurring transaction - " + transaction);

        // Stelle sicher, dass Ausgaben immer negativ sind
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        // Speichere die reguläre Transaktion und die wiederkehrende Transaktion
        transactionRepository.saveTransaction(transaction);
        transactionRepository.saveRecurringTransaction(transaction);

        // Plane das nächste Vorkommen der wiederkehrenden Transaktion
        scheduleNextRecurringTransaction(transaction);
        System.out.println("TransactionService.addRecurringTransaction: Recurring transaction added successfully.");
    }

    // Methode zum Aktualisieren einer bestehenden Transaktion
    public void updateTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.updateTransaction: Updating transaction - " + transaction);

        // Gleiche Logik für Expenses: stelle sicher, dass der Betrag negativ bleibt
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        // Aktualisiere die Transaktion in der Datenbank
        transactionRepository.updateTransaction(transaction);
        System.out.println("TransactionService.updateTransaction: Transaction updated successfully.");
    }

    // Methode zum Aktualisieren einer bestehenden wiederkehrenden Transaktion
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.updateRecurringTransaction: Updating recurring transaction - " + transaction);

        // Stelle sicher, dass Ausgaben immer negativ bleiben
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        // Aktualisiere die wiederkehrende Transaktion
        transactionRepository.updateRecurringTransaction(transaction);
        System.out.println("TransactionService.updateRecurringTransaction: Recurring transaction updated successfully.");
    }

    // Methode zum Löschen einer Transaktion
    public void deleteTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.deleteTransaction: Deleting transaction - " + transaction);
        transactionRepository.deleteTransaction(transaction);
        System.out.println("TransactionService.deleteTransaction: Transaction deleted successfully.");
    }

    // Methode zum Löschen einer wiederkehrenden Transaktion
    public void deleteRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.deleteRecurringTransaction: Deleting recurring transaction - " + transaction);

        // Lösche die wiederkehrende Transaktion und alle damit verbundenen pending Transaktionen
        transactionRepository.deleteRecurringTransaction(transaction);
        transactionRepository.deletePendingTransactionsByRecurringId(transaction.getId());

        // Setze die Transaktion auf einmalig und aktualisiere sie
        transaction.setRecurring(false);
        transaction.setRecurrenceInterval(null);
        transactionRepository.updateTransaction(transaction);

        System.out.println("TransactionService.deleteRecurringTransaction: Recurring transaction deleted and converted to a single transaction.");
    }

    // Methode zum Löschen ausstehender (pending) Transaktionen anhand der recurring_transaction_id
    public void deletePendingTransactionsByRecurringId(String recurringTransactionId) throws SQLException {
        System.out.println("TransactionService.deletePendingTransactionsByRecurringId: Deleting pending transactions for recurring ID - " + recurringTransactionId);
        transactionRepository.deletePendingTransactionsByRecurringId(recurringTransactionId);
        System.out.println("TransactionService.deletePendingTransactionsByRecurringId: Pending transactions deleted successfully.");
    }

    // Methode zum Löschen einer einzelnen ausstehenden (pending) Transaktion
    public void deletePendingTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionService.deletePendingTransaction: Deleting pending transaction - " + transaction);

        if ("pending".equalsIgnoreCase(transaction.getStatus())) {
            transactionRepository.deleteTransaction(transaction);
            System.out.println("TransactionService.deletePendingTransaction: Pending transaction deleted successfully.");
        } else {
            System.out.println("TransactionService.deletePendingTransaction: Transaction is not pending.");
        }
    }


    // Zeitplanung für das nächste Vorkommen einer wiederkehrenden Transaktion
    private void scheduleNextRecurringTransaction(Transaction transaction) {
        System.out.println("TransactionService.scheduleNextRecurringTransaction: Scheduling next occurrence for recurring transaction - " + transaction);
        // Logik zur Zeitplanung der nächsten wiederkehrenden Transaktion
        // Dies muss basierend auf der Logik des Wiederholungsintervalls implementiert werden.
    }

    // Methode zum Abrufen der nächsten Vorkommen wiederkehrender Transaktionen für ein Konto
    public List<Transaction> getNextRecurringTransactionsByAccount(String accountId) throws SQLException {
        System.out.println("TransactionService.getNextRecurringTransactionsByAccount: Fetching next occurrences of recurring transactions for account ID - " + accountId);
        List<Transaction> recurringTransactions = transactionRepository.getRecurringTransactionsByAccount(accountId);
        List<Transaction> nextOccurrences = new ArrayList<>();

        for (Transaction recurring : recurringTransactions) {
            LocalDate nextDate = calculateNextRecurringDate(recurring);
            if (nextDate != null) {
                Transaction nextTransaction = new Transaction(recurring.getDescription(), recurring.getAmount(), recurring.getType(), null, recurring.getAccount(), recurring.getCategory(), Date.valueOf(nextDate), Time.valueOf(recurring.getTime().toLocalTime()), "pending");
                nextTransaction.setId(recurring.getId());
                nextTransaction.setRecurring(true);
                nextTransaction.setRecurrenceInterval(recurring.getRecurrenceInterval());
                nextOccurrences.add(nextTransaction);
            }
        }
        System.out.println("TransactionService.getNextRecurringTransactionsByAccount: Next occurrences fetched - " + nextOccurrences);
        return nextOccurrences;
    }

    // Methode zur Berechnung des nächsten Vorkommens einer wiederkehrenden Transaktion
    public LocalDate calculateNextRecurringDate(Transaction transaction) {
        System.out.println("TransactionService.calculateNextRecurringDate: Calculating next date for transaction - " + transaction);
        LocalDate startDate = new java.sql.Date(transaction.getDate().getTime()).toLocalDate();
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

    // Methode zum Abrufen aller Transaktionen
    public List<Transaction> getAllTransactions() throws SQLException {
        System.out.println("TransactionService.getAllTransactions: Fetching all transactions.");
        List<Transaction> transactions = transactionRepository.getAllTransactions();
        System.out.println("TransactionService.getAllTransactions: Transactions fetched - " + transactions);
        return transactions;
    }

    // Methode zum Abrufen der Transaktionen für ein Konto
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        System.out.println("TransactionService.getTransactionsByAccount: Fetching transactions for account - " + accountName);
        List<Transaction> transactions = transactionRepository.getTransactionsByAccount(accountName);
        System.out.println("TransactionService.getTransactionsByAccount: Transactions fetched - " + transactions);
        return transactions;
    }

    // Methode zum Abrufen abgeschlossener Transaktionen für ein Konto
    public List<Transaction> getCompletedTransactionsByAccount(String accountName) throws SQLException {
        System.out.println("TransactionService.getCompletedTransactionsByAccount: Fetching completed transactions for account - " + accountName);
        List<Transaction> transactions = transactionRepository.getTransactionsByAccount(accountName);
        return transactions.stream().filter(t -> t.getStatus().equalsIgnoreCase("completed")).collect(Collectors.toList());
    }

    // Methode zum Abrufen der Transaktionen für eine Kategorie
    public List<Transaction> getTransactionsByCategory(Category category) throws SQLException {
        System.out.println("TransactionService.getTransactionsByCategory: Fetching transactions for category - " + category);
        List<Transaction> transactions = transactionRepository.getTransactionsByCategory(category.getId());
        System.out.println("TransactionService.getTransactionsByCategory: Transactions fetched - " + transactions);
        return transactions;
    }

    // Methode zum Abrufen wiederkehrender Transaktionen für eine Kategorie
    public List<Transaction> getRecurringTransactionsByCategory(Category category) throws SQLException {
        System.out.println("TransactionService.getRecurringTransactionsByCategory: Fetching recurring transactions for category - " + category);
        List<Transaction> recurringTransactions = transactionRepository.getRecurringTransactionsByCategory(category.getId());
        System.out.println("TransactionService.getRecurringTransactionsByCategory: Recurring transactions fetched - " + recurringTransactions);
        return recurringTransactions;
    }

    // Methode zum Abrufen aller Kontonamen
    public List<String> getAllAccountNames() throws SQLException {
        System.out.println("TransactionService.getAllAccountNames: Fetching all account names.");
        List<String> accountNames = accountRepository.getAllAccountNames();
        System.out.println("TransactionService.getAllAccountNames: Account names fetched - " + accountNames);
        return accountNames;
    }

    // Methode zum Abrufen aller Kategorien für einen Benutzer
    public List<Category> getAllCategoriesForUser(String userId) throws SQLException {
        System.out.println("TransactionService.getAllCategoriesForUser: Fetching categories for user ID - " + userId);
        List<Category> categories = categoryService.getAllCategoriesForUser(userId);
        System.out.println("TransactionService.getAllCategoriesForUser: Categories fetched - " + categories);
        return categories;
    }

    // Methode zum Abrufen aller Konten für einen Benutzer
    public List<Account> getAccountsForUser(String userId) throws SQLException {
        System.out.println("TransactionService.getAccountsForUser: Fetching accounts for user ID - " + userId);
        List<Account> accounts = accountRepository.getAllAccountsForUser(userId);
        System.out.println("TransactionService.getAccountsForUser: Accounts fetched - " + accounts);
        return accounts;
    }
}
