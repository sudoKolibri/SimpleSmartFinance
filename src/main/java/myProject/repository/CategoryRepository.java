package myProject.repository;

import myProject.model.Category;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Das CategoryRepository ist für die Datenbankzugriffe im Zusammenhang mit Kategorien verantwortlich.
 * Es bietet Methoden zum Hinzufügen, Abrufen, Aktualisieren und Löschen von Kategorien.
 */
public class CategoryRepository {

    /**
     * Methode zum Hinzufügen einer neuen Kategorie in die Datenbank. Benutzerdefinierte Kategorien sind mit einem Benutzer verknüpft.
     *
     * @param category Die hinzuzufügende Kategorie.
     * @param userId   Die ID des Benutzers, falls die Kategorie benutzerdefiniert ist.
     * @return true, wenn die Kategorie erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addCategory(Category category, String userId) {
        String sql = "INSERT INTO categories (id, name, budget, user_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());

            if (category.getBudget() != null) {
                pstmt.setDouble(3, category.getBudget());
            } else {
                pstmt.setNull(3, java.sql.Types.DOUBLE);
            }

            pstmt.setString(4, userId);

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
     *
     * @param category Die zu aktualisierende Kategorie.
     * @return true, wenn die Kategorie erfolgreich aktualisiert wurde, false bei einem Fehler.
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, budget = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            if (category.getBudget() != null) {
                pstmt.setDouble(2, category.getBudget());
            } else {
                pstmt.setNull(2, java.sql.Types.DOUBLE);  // Set NULL if the budget is not provided
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


    // Methode zum Löschen einer Kategorie
    public boolean deleteCategory(String categoryId) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);
            pstmt.executeUpdate();

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Kategorie erfolgreich gelöscht: " + categoryId);
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Löschen der Kategorie: " + categoryId, e);
            return false;
        }
    }

    public void updateTransactionsToNoCategory(String categoryId, String userId) {
        // Ermitteln der "No Category" ID für den Benutzer
        String noCategoryId = findNoCategoryIdForUser(userId);
        if (noCategoryId == null) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Keine 'No Category' Kategorie gefunden für Benutzer: " + userId, null);
            return;
        }

        String sql = "UPDATE transactions SET category_id = ? WHERE category_id = ? AND account_id IN (SELECT id FROM accounts WHERE user_id = ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, noCategoryId);
            pstmt.setString(2, categoryId);
            pstmt.setString(3, userId);
            pstmt.executeUpdate();

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Transaktionen erfolgreich auf 'No Category' gesetzt für Kategorie: " + categoryId);

        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Aktualisieren der Transaktionen für Kategorie: " + categoryId, e);
        }
    }

    private String findNoCategoryIdForUser(String userId) {
        String sql = "SELECT id FROM categories WHERE name = 'No Category' AND user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Finden der 'No Category' ID für Benutzer: " + userId, e);
        }
        return null;
    }



    /**
     * Methode zum Abrufen einer Kategorie anhand ihrer ID.
     *
     * @param categoryId Die ID der Kategorie.
     * @return Die gefundene Kategorie oder null, wenn sie nicht gefunden wird.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt.
     */
    public Category findCategoryById(String categoryId) throws SQLException {
        if (categoryId == null) return null;
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

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
     * Findet eine Kategorie anhand ihres Namens für einen bestimmten Benutzer.
     *
     * @param userId       Die ID des Benutzers.
     * @param categoryName Der Name der Kategorie.
     * @return Die gefundene Kategorie oder null, wenn keine Kategorie gefunden wurde.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public Category findCategoryByName(String userId, String categoryName) throws SQLException {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND name = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, categoryName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der Kategorie mit Namen: " + categoryName + " für Benutzer: " + userId, e);
            throw e;
        }
        return null;
    }



    /**
     * Ruft alle Kategorien (globale und benutzerdefinierte) für einen bestimmten Benutzer ab.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Kategorien des Benutzers.
     */
    public List<Category> getAllCategoriesForUser(String userId) {
        String sql = "SELECT * FROM categories WHERE user_id = ?";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }

            LoggerUtils.logInfo(CategoryRepository.class.getName(), "Kategorien erfolgreich abgerufen für Benutzer: " + userId);
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der Kategorien für Benutzer: " + userId, e);
        }

        return categories;
    }


    /**
     * Berechnet die Gesamtausgaben für eine bestimmte Kategorie innerhalb eines angegebenen Zeitraums.
     *
     * @param categoryId Die ID der Kategorie.
     * @param startDate  Das Startdatum des Zeitraums.
     * @param endDate    Das Enddatum des Zeitraums.
     * @return Die Gesamtsumme der Ausgaben für die Kategorie in dem Zeitraum.
     */
    public double getSpentAmountForCategoryInPeriod(String categoryId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(amount) AS totalSpent FROM transactions WHERE category_id = ? AND date >= ? AND date <= ? AND type = 'expense'";
        double totalSpent = 0;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalSpent = rs.getDouble("totalSpent");
                }
            }
        } catch (SQLException e) {
            LoggerUtils.logError(CategoryRepository.class.getName(), "Fehler beim Abrufen der Ausgaben für Kategorie: " + categoryId + " im Zeitraum von " + startDate + " bis " + endDate, e);
        }

        return totalSpent;
    }

    /**
     * Hilfsmethode zum Mapping eines ResultSet auf ein Category-Objekt.
     *
     * @param rs Das ResultSet der SQL-Abfrage.
     * @return Die abgebildete Kategorie.
     * @throws SQLException Wenn ein Fehler bei der Abfrage auftritt.
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(rs.getString("id"), rs.getString("name"), rs.getObject("budget") != null ? rs.getDouble("budget") : null);
    }
}
