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
            // Get only completed transactions from TransactionService
            List<Transaction> completedTransactions = transactionService.getCompletedTransactionsByAccount(account.getName());

            // Sum the completed transactions to calculate the balance
            double transactionSum = completedTransactions.stream().mapToDouble(Transaction::getAmount).sum();

            // Calculate the new balance based on the account's initial balance and completed transactions
            double newBalance = account.getBalance() + transactionSum;

            // Persist the updated balance in the database
            account.setBalance(newBalance);
            updateAccount(account); // Persist the updated account balance

            return newBalance;
        } catch (SQLException e) {
            System.err.println("Error calculating account balance: " + e.getMessage());
            return account.getBalance(); // Return current balance if an error occurs
        }
    }


    // Get all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        return accountRepository.getAllAccountsForUser(userId);
    }

    // Calculate overall balance for a specific user
    public double calculateOverallBalanceForUser(String userId) throws SQLException {
        return accountRepository.getAllAccountsForUser(userId)
                .stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
