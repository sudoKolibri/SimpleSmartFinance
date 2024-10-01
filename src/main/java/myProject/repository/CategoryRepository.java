package myProject.repository;

import myProject.model.Category;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Das CategoryRepository ist für die Datenbankzugriffe im Zusammenhang mit Kategorien verantwortlich.
 * Es bietet Methoden zum Hinzufügen, Abrufen, Aktualisieren und Löschen von Kategorien.
 */
public class CategoryRepository {

    /**
     * Methode zum Hinzufügen einer neuen Kategorie in die Datenbank. Benutzerdefinierte Kategorien sind mit einem Benutzer verknüpft.
     * @param category Die hinzuzufügende Kategorie.
     * @param userId Die ID des Benutzers, falls die Kategorie benutzerdefiniert ist.
     * @return true, wenn die Kategorie erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addCategory(Category category, String userId) {
        String sql = "INSERT INTO categories (id, name, is_standard, is_custom, budget, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setBoolean(3, category.isStandard());
            pstmt.setBoolean(4, category.isCustom());

            if (category.getBudget() != null) {
                pstmt.setDouble(5, category.getBudget());
            } else {
                pstmt.setNull(5, java.sql.Types.DOUBLE);
            }

            pstmt.setString(6, category.isCustom() ? userId : null);

            pstmt.executeUpdate();

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Kategorie erfolgreich hinzugefügt: " + category.getName());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Hinzufügen der Kategorie: " + category.getName(), e);
            return false;
        }
    }

    /**
     * Methode zum Aktualisieren einer bestehenden Kategorie in der Datenbank.
     * @param category Die zu aktualisierende Kategorie.
     * @return true, wenn die Kategorie erfolgreich aktualisiert wurde, false bei einem Fehler.
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, budget = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            if (category.getBudget() != null) {
                pstmt.setDouble(2, category.getBudget());
            } else {
                pstmt.setNull(2, java.sql.Types.DOUBLE);
            }
            pstmt.setString(3, category.getId());

            pstmt.executeUpdate();

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Kategorie erfolgreich aktualisiert: " + category.getName());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Aktualisieren der Kategorie: " + category.getName(), e);
            return false;
        }
    }

    /**
     * Methode zum Abrufen aller globalen (nicht benutzerdefinierten) Kategorien aus der Datenbank.
     * @return Eine Liste der globalen Kategorien.
     */
    public List<Category> getGlobalCategories() {
        String sql = "SELECT * FROM categories WHERE user_id IS NULL AND is_custom = false";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Globale Kategorien erfolgreich abgerufen.");
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der globalen Kategorien.", e);
        }

        return categories;
    }

    /**
     * Methode zum Abrufen aller Standardkategorien aus der Datenbank.
     * @return Eine Liste der Standardkategorien.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt.
     */
    public List<Category> getStandardCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_standard = true";

        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Standardkategorien erfolgreich abgerufen.");
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der Standardkategorien.", e);
            throw e;
        }

        return categories;
    }

    /**
     * Methode zum Abrufen benutzerdefinierter Kategorien für einen bestimmten Benutzer.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste der benutzerdefinierten Kategorien.
     */
    public List<Category> getCustomCategoriesForUser(String userId) {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND is_custom = true";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Benutzerdefinierte Kategorien erfolgreich abgerufen für Benutzer: " + userId);
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der benutzerdefinierten Kategorien für Benutzer: " + userId, e);
        }

        return categories;
    }

    /**
     * Methode zum Abrufen einer Kategorie anhand ihrer ID.
     * @param categoryId Die ID der Kategorie.
     * @return Die gefundene Kategorie oder null, wenn sie nicht gefunden wird.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt.
     */
    public Category findCategoryById(String categoryId) throws SQLException {
        if (categoryId == null) return null;
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LoggerUtils.logInfo(CategoryRepository.class.getName(), "Kategorie erfolgreich gefunden: " + categoryId);
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der Kategorie mit ID: " + categoryId, e);
            throw e;
        }

        return null;
    }

    /**
     * Hilfsmethode zum Mapping eines ResultSet auf ein Category-Objekt.
     * @param rs Das ResultSet der SQL-Abfrage.
     * @return Die abgebildete Kategorie.
     * @throws SQLException Wenn ein Fehler bei der Abfrage auftritt.
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(rs.getString("id"), rs.getString("name"), rs.getBoolean("is_standard"),
                rs.getBoolean("is_custom"), rs.getObject("budget") != null ? rs.getDouble("budget") : null);
    }
}
