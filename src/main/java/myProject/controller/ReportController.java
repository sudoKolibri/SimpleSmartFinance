package myProject.controller;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.service.AccountService;
import myProject.service.BudgetService;
import myProject.service.CategoryService;
import myProject.service.TransactionService;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;

    // Constructor initializing the controller with necessary services
    public ReportController(TransactionService transactionService, AccountService accountService,
                            CategoryService categoryService, BudgetService budgetService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
    }

    // Fetch and process data for the Category Spending Analysis (Pie Chart)
    public Map<String, Double> getCategorySpendingData(String userId) {
        Map<String, Double> spendingData = new HashMap<>();
        try {
            // Fetch all categories for the user
            List<Category> categories = categoryService.getAllCategoriesForUser(userId);

            // Calculate spending for each category
            for (Category category : categories) {
                // Fetch both regular and recurring transactions for the category
                List<Transaction> regularTransactions = transactionService.getTransactionsByCategory(category);
                List<Transaction> recurringTransactions = transactionService.getRecurringTransactionsByCategory(category);

                // Combine both lists and calculate total spent
                double totalSpent = regularTransactions.stream().mapToDouble(Transaction::getAmount).sum() +
                        recurringTransactions.stream().mapToDouble(Transaction::getAmount).sum();

                spendingData.put(category.getName(), totalSpent);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category spending data: " + e.getMessage());
        }
        return spendingData;
    }

    // Fetch and process data for Budget Analysis (Bar Chart)
    public Map<String, Double> getBudgetAnalysisData(String userId) {
        Map<String, Double> budgetAnalysisData = new HashMap<>();
        try {
            // Fetch categories and compare budgeted vs actual spending
            List<Category> categories = categoryService.getAllCategoriesForUser(userId);
            for (Category category : categories) {
                // Get the budgeted amount, defaulting to 0 if null
                Double budget = category.getBudget() != null ? category.getBudget() : 0.0;

                // Fetch both regular and recurring transactions for the category
                List<Transaction> regularTransactions = transactionService.getTransactionsByCategory(category);
                List<Transaction> recurringTransactions = transactionService.getRecurringTransactionsByCategory(category);

                // Calculate the actual spending by summing amounts from both types of transactions
                double actualSpending = regularTransactions.stream().mapToDouble(Transaction::getAmount).sum() +
                        recurringTransactions.stream().mapToDouble(Transaction::getAmount).sum();

                // Store the difference between actual spending and budget
                budgetAnalysisData.put(category.getName(), actualSpending - budget);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching budget analysis data: " + e.getMessage());
        }
        return budgetAnalysisData;
    }

    // Fetch and process data for Income vs Expense Analysis (Bar Chart)
    public Map<String, Double> getIncomeVsExpenseData(String userId) {
        Map<String, Double> incomeExpenseData = new HashMap<>();
        try {
            // Fetch all transactions and categorize into income and expense
            List<Transaction> transactions = transactionService.getAllTransactions();
            double totalIncome = transactions.stream()
                    .filter(t -> t.getType().equalsIgnoreCase("income"))
                    .mapToDouble(Transaction::getAmount).sum();
            double totalExpense = transactions.stream()
                    .filter(t -> t.getType().equalsIgnoreCase("expense"))
                    .mapToDouble(Transaction::getAmount).sum();

            incomeExpenseData.put("Income", totalIncome);
            incomeExpenseData.put("Expense", totalExpense);
        } catch (SQLException e) {
            System.err.println("Error fetching income vs expense data: " + e.getMessage());
        }
        return incomeExpenseData;
    }

    // Fetch and process data for Balance Over Time Analysis (Line Chart)
    public List<Double> getBalanceOverTimeData(String userId) {
        List<Double> balanceOverTime = new ArrayList<>();
        try {
            // Fetch all accounts for the user and sort transactions by date to calculate balance changes
            List<Account> accounts = accountService.getAllAccountsForUser(userId);
            Map<Date, Double> balanceHistory = new TreeMap<>();

            // Fetch transactions for all accounts and map their balances over time
            for (Account account : accounts) {
                List<Transaction> transactions = transactionService.getTransactionsByAccount(account.getName());
                double currentBalance = account.getBalance();

                for (Transaction transaction : transactions) {
                    Date date = transaction.getDate();
                    double amount = transaction.getType().equalsIgnoreCase("income") ? transaction.getAmount() : -transaction.getAmount();
                    balanceHistory.put(date, balanceHistory.getOrDefault(date, currentBalance) + amount);
                }
            }

            // Convert the map to a list of balances sorted by date
            balanceOverTime.addAll(balanceHistory.values());
        } catch (SQLException e) {
            System.err.println("Error fetching balance over time data: " + e.getMessage());
        }
        return balanceOverTime;
    }

    // Fetch data for a detailed report view
    public List<Transaction> getDetailedReportData(String userId, String category) {
        try {
            // Fetch transactions for a specific category
            Category categoryObj = categoryService.getAllCategoriesForUser(userId).stream()
                    .filter(cat -> cat.getName().equalsIgnoreCase(category))
                    .findFirst()
                    .orElse(null);
            if (categoryObj != null) {
                return transactionService.getTransactionsByCategory(categoryObj);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching detailed report data: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
