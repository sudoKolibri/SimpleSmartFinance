package myProject.service;

import myProject.model.Account;
import myProject.repository.AccountRepository;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Der AccountService verwaltet die Geschäftslogik für Konten.
 * Er bietet Methoden zum Hinzufügen, Aktualisieren und Abrufen von Konten
 * sowie zur Berechnung von Kontobilanzen auf Basis abgeschlossener Transaktionen.
 * Die Klasse interagiert mit dem AccountRepository für Datenbankoperationen und
 * dem TransactionService, um Transaktionen zu verarbeiten.
 */
public class AccountService {

    private final AccountRepository accountRepository;

    // Konstruktor, um AccountRepository und TransactionService zu initialisieren
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Methode zum Hinzufügen eines neuen Kontos für einen bestimmten Benutzer.
     *
     * @param userId Die ID des Benutzers.
     * @param name Der Name des Kontos.
     * @param balance Der Startkontostand.
     * @return true, wenn das Konto erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addAccount(String userId, String name, double balance) {
        Account newAccount = new Account(userId, name, balance);  // Verknüpft das Konto mit dem Benutzer
        try {
            return accountRepository.addAccount(newAccount);
        } catch (Exception e) {
            LoggerUtils.logError(AccountService.class.getName(), "Fehler beim Hinzufügen eines Kontos für Benutzer: " + userId, e);
            return false;
        }
    }

    /**
     * Methode zum Aktualisieren eines bestehenden Kontos.
     *
     * @param account Das Konto, das aktualisiert werden soll.
     */
    public void updateAccount(Account account) {
        try {
            accountRepository.updateAccount(account);  // Aktualisiere das Konto in der Datenbank
        } catch (Exception e) {
            LoggerUtils.logError(AccountService.class.getName(), "Fehler beim Aktualisieren des Kontos: " + account.getName(), e);
        }
    }

    /**
     * Methode zum Abrufen aller Konten eines bestimmten Benutzers.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste von Konten des Benutzers.
     * @throws SQLException bei einem Datenbankfehler.
     */
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        try {
            return accountRepository.getAllAccountsForUser(userId);
        } catch (SQLException e) {
            LoggerUtils.logError(AccountService.class.getName(), "Fehler beim Abrufen der Konten für Benutzer: " + userId, e);
            throw e;
        }
    }

    /**
     * Methode zum Abrufen eines Kontos anhand seines Namens.
     *
     * @param userId Die ID des Benutzers.
     * @param accountName Der Name des Kontos.
     * @return Das Konto mit dem angegebenen Namen, null falls nicht gefunden.
     * @throws SQLException bei einem Datenbankfehler.
     */
    public Account findAccountByName(String userId, String accountName) throws SQLException {
        try {
            return accountRepository.findAccountByName(userId, accountName);
        } catch (SQLException e) {
            LoggerUtils.logError(AccountService.class.getName(), "Fehler beim Finden des Kontos: " + accountName + " für Benutzer: " + userId, e);
            throw e;
        }
    }

    /**
     * Methode zur Berechnung der Gesamtbilanz eines bestimmten Benutzers.
     *
     * @param userId Die ID des Benutzers.
     * @return Die berechnete Gesamtbilanz.
     * @throws SQLException bei einem Datenbankfehler.
     */
    public double calculateOverallBalanceForUser(String userId) throws SQLException {
        try {
            return accountRepository.getAllAccountsForUser(userId)
                    .stream()
                    .mapToDouble(Account::getBalance)
                    .sum();
        } catch (SQLException e) {
            LoggerUtils.logError(AccountService.class.getName(), "Fehler bei der Berechnung der Gesamtbilanz für Benutzer: " + userId, e);
            throw e;
        }
    }
}
