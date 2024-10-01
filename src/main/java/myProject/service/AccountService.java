package myProject.service;

import myProject.model.Account;
import myProject.model.Transaction;
import myProject.repository.AccountRepository;

import java.sql.SQLException;
import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    public AccountService(AccountRepository accountRepository, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    // Add a new account for a specific user
    public boolean addAccount(String userId, String name, double balance) {
        Account newAccount = new Account(userId, name, balance);  // Link account to user
        return accountRepository.addAccount(newAccount);
    }

    // Update an existing account
    public boolean updateAccount(Account account) {
        return accountRepository.updateAccount(account);
    }

    public double calculateBalanceForCompletedTransactions(Account account) {
        try {
            // Hole nur abgeschlossene Transaktionen vom TransactionService
            List<Transaction> completedTransactions = transactionService.getCompletedTransactionsByAccount(account.getName());

            // Summiere die abgeschlossenen Transaktionen, um die Bilanz zu berechnen
            double transactionSum = completedTransactions.stream().mapToDouble(Transaction::getAmount).sum();

            // Setze die Bilanz nur basierend auf den abgeschlossenen Transaktionen
            account.setBalance(transactionSum);

            // Speichere die neue Bilanz in der Datenbank
            updateAccount(account);

            return transactionSum; // Gib die neue Bilanz zurück
        } catch (SQLException e) {
            System.err.println("Error calculating account balance: " + e.getMessage());
            return account.getBalance(); // Gib den aktuellen Kontostand zurück, wenn ein Fehler auftritt
        }
    }



    // Get all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        return accountRepository.getAllAccountsForUser(userId);
    }

    // Methode zum Abrufen eines Kontos anhand seines Namens
    public Account findAccountByName(String userId, String accountName) throws SQLException {
        return accountRepository.findAccountByName(userId, accountName);
    }


    // Calculate overall balance for a specific user
    public double calculateOverallBalanceForUser(String userId) throws SQLException {
        return accountRepository.getAllAccountsForUser(userId)
                .stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
