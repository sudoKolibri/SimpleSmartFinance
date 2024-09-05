package myProject.service;

import myProject.model.Account;
import myProject.repository.AccountRepository;

import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService() {
        this.accountRepository = new AccountRepository();
    }

    // Add a new account for a specific user
    public boolean addAccount(String userId, String name, double balance) {
        Account newAccount = new Account(userId, name, balance);  // Link account to user
        return accountRepository.addAccount(newAccount);
    }

    // Get all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) {
        return accountRepository.getAllAccountsForUser(userId);
    }

    // Calculate overall balance for a specific user
    public double calculateOverallBalanceForUser(String userId) {
        return accountRepository.getAllAccountsForUser(userId)
                .stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
