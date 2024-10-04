package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.repository.AccountRepository;
import myProject.repository.TransactionRepository;
import myProject.util.LoggerUtils;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
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
    public void addTransaction(Transaction transaction) {
        // Überprüfe, ob die Transaktion in der Zukunft liegt
        LocalDate transactionDate = transaction.getDate().toLocalDate();
        LocalTime transactionTime = transaction.getTime().toLocalTime();

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Check if the transaction date is in the future
        if (transactionDate.isAfter(currentDate) ||
                (transactionDate.equals(currentDate) && transactionTime.isAfter(currentTime))) {
            LoggerUtils.logError(TransactionService.class.getName(), "Die Transaktion kann nicht in der Zukunft liegen.");
            throw new IllegalArgumentException("Transaktionen in der Zukunft sind nicht erlaubt.");
        }

        // Negativer Betrag für Ausgaben erzwingen
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }
        try {
            transactionRepository.saveTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Hinzufügen der Transaktion: " + transaction.getId(), e);
            throw e;
        }
    }



    /**
     * Aktualisiert eine bestehende Transaktion.
     *
     * @param transaction Die zu aktualisierende Transaktion.
     */
    public void updateTransaction(Transaction transaction) {
        if (transaction.getType().equalsIgnoreCase("expense") && transaction.getAmount() > 0) {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        try {
            transactionRepository.updateTransaction(transaction);
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Aktualisieren der Transaktion: " + transaction.getId(), e);
            throw e; // SQLException weiter werfen
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
            throw e; // SQLException weiter werfen
        }
    }

    /**
     * Löscht alle Transaktionen die mit einem bestimmten Account assoziiert werden.
     *
     * @param accountId ID des Accounts
     * @throws SQLException Error Exception
     */
    public void deleteTransactionsByAccount(String accountId) throws SQLException {
        try {
            transactionRepository.deleteTransactionsByAccount(accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Error deleting transactions for account: " + accountId, e);
            throw e;
        }
    }

    /**
     * Ruft die Transaktionen für ein Konto ab.
     *
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
     * Ruft die abgeschlossenen Transaktionen für ein Konto ab.
     *
     * @param accountName Der Name des Kontos.
     * @return Liste der abgeschlossenen Transaktionen.
     */
    public List<Transaction> getCompletedTransactionsByAccount(String accountName) {
        try {
            List<Transaction> transactions = getTransactionsByAccount(accountName);
            // Filtere nur Transaktionen, deren Datum und Zeit in der Vergangenheit liegen
            return transactions.stream()
                    .filter(t -> {
                        LocalDate transactionDate = t.getDate().toLocalDate();
                        LocalTime transactionTime = t.getTime().toLocalTime();

                        // Prüfe, ob das Datum in der Vergangenheit liegt oder das Datum heute ist, aber die Zeit in der Vergangenheit liegt
                        return transactionDate.isBefore(LocalDate.now()) ||
                                (transactionDate.isEqual(LocalDate.now()) && transactionTime.isBefore(LocalTime.now()));
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LoggerUtils.logError(TransactionService.class.getName(), "Fehler beim Abrufen der abgeschlossenen Transaktionen für Konto: " + accountName, e);
            return new ArrayList<>();
        }
    }


    /**
     * Ruft die Transaktionen für eine Kategorie ab.
     *
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
     * Ruft alle Kategorien für einen Benutzer ab.
     *
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
     *
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

    // Neue Methode: Transaktionen für einen Benutzer in einem Zeitraum abrufen
    public List<Transaction> getTransactionsByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTransactionsByUserAndPeriod(userId, startDate, endDate);
    }
}
