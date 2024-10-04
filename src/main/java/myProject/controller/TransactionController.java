package myProject.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.service.TransactionService;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Der TransactionController verwaltet die Interaktionen zwischen der Benutzeroberfläche
 * und dem TransactionService. Er stellt Funktionen zum Erstellen, Aktualisieren, Löschen
 * und Abrufen von Transaktionen bereit.
 */
public class TransactionController {

    private final TransactionService transactionService;

    // Konstruktor zum Initialisieren des TransactionService
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Methode zur Erstellung einer neuen regulären oder wiederkehrenden Transaktion.
     * @param transaction Die zu erstellende Transaktion.
     */
    public void createTransaction(Transaction transaction) throws SQLException {

                transactionService.addTransaction(transaction);
                LoggerUtils.logInfo(TransactionController.class.getName(), "Reguläre Transaktion erfolgreich erstellt: " + transaction.getId());

    }

    /**
     * Methode zur Aktualisierung einer Transaktion (regulär oder wiederkehrend).
     * @param transaction Die zu aktualisierende Transaktion.
     */
    public void updateTransaction(Transaction transaction) throws SQLException {
        transactionService.updateTransaction(transaction);

    }

    /**
     * Methode zum Löschen einer Transaktion.
     * @param transaction Die zu löschende Transaktion.
     */
    public void deleteTransaction(Transaction transaction) {
        try {
            transactionService.deleteTransaction(transaction);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Transaktion erfolgreich gelöscht: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Löschen der Transaktion: " + e.getMessage(), e);
        }
    }

    /**
     * Methode zum Löschen aller Transaktionen eines Accounts
     *
     * @param accountId ID des Accounts
     * @throws SQLException Error Exception
     */
    public void deleteTransactionsByAccount(String accountId) throws SQLException {
        try {
            transactionService.deleteTransactionsByAccount(accountId);
            LoggerUtils.logInfo(TransactionController.class.getName(), "All transactions deleted for account: " + accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Error deleting transactions for account: " + accountId, e);
            throw e;
        }
    }

    /**
     * Methode zum Abrufen aller Transaktionen für eine bestimmte Kategorie.
     * @param category Die Kategorie, für die Transaktionen abgerufen werden sollen.
     * @return Liste der Transaktionen für die angegebene Kategorie.
     */
    public List<Transaction> getTransactionsByCategory(Category category) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByCategory(category);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Transaktionen für Kategorie " + category.getName() + " abgerufen.");
            return transactions;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Transaktionen für Kategorie: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Methode zum Abrufen aller Transaktionen für eine bestimmte Kategorie.
     * @param category Die Kategorie, für die Transaktionen abgerufen werden sollen.
     * @return Liste der Transaktionen für die angegebene Kategorie.
     */
    public List<Transaction> getTransactionsForCategory(Category category) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByCategory(category);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Transaktionen für Kategorie " + category.getName() + " abgerufen.");
            return transactions;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Transaktionen für Kategorie: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Methode zur Ermittlung des ausgegebenen Betrags für eine bestimmte Kategorie (inklusive wiederkehrender Transaktionen).
     * @param category Die Kategorie, für die der ausgegebene Betrag ermittelt werden soll.
     * @return Der ausgegebene Betrag.
     */
    public double getSpentAmountForCategory(Category category) {
        try {
            List<Transaction> regularTransactions = transactionService.getTransactionsByCategory(category);
            List<Transaction> allTransactions = new ArrayList<>(regularTransactions);

            // Nur Ausgaben (expense) Transaktionen summieren
            double totalSpent = allTransactions.stream()
                    .filter(transaction -> "expense".equals(transaction.getType()) && transaction.getAmount() < 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            LoggerUtils.logInfo(TransactionController.class.getName(), "Ausgegebener Betrag für Kategorie " + category.getName() + ": " + totalSpent);
            return totalSpent;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen des ausgegebenen Betrags für Kategorie: " + e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * Methode zur Ermittlung der Ausgaben nach Kategorie für ein Konto.
     * @param account Das Konto, für das die Ausgaben ermittelt werden sollen.
     * @return ObservableList der Ausgaben nach Kategorie.
     */
    public ObservableList<PieChart.Data> getSpendingByCategoryForAccount(Account account) {
        try {
            List<Category> categories = transactionService.getAllCategoriesForUser(account.getUserId());
            Map<String, Double> categorySpendingMap = new HashMap<>();

            for (Category category : categories) {
                List<Transaction> transactions = getTransactionsForCategory(category);
                double totalSpent = transactions.stream()
                        .filter(t -> t.getAccount() != null && t.getAccount().getId().equals(account.getId()) &&
                                t.getType().equalsIgnoreCase("expense"))
                        .mapToDouble(t -> Math.abs(t.getAmount()))
                        .sum();
                categorySpendingMap.put(category.getName(), totalSpent);
            }

            if (categorySpendingMap.isEmpty() || categorySpendingMap.values().stream().allMatch(v -> v == 0)) {
                return FXCollections.observableArrayList(
                        categories.stream()
                                .map(cat -> new PieChart.Data(cat.getName(), 0))
                                .collect(Collectors.toList())
                );
            }

            return FXCollections.observableArrayList(
                    categorySpendingMap.entrySet().stream()
                            .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Ausgaben nach Kategorie: " + e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Methode zum Filtern von Transaktionen nach Konto und Rückgabe als ObservableList.
     * @param accountName Der Name des Kontos, für das die Transaktionen gefiltert werden sollen.
     * @return ObservableList der gefilterten Transaktionen.
     */
    public ObservableList<Transaction> getTransactionsByAccount(String accountName) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccount(accountName);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Transaktionen für Konto " + accountName + " abgerufen.");
            return FXCollections.observableArrayList(transactions);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Transaktionen für Konto: " + e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Methode zum Abrufen aller Kategorien für einen bestimmten Benutzer als ObservableList.
     * @param userId Die ID des Benutzers.
     * @return ObservableList der Kategorien für den Benutzer.
     */
    public ObservableList<Category> getAllCategoriesForUser(String userId) {
        try {
            List<Category> categories = transactionService.getAllCategoriesForUser(userId);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Kategorien für Benutzer " + userId + " abgerufen.");
            return FXCollections.observableArrayList(categories);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Kategorien für Benutzer: " + e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Methode zum Abrufen aller Konten für den angemeldeten Benutzer als ObservableList.
     * @param userId Die ID des Benutzers.
     * @return ObservableList der Konten für den Benutzer.
     */
    public ObservableList<Account> getAccountsForUser(String userId) {
        try {
            List<Account> accounts = transactionService.getAccountsForUser(userId);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Konten für Benutzer " + userId + " abgerufen.");
            return FXCollections.observableArrayList(accounts);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der Konten für Benutzer: " + e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Methode zum Abrufen abgeschlossener Transaktionen für ein bestimmtes Konto.
     * @param accountName Der Name des Kontos.
     * @return Liste der abgeschlossenen Transaktionen für das Konto.
     */
    public List<Transaction> getCompletedTransactionsByAccount(String accountName) {
        try {
            List<Transaction> completedTransactions = transactionService.getCompletedTransactionsByAccount(accountName);
            LoggerUtils.logInfo(TransactionController.class.getName(), "Abgeschlossene Transaktionen für Konto " + accountName + " abgerufen.");
            return completedTransactions;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionController.class.getName(), "Fehler beim Abrufen der abgeschlossenen Transaktionen: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
