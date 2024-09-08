package myProject.controller;

import myProject.model.Budget;
import myProject.service.BudgetService;

import java.util.List;

public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Add a new budget
    public boolean addBudget(Budget budget) {
        return budgetService.addBudget(budget);
    }

    // Get all budgets for a specific user
    public List<Budget> getBudgetsForUser(String userId) {
        return budgetService.getBudgetsForUser(userId);
    }

    // Delete a budget by its ID
    public boolean deleteBudget(String budgetId) {
        return budgetService.deleteBudget(budgetId);
    }

    // Update an existing budget
    public boolean updateBudget(Budget budget) {
        return budgetService.updateBudget(budget);
    }
}
