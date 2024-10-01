package myProject.controller;

import myProject.model.Budget;
import myProject.service.BudgetService;
import myProject.util.LoggerUtils;

import java.util.List;

/**
 * Der BudgetController verwaltet die Interaktionen zwischen der Benutzeroberfläche
 * und dem BudgetService. Er stellt Funktionen zum Hinzufügen, Abrufen, Löschen und
 * Aktualisieren von Budgets bereit.
 */
public class BudgetController {
    private final BudgetService budgetService;

    // Konstruktor, um den BudgetService zu initialisieren
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Methode zum Hinzufügen eines neuen Budgets.
     * @param budget Das Budget, das hinzugefügt werden soll.
     */
    public void addBudget(Budget budget) {
        boolean success = budgetService.addBudget(budget);
        if (success) {
            LoggerUtils.logInfo(BudgetController.class.getName(), "Budget erfolgreich hinzugefügt: " + budget.getId());
        } else {
            LoggerUtils.logError(BudgetController.class.getName(), "Fehler beim Hinzufügen des Budgets: " + budget.getId(), null);
        }
    }

    /**
     * Methode zum Abrufen aller Budgets für einen bestimmten Benutzer.
     * @param userId Die ID des Benutzers.
     * @return Liste der Budgets des Benutzers.
     */
    public List<Budget> getBudgetsForUser(String userId) {
        List<Budget> budgets = budgetService.getBudgetsForUser(userId);
        LoggerUtils.logInfo(BudgetController.class.getName(), "Budgets für Benutzer " + userId + " abgerufen.");
        return budgets;
    }

    /**
     * Methode zum Löschen eines Budgets anhand seiner ID.
     * @param budgetId Die ID des zu löschenden Budgets.
     */
    public void deleteBudget(String budgetId) {
        boolean success = budgetService.deleteBudget(budgetId);
        if (success) {
            LoggerUtils.logInfo(BudgetController.class.getName(), "Budget erfolgreich gelöscht: " + budgetId);
        } else {
            LoggerUtils.logError(BudgetController.class.getName(), "Fehler beim Löschen des Budgets: " + budgetId, null);
        }
    }

    /**
     * Methode zum Aktualisieren eines bestehenden Budgets.
     * @param budget Das zu aktualisierende Budget.
     */
    public void updateBudget(Budget budget) {
        boolean success = budgetService.updateBudget(budget);
        if (success) {
            LoggerUtils.logInfo(BudgetController.class.getName(), "Budget erfolgreich aktualisiert: " + budget.getId());
        } else {
            LoggerUtils.logError(BudgetController.class.getName(), "Fehler beim Aktualisieren des Budgets: " + budget.getId(), null);
        }
    }
}
