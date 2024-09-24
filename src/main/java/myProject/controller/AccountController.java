package myProject.controller;

import myProject.model.Account;
import myProject.service.AccountService;

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

    // Update an existing account
    public boolean updateAccount(Account account) {
        return accountService.updateAccount(account);
    }

    // Get the list of all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) {
        return accountService.getAllAccountsForUser(userId);
    }

    // Calculate the overall balance for a specific user
    public double getOverallBalanceForUser(String userId) {
        return accountService.calculateOverallBalanceForUser(userId);
    }
}
