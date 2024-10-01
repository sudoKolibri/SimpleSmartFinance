package myProject.service;

import myProject.model.Budget;
import myProject.repository.BudgetRepository;
import myProject.util.LoggerUtils;

import java.util.List;

/**
 * Der BudgetService ist für die Geschäftslogik im Zusammenhang mit Budgets verantwortlich.
 * Er arbeitet mit dem BudgetRepository zusammen, um Budgets zu erstellen, zu aktualisieren, zu löschen und abzurufen.
 */
public class BudgetService {
    private final BudgetRepository budgetRepository;

    // Konstruktor, um das BudgetRepository zu initialisieren
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Fügt ein neues Budget hinzu.
     * @param budget Das Budget, das hinzugefügt werden soll.
     * @return true, wenn das Hinzufügen erfolgreich war, false bei einem Fehler.
     */
    public boolean addBudget(Budget budget) {
        try {
            return budgetRepository.addBudget(budget);
        } catch (Exception e) {
            LoggerUtils.logError(BudgetService.class.getName(), "Fehler beim Hinzufügen des Budgets: " + budget.getId(), e);
            return false;
        }
    }

    /**
     * Ruft alle Budgets eines bestimmten Benutzers ab.
     * @param userId Die ID des Benutzers.
     * @return Liste der Budgets des Benutzers.
     */
    public List<Budget> getBudgetsForUser(String userId) {
        try {
            return budgetRepository.getBudgetsByUserId(userId);
        } catch (Exception e) {
            LoggerUtils.logError(BudgetService.class.getName(), "Fehler beim Abrufen der Budgets für Benutzer: " + userId, e);
            return null;
        }
    }

    /**
     * Löscht ein Budget anhand seiner ID.
     * @param budgetId Die ID des zu löschenden Budgets.
     * @return true, wenn das Löschen erfolgreich war, false bei einem Fehler.
     */
    public boolean deleteBudget(String budgetId) {
        try {
            return budgetRepository.deleteBudget(budgetId);
        } catch (Exception e) {
            LoggerUtils.logError(BudgetService.class.getName(), "Fehler beim Löschen des Budgets: " + budgetId, e);
            return false;
        }
    }

    /**
     * Aktualisiert ein bestehendes Budget.
     * @param budget Das zu aktualisierende Budget.
     * @return true, wenn die Aktualisierung erfolgreich war, false bei einem Fehler.
     */
    public boolean updateBudget(Budget budget) {
        try {
            return budgetRepository.updateBudget(budget);
        } catch (Exception e) {
            LoggerUtils.logError(BudgetService.class.getName(), "Fehler beim Aktualisieren des Budgets: " + budget.getId(), e);
            return false;
        }
    }
}
