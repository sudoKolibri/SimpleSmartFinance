package myProject.service;

import myProject.model.Budget;
import myProject.repository.BudgetRepository;

import java.util.List;

public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    // Add a new budget
    public boolean addBudget(Budget budget) {
        return budgetRepository.addBudget(budget);
    }

    // Get all budgets for a specific user
    public List<Budget> getBudgetsForUser(String userId) {
        return budgetRepository.getBudgetsByUserId(userId);
    }

    // Delete a budget by its ID
    public boolean deleteBudget(String budgetId) {
        return budgetRepository.deleteBudget(budgetId);
    }

    // Update an existing budget
    public boolean updateBudget(Budget budget) {
        return budgetRepository.updateBudget(budget);
    }
}
