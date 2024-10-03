package myProject.service;

import myProject.model.Category;
import myProject.repository.CategoryRepository;
import myProject.util.LoggerUtils;

import java.util.List;

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

    public boolean deleteCategoryAndUpdateTransactions(String categoryId) {
        try {
            // Überprüfe, ob es sich um eine globale Kategorie handelt
            Category category = categoryRepository.findCategoryById(categoryId);
            if (category.isStandard()) {
                LoggerUtils.logError(CategoryService.class.getName(), "Globale Kategorie kann nicht gelöscht werden: " + categoryId);
                return false;  // Verhindert das Löschen der Kategorie
            }

            // Aktualisiere alle Transaktionen der Kategorie auf 'No Category'
            categoryRepository.updateTransactionsToNoCategory(categoryId);

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
     * Ruft alle globalen Kategorien ab.
     * @return Eine Liste globaler Kategorien.
     */
    public List<Category> getGlobalCategories() {
        try {
            List<Category> globalCategories = categoryRepository.getGlobalCategories();
            LoggerUtils.logInfo(CategoryService.class.getName(), "Globale Kategorien erfolgreich abgerufen.");
            return globalCategories;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Abrufen der globalen Kategorien.", e);
            return null;
        }
    }

    /**
     * Ruft alle benutzerdefinierten Kategorien für einen bestimmten Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste benutzerdefinierter Kategorien.
     */
    public List<Category> getCustomCategoriesForUser(String userId) {
        try {
            List<Category> customCategories = categoryRepository.getCustomCategoriesForUser(userId);
            LoggerUtils.logInfo(CategoryService.class.getName(), "Benutzerdefinierte Kategorien erfolgreich abgerufen für Benutzer: " + userId);
            return customCategories;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Abrufen der benutzerdefinierten Kategorien für Benutzer: " + userId, e);
            return null;
        }
    }

    /**
     * Ruft alle Kategorien (globale und benutzerdefinierte) für einen bestimmten Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Kategorien des Benutzers.
     */
    public List<Category> getAllCategoriesForUser(String userId) {
        try {
            List<Category> globalCategories = getGlobalCategories();
            List<Category> customCategories = getCustomCategoriesForUser(userId);
            globalCategories.addAll(customCategories);
            return globalCategories;
        } catch (Exception e) {
            LoggerUtils.logError(CategoryService.class.getName(), "Fehler beim Abrufen aller Kategorien für Benutzer: " + userId, e);
            return null;
        }
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
}
