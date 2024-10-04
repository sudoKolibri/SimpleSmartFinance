package myProject.controller;

import myProject.model.Account;
import myProject.model.Transaction;
import myProject.service.AccountService;
import myProject.service.TransactionService;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * AccountController verwaltet Konten und deren Interaktionen mit dem AccountService und TransactionService.
 */
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    // Konstruktor, um den AccountService und den TransactionService zu initialisieren
    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    /**
     * Methode zum Hinzufügen eines neuen Kontos für einen Benutzer.
     *
     * @param userId Die ID des Benutzers.
     * @param name Der Name des Kontos.
     * @param balance Der Startkontostand.
     * @return true, wenn das Konto erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addAccount(String userId, String name, double balance) {
        boolean isSuccess = accountService.addAccount(userId, name, balance);
        if (isSuccess) {
            LoggerUtils.logInfo(AccountController.class.getName(), "Konto erfolgreich hinzugefügt für Benutzer: " + userId);
        } else {
            LoggerUtils.logInfo(AccountController.class.getName(), "Fehler beim Hinzufügen des Kontos für Benutzer: " + userId);
        }
        return isSuccess;
    }

    /**
     * Löscht einen Account mit der spezifischen ID
     *
     * @param accountId ID des Accounts
     * @throws SQLException Error Exception
     */
    public void deleteAccount(String accountId) throws SQLException {
        try {
            accountService.deleteAccount(accountId);
            LoggerUtils.logInfo(AccountController.class.getName(), "Account deleted: " + accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(AccountController.class.getName(), "Error deleting account: " + accountId, e);
            throw e;
        }
    }

    /**
     * Überprüft, ob ein Konto mit einem bestimmten Namen für den Benutzer existiert.
     *
     * @param userId Die ID des Benutzers.
     * @param accountName Der Name des Kontos.
     * @return true, wenn das Konto existiert, false wenn nicht.
     * @throws SQLException bei einem Datenbankfehler.
     */
    public boolean doesAccountExist(String userId, String accountName) throws SQLException {
        try {
            List<Account> accounts = getAllAccountsForUser(userId);
            boolean exists = accounts.stream().anyMatch(account -> account.getName().equalsIgnoreCase(accountName));
            LoggerUtils.logInfo(AccountController.class.getName(), "Überprüfung abgeschlossen: Konto existiert: " + exists);
            return exists;
        } catch (SQLException e) {
            LoggerUtils.logError(AccountController.class.getName(), "Fehler bei der Überprüfung, ob Konto existiert für Benutzer: " + userId, e);
            throw e;
        }
    }

    /**
     * Methode zum Aktualisieren eines bestehenden Kontos.
     *
     * @param account Das Konto, das aktualisiert werden soll.
     */
    public void updateAccount(Account account) {
        accountService.updateAccount(account);
        LoggerUtils.logInfo(AccountController.class.getName(), "Konto erfolgreich aktualisiert: " + account.getName());
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
            List<Account> accounts = accountService.getAllAccountsForUser(userId);
            LoggerUtils.logInfo(AccountController.class.getName(), "Alle Konten für Benutzer abgerufen: " + userId);
            return accounts;
        } catch (SQLException e) {
            LoggerUtils.logError(AccountController.class.getName(), "Fehler beim Abrufen der Konten für Benutzer: " + userId, e);
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
            Account account = accountService.findAccountByName(userId, accountName);
            LoggerUtils.logInfo(AccountController.class.getName(), "Konto gefunden: " + accountName + " für Benutzer: " + userId);
            return account;
        } catch (SQLException e) {
            LoggerUtils.logError(AccountController.class.getName(), "Fehler beim Finden des Kontos: " + accountName + " für Benutzer: " + userId, e);
            throw e;
        }
    }

    /**
     * Methode zum Berechnen der Gesamtbilanz eines Benutzers.
     *
     * @param userId Die ID des Benutzers.
     * @return Die berechnete Gesamtbilanz.
     * @throws SQLException bei einem Datenbankfehler.
     */
    public double getOverallBalanceForUser(String userId) throws SQLException {
        try {
            double balance = accountService.calculateOverallBalanceForUser(userId);
            LoggerUtils.logInfo(AccountController.class.getName(), "Gesamtbilanz erfolgreich berechnet für Benutzer: " + userId);
            return balance;
        } catch (SQLException e) {
            LoggerUtils.logError(AccountController.class.getName(), "Fehler bei der Berechnung der Gesamtbilanz für Benutzer: " + userId, e);
            throw e;
        }
    }

    /**
     * Methode zum Aktualisieren der Bilanz eines Kontos.
     *
     * @param account Das Konto, dessen Bilanz aktualisiert werden soll.
     */
    public void updateAccountBalance(Account account) {
        accountService.updateAccount(account);
        LoggerUtils.logInfo(AccountController.class.getName(), "Konto-Bilanz erfolgreich aktualisiert: " + account.getName());
    }

    /**
     * Berechnet die Bilanz für abgeschlossene Transaktionen eines Kontos.
     *
     * @param account Das Konto, für das die Bilanz berechnet werden soll.
     * @return Die berechnete Bilanz.
     */
    public double calculateUpdatedBalanceForCompletedTransactions(Account account) throws SQLException {
        List<Transaction> completedTransactions = transactionService.getCompletedTransactionsByAccount(account.getName());

        // Summiere alle abgeschlossenen Transaktionen und berechne die Bilanz
        return completedTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

}
