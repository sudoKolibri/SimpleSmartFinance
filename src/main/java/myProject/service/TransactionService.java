package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.repository.AccountRepository;
import myProject.repository.TransactionRepository;
import myProject.util.LoggerUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Der TransactionService ist für die Geschäftslogik im Zusammenhang mit Transaktionen verantwortlich.
 * Er arbeitet mit dem TransactionRepository zusammen, um Transaktionen zu erstellen, zu aktualisieren, zu löschen und abzurufen.
 */
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountRepository accountRepository;

    // Konstruktor mit den benötigten Abhängigkeiten
    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.accountRepository = accountRepository;
    }

    /**
     * Fügt eine neue Transaktion hinzu.
     *
     * @param transaction Die hinzuzufügende Transaktion.
     */
    public void addTransaction(Transaction transaction) throws SQLException {
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        try {
            transactionRepository.saveTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Hinzufügen der Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Fügt eine neue wiederkehrende Transaktion hinzu.
     *
     * @param transaction Die hinzuzufügende wiederkehrende Transaktion.
     */
    public void addRecurringTransaction(Transaction transaction) throws SQLException {
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        try {
            transactionRepository.saveTransaction(transaction);
            transactionRepository.saveRecurringTransaction(transaction);
            scheduleNextRecurringTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Hinzufügen der wiederkehrenden Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Aktualisiert eine bestehende Transaktion.
     *
     * @param transaction Die zu aktualisierende Transaktion.
     */
    public void updateTransaction(Transaction transaction) throws SQLException {
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        try {
            transactionRepository.updateTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Aktualisieren der Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Aktualisiert eine bestehende wiederkehrende Transaktion.
     *
     * @param transaction Die zu aktualisierende wiederkehrende Transaktion.
     */
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        try {
            transactionRepository.updateRecurringTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Aktualisieren der wiederkehrenden Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Löscht eine Transaktion.
     *
     * @param transaction Die zu löschende Transaktion.
     */
    public void deleteTransaction(Transaction transaction) throws SQLException {
        try {
            transactionRepository.deleteTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Löschen der Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Löscht eine wiederkehrende Transaktion.
     *
     * @param transaction Die zu löschende wiederkehrende Transaktion.
     */
    public void deleteRecurringTransaction(Transaction transaction) throws SQLException {
        try {
            transactionRepository.deleteRecurringTransaction(transaction);
            transactionRepository.deletePendingTransactionsByRecurringId(transaction.getId());

            transaction.setRecurring(false);
            transaction.setRecurrenceInterval(null);
            transactionRepository.updateTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Löschen der wiederkehrenden Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Löscht ausstehende (pending) Transaktionen anhand der recurring_transaction_id.
     *
     * @param recurringTransactionId Die ID der wiederkehrenden Transaktion.
     */
    public void deletePendingTransactionsByRecurringId(String recurringTransactionId) throws SQLException {
        try {
            transactionRepository.deletePendingTransactionsByRecurringId(recurringTransactionId);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Löschen ausstehender Transaktionen: " + recurringTransactionId, e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Löscht eine einzelne ausstehende (pending) Transaktion.
     *
     * @param transaction Die zu löschende ausstehende Transaktion.
     */
    public void deletePendingTransaction(Transaction transaction) throws SQLException {
        if ("pending".equalsIgnoreCase(transaction.getStatus())) {
            try {
                transactionRepository.deleteTransaction(transaction);
            } catch (Exception e) {
                LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Löschen der ausstehenden Transaktion: " + transaction.getId(), e);
                throw e; // SQLException weiterwerfen
            }
        }
    }

    // Zeitplanung für das nächste Vorkommen einer wiederkehrenden Transaktion
    private void scheduleNextRecurringTransaction(Transaction transaction) {
        LocalDate startDate = ((java.sql.Date) transaction.getDate()).toLocalDate();
        LocalDate today = LocalDate.now();

        // Berechne alle Vorkommnisse zwischen Startdatum und heute
        while (startDate.isBefore(today) || startDate.isEqual(today)) {
            // Erstelle die nächste Vorkommnis-Transaktion
            Transaction nextTransaction = new Transaction(
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getType(),
                    null,
                    transaction.getAccount(),
                    transaction.getCategory(),
                    Date.valueOf(startDate),
                    transaction.getTime(),
                    "completed"  // Als abgeschlossen markieren, da diese in der Vergangenheit liegt
            );

            try {
                transactionRepository.saveTransaction(nextTransaction);
            } catch (Exception e) {
                LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Speichern der nächsten Vorkommnisse der wiederkehrenden Transaktion: " + transaction.getId(), e);
            }

            // Berechne das nächste Vorkommnis
            startDate = calculateNextRecurringDate(transaction);
        }
    }


    /**
     * Ruft die nächsten Vorkommen wiederkehrender Transaktionen für ein Konto ab.
     * @param accountId Die ID des Kontos.
     * @return Liste der nächsten wiederkehrenden Transaktionen.
     */
    public List<Transaction> getNextRecurringTransactionsByAccount(String accountId) {
        try {
            List<Transaction> recurringTransactions = transactionRepository.getRecurringTransactionsByAccount(accountId);
            List<Transaction> nextOccurrences = new ArrayList<>();

            for (Transaction recurring : recurringTransactions) {
                LocalDate nextDate = calculateNextRecurringDate(recurring);
                if (nextDate != null) {
                    Transaction nextTransaction = new Transaction(recurring.getDescription(),
                            recurring.getAmount(), recurring.getType(), null, recurring.getAccount(),
                            recurring.getCategory(), Date.valueOf(nextDate),
                            Time.valueOf(recurring.getTime().toLocalTime()), "pending");

                    nextTransaction.setId(recurring.getId());
                    nextTransaction.setRecurring(true);
                    nextTransaction.setRecurrenceInterval(recurring.getRecurrenceInterval());
                    nextOccurrences.add(nextTransaction);
                }
            }
            return nextOccurrences;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der nächsten Vorkommen wiederkehrender Transaktionen: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Berechnet das nächste Vorkommen einer wiederkehrenden Transaktion.
     * @param transaction Die wiederkehrende Transaktion.
     * @return Das nächste Vorkommen als LocalDate.
     */
    public LocalDate calculateNextRecurringDate(Transaction transaction) {
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
                return null; // Rückgabe null, wenn kein gültiges Intervall
        }
        return startDate;
    }

    /**
     * Ruft alle Transaktionen ab.
     * @return Liste aller Transaktionen.
     */
    public List<Transaction> getAllTransactions() {
        try {
            return transactionRepository.getAllTransactions();
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen aller Transaktionen: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft die Transaktionen für ein Konto ab.
     * @param accountName Der Name des Kontos.
     * @return Liste der Transaktionen für das Konto.
     */
    public List<Transaction> getTransactionsByAccount(String accountName) {
        try {
            return transactionRepository.getTransactionsByAccount(accountName);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der Transaktionen für Konto: " + accountName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Berechnet die Bilanz für abgeschlossene Transaktionen eines Kontos.
     * @param account Das Konto, für das die Bilanz berechnet werden soll.
     * @return Die berechnete Bilanz.
     */
    public double calculateBalanceForCompletedTransactions(Account account) throws SQLException {
        try {
            // Abrufen der abgeschlossenen Transaktionen
            List<Transaction> completedTransactions = getCompletedTransactionsByAccount(account.getName());

            // Berechne die Bilanz basierend auf den abgeschlossenen Transaktionen
            double transactionSum = completedTransactions.stream().mapToDouble(Transaction::getAmount).sum();

            // Setze die Bilanz des Kontos und aktualisiere sie in der Datenbank
            account.setBalance(transactionSum);
            accountRepository.updateAccount(account);  // Bilanz im Konto aktualisieren

            return transactionSum;
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Berechnen der Bilanz für Konto: " + account.getName() + ", " + e.getMessage(), e);
            throw e; // SQLException weiterwerfen
        }
    }

    /**
     * Ruft die abgeschlossenen Transaktionen für ein Konto ab.
     * @param accountName Der Name des Kontos.
     * @return Liste der abgeschlossenen Transaktionen.
     */
    public List<Transaction> getCompletedTransactionsByAccount(String accountName) {
        try {
            List<Transaction> transactions = getTransactionsByAccount(accountName);
            return transactions.stream().filter(t -> t.getStatus().equalsIgnoreCase("completed")).collect(Collectors.toList());
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der abgeschlossenen Transaktionen für Konto: " + accountName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft die Transaktionen für eine Kategorie ab.
     * @param category Die Kategorie, für die die Transaktionen abgerufen werden sollen.
     * @return Liste der Transaktionen für die Kategorie.
     */
    public List<Transaction> getTransactionsByCategory(Category category) {
        try {
            return transactionRepository.getTransactionsByCategory(category.getId());
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der Transaktionen für Kategorie: " + category.getName(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft wiederkehrende Transaktionen für eine Kategorie ab.
     * @param category Die Kategorie, für die die wiederkehrenden Transaktionen abgerufen werden sollen.
     * @return Liste der wiederkehrenden Transaktionen für die Kategorie.
     */
    public List<Transaction> getRecurringTransactionsByCategory(Category category) {
        try {
            return transactionRepository.getRecurringTransactionsByCategory(category.getId());
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen wiederkehrender Transaktionen für Kategorie: " + category.getName(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft alle Kontonamen ab.
     * @return Liste der Kontonamen.
     */
    public List<String> getAllAccountNames() {
        try {
            return accountRepository.getAllAccountNames();
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der Kontonamen: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft alle Kategorien für einen Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @return Liste der Kategorien für den Benutzer.
     */
    public List<Category> getAllCategoriesForUser(String userId) {
        try {
            return categoryService.getAllCategoriesForUser(userId);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der Kategorien für Benutzer: " + userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Ruft alle Konten für einen Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @return Liste der Konten für den Benutzer.
     */
    public List<Account> getAccountsForUser(String userId) {
        try {
            return accountRepository.getAllAccountsForUser(userId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der Konten für Benutzer: " + userId, e);
            return new ArrayList<>();
        }
    }
}
