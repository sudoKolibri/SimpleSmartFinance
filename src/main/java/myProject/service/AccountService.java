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

    // Update an existing account using business logic
    public boolean updateAccount(Account account) {
        // Hier könnte zusätzliche Logik hinzugefügt werden, z.B. Validierung
        System.out.println("AccountService.updateAccount: Updating account balance for account - " + account.getName());
        return accountRepository.updateAccount(account);  // Rufe die Repository-Methode auf
    }

    public double calculateBalanceForCompletedTransactions(Account account) {
        try {
            System.out.println("Starting balance calculation for account: " + account.getName());
            List<Transaction> completedTransactions = transactionService.getCompletedTransactionsByAccount(account.getName());

            // Summiere die abgeschlossenen Transaktionen, um die Bilanz zu berechnen
            double transactionSum = completedTransactions.stream().mapToDouble(Transaction::getAmount).sum();

            // Log the transaction sum before updating the account balance
            System.out.println("Calculated transaction sum: " + transactionSum);

            // Setze die Bilanz nur basierend auf den abgeschlossenen Transaktionen
            account.setBalance(transactionSum);
            updateAccount(account);

            System.out.println("Balance updated for account: " + account.getName() + ", New Balance: " + transactionSum);

            return transactionSum;
        } catch (SQLException e) {
            System.err.println("Error calculating account balance for account: " + account.getName() + " - " + e.getMessage());
            return account.getBalance();
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
