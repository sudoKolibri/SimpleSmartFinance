package myProject.service;

import myProject.model.Category;
import myProject.repository.CategoryRepository;
import myProject.util.LoggerUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Der CategoryService verwaltet die Geschäftslogik für Kategorien.
 * Er bietet Methoden zum Hinzufügen, Aktualisieren und Abrufen von Kategorien
 * und interagiert mit dem CategoryRepository für Datenbankoperationen.
 */
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // Konstruktor mit Dependency Injection
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Fügt eine neue Kategorie hinzu.
     * @param category Die Kategorie, die hinzugefügt werden soll.
     * @param userId Die ID des Benutzers, der die Kategorie hinzufügt.
     * @return true, wenn das Hinzufügen erfolgreich war, false bei einem Fehler.
     */
    public boolean addCategory(Category category, String userId) {
        try {
            boolean success = categoryRepository.addCategory(category, userId);
            if (success) {
                LoggerUtils.logInfo(CategoryService.class.getName(), "Kategorie erfolgreich hinzugefügt: " + category.getName() + " für Benutzer: " + userId);
            }
            return success;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Hinzufügen der Kategorie: " + category.getName() + " für Benutzer: " + userId, e);
            return false;
        }
    }

    /**
     * Aktualisiert eine bestehende Kategorie.
     * @param category Die zu aktualisierende Kategorie.
     * @return true, wenn die Aktualisierung erfolgreich war, false bei einem Fehler.
     */
    public boolean updateCategory(Category category) {
        try {
            boolean success = categoryRepository.updateCategory(category);
            if (success) {
                LoggerUtils.logInfo(CategoryService.class.getName(), "Kategorie erfolgreich aktualisiert: " + category.getName());
            }
            return success;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Aktualisieren der Kategorie: " + category.getName(), e);
            return false;
        }
    }


    /**
     * Löscht eine Kategorie und aktualisiert alle zugehörigen Transaktionen auf die "No Category" des Benutzers.
     *
     * @param categoryId Die ID der zu löschenden Kategorie.
     * @param userId     Die ID des Benutzers.
     * @return true, wenn das Löschen und Aktualisieren erfolgreich war, false bei einem Fehler.
     */
    public boolean deleteCategoryAndUpdateTransactions(String categoryId, String userId) {
        try {
            // Aktualisiere Transaktionen auf die "No Category" des Benutzers
            categoryRepository.updateTransactionsToNoCategory(categoryId, userId);

            // Lösche die Kategorie
            boolean success = categoryRepository.deleteCategory(categoryId);
            if (success) {
                LoggerUtils.logInfo(CategoryService.class.getName(), "Kategorie erfolgreich gelöscht und Transaktionen aktualisiert: " + categoryId);
            }
            return success;

        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Löschen der Kategorie und Aktualisieren der Transaktionen: " + categoryId, e);
            return false;
        }
    }


    /**
     * Ruft alle Kategorien für einen bestimmten Benutzer ab.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Kategorien des Benutzers.
     */
    public List<Category> getAllCategoriesForUser(String userId) {
        return categoryRepository.getAllCategoriesForUser(userId);
    }


    /**
     * Ruft eine Kategorie anhand ihres Namens für einen bestimmten Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @param categoryName Der Name der Kategorie.
     * @return Die gefundene Kategorie oder null, wenn keine Kategorie gefunden wurde.
     */
    public Category getCategoryByName(String userId, String categoryName) {
        try {
            Category category = categoryRepository.findCategoryByName(userId, categoryName);
            if (category != null) {
                LoggerUtils.logInfo(CategoryService.class.getName(), "Kategorie erfolgreich abgerufen: " + categoryName + " für Benutzer: " + userId);
            } else {
                LoggerUtils.logInfo(CategoryService.class.getName(), "Keine Kategorie gefunden mit Namen: " + categoryName + " für Benutzer: " + userId);
            }
            return category;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Abrufen der Kategorie: " + categoryName + " für Benutzer: " + userId, e);
            return null;
        }
    }



    /**
     * Berechnet den Budgetfortschritt für alle Kategorien eines Benutzers in einem bestimmten Zeitraum.
     *
     * @param userId Die ID des Benutzers.
     * @param startDate Das Startdatum des Zeitraums.
     * @param endDate Das Enddatum des Zeitraums.
     * @return Map mit Kategorien als Schlüssel und ihrem Budgetfortschritt als Werte.
     */
    public Map<Category, Double> getCategoryBudgetProgress(String userId, LocalDate startDate, LocalDate endDate) {
        List<Category> categories = categoryRepository.getAllCategoriesForUser(userId);
        Map<Category, Double> budgetProgress = new HashMap<>();

        for (Category category : categories) {
            if (category.getBudget() != null) {

                double spent = Math.abs(categoryRepository.getSpentAmountForCategoryInPeriod(category.getId(), startDate, endDate));

                budgetProgress.put(category, spent);
            }
        }
        return budgetProgress;
    }

}

