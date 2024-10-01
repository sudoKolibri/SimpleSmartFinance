package myProject.controller;

import myProject.model.Account;
import myProject.service.AccountService;

import java.sql.SQLException;
import java.util.List;

public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Add a new account (requires userId as a parameter)
    public boolean addAccount(String userId, String name, double balance) {
        return accountService.addAccount(userId, name, balance);
    }

    // Check if an account exists
    public boolean doesAccountExist(String userId, String accountName) throws SQLException {
        List<Account> accounts = getAllAccountsForUser(userId);
        return accounts.stream().anyMatch(account -> account.getName().equalsIgnoreCase(accountName));
    }

    // Update an existing account
    public boolean updateAccount(Account account) {
        return accountService.updateAccount(account);
    }

    // Get the list of all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        return accountService.getAllAccountsForUser(userId);
    }

    // Methode zum Abrufen eines Kontos anhand seines Namens
    public Account findAccountByName(String userId, String accountName) throws SQLException {
        return accountService.findAccountByName(userId, accountName);
    }


    // Calculate the overall balance for a specific user
    public double getOverallBalanceForUser(String userId) throws SQLException {
        return accountService.calculateOverallBalanceForUser(userId);
    }

    public void updateAccountBalance(Account account) {
        // Ensure the balance is persisted in the database via the service layer
        accountService.updateAccount(account);
    }

    public double calculateUpdatedBalanceForCompletedTransactions(Account account) {
        return accountService.calculateBalanceForCompletedTransactions(account);
    }





}
