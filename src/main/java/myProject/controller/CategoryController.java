package myProject.controller;

import myProject.model.Category;
import myProject.service.CategoryService;
import myProject.util.LoggerUtils;

import java.util.List;

/**
 * Der CategoryController verwaltet die Interaktionen zwischen der Benutzeroberfläche
 * und dem CategoryService. Er bietet Funktionen zum Hinzufügen, Aktualisieren und
 * Abrufen von Kategorien.
 */
public class CategoryController {
    private final CategoryService categoryService;

    // Konstruktor mit Dependency Injection
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Methode zum Hinzufügen einer neuen Kategorie.
     *
     * @param category Die Kategorie, die hinzugefügt werden soll.
     * @param userId Die ID des Benutzers, der die Kategorie hinzufügt.
     */
    public void addCategory(Category category, String userId) {
        boolean success = categoryService.addCategory(category, userId);
        if (success) {
            LoggerUtils.logInfo(CategoryController.class.getName(), "Kategorie erfolgreich hinzugefügt: " + category.getName() + " für Benutzer: " + userId);
        } else {
            LoggerUtils.logError(CategoryController.class.getName(), "Fehler beim Hinzufügen der Kategorie: " + category.getName() + " für Benutzer: " + userId, null);
        }
    }

    /**
     * Methode zum Aktualisieren einer bestehenden Kategorie.
     *
     * @param category Die Kategorie, die aktualisiert werden soll.
     */
    public void updateCategory(Category category) {
        boolean success = categoryService.updateCategory(category);
        if (success) {
            LoggerUtils.logInfo(CategoryController.class.getName(), "Kategorie erfolgreich aktualisiert: " + category.getName());
        } else {
            LoggerUtils.logError(CategoryController.class.getName(), "Fehler beim Aktualisieren der Kategorie: " + category.getName(), null);
        }
    }

    /**
     * Methode zum Abrufen aller globalen Kategorien.
     *
     * @return Eine Liste globaler Kategorien.
     */
    public List<Category> getGlobalCategories() {
        List<Category> categories = categoryService.getGlobalCategories();
        LoggerUtils.logInfo(CategoryController.class.getName(), "Globale Kategorien erfolgreich abgerufen.");
        return categories;
    }

    /**
     * Methode zum Abrufen aller benutzerdefinierten Kategorien für einen bestimmten Benutzer.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste benutzerdefinierter Kategorien.
     */
    public List<Category> getCustomCategoriesForUser(String userId) {
        List<Category> categories = categoryService.getCustomCategoriesForUser(userId);
        LoggerUtils.logInfo(CategoryController.class.getName(), "Benutzerdefinierte Kategorien erfolgreich abgerufen für Benutzer: " + userId);
        return categories;
    }

    /**
     * Methode zum Abrufen aller Kategorien für einen bestimmten Benutzer.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Kategorien des Benutzers.
     */
    public List<Category> getAllCategoriesForUser(String userId) {
        List<Category> categories = categoryService.getAllCategoriesForUser(userId);
        LoggerUtils.logInfo(CategoryController.class.getName(), "Alle Kategorien erfolgreich abgerufen für Benutzer: " + userId);
        return categories;
    }

    /**
     * Methode zum Abrufen einer Kategorie anhand ihrer ID.
     *
     * @param categoryId Die ID der Kategorie.
     * @param userId Die ID des Benutzers.
     * @return Die Kategorie, falls vorhanden, sonst null.
     */
    public Category getCategoryById(String categoryId, String userId) {
        List<Category> allCategories = getAllCategoriesForUser(userId);
        Category category = allCategories.stream()
                .filter(cat -> cat.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
        if (category != null) {
            LoggerUtils.logInfo(CategoryController.class.getName(), "Kategorie gefunden: " + category.getName());
        } else {
            LoggerUtils.logError(CategoryController.class.getName(), "Kategorie mit ID: " + categoryId + " nicht gefunden für Benutzer: " + userId, null);
        }
        return category;
    }

    /**
     * Methode zum Überprüfen, ob ein Kategoriename bereits existiert.
     *
     * @param name Der zu überprüfende Kategoriename.
     * @param currentCategoryId Die ID der aktuellen Kategorie, um sich selbst auszuschließen.
     * @return true, wenn der Name ein Duplikat ist, false ansonsten.
     */
    public boolean isCategoryNameDuplicate(String name, String currentCategoryId) {
        List<Category> allCategories = categoryService.getAllCategoriesForUser(null); // Korrigiere den Benutzer, falls notwendig
        boolean isDuplicate = allCategories.stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(name) && !category.getId().equals(currentCategoryId));
        LoggerUtils.logInfo(CategoryController.class.getName(), "Überprüfung abgeschlossen: Kategorie ist ein Duplikat: " + isDuplicate);
        return isDuplicate;
    }
}
