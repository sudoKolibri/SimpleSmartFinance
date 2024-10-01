package myProject.repository;

import myProject.model.Budget;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Das BudgetRepository ist für die Datenbankzugriffe im Zusammenhang mit Budgets verantwortlich.
 * Hier werden Budgets in die Datenbank eingefügt, abgerufen, aktualisiert und gelöscht.
 */
public class BudgetRepository {

    /**
     * Fügt ein neues Budget hinzu und verknüpft es mit mehreren Kategorien in der Datenbank.
     *
     * @param budget Das hinzuzufügende Budget.
     * @return true, wenn das Budget erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addBudget(Budget budget) {
        String budgetSql = "INSERT INTO budgets (id, user_id, amount, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        String budgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Transaktion starten

            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.setString(2, budget.getUserId());
                pstmt.setDouble(3, budget.getAmount());
                pstmt.setDate(4, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(5, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.executeUpdate();
            }

            // Budget mit Kategorien verknüpfen
            try (PreparedStatement pstmt = connection.prepareStatement(budgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
            LoggerUtils.logInfo(BudgetRepository.class.getName(), "Budget erfolgreich hinzugefügt: " + budget.getId());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(BudgetRepository.class.getName(), "Fehler beim Hinzufügen des Budgets: " + budget.getId(), e);
            return false;
        }
    }

    /**
     * Ruft alle Budgets eines bestimmten Benutzers aus der Datenbank ab.
     *
     * @param userId Die ID des Benutzers, dessen Budgets abgerufen werden sollen.
     * @return Eine Liste der Budgets des Benutzers.
     */
    public List<Budget> getBudgetsByUserId(String userId) {
        String budgetSql = "SELECT * FROM budgets WHERE user_id = ?";
        String categorySql = "SELECT category_id FROM budget_categories WHERE budget_id = ?";
        List<Budget> budgets = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                double amount = rs.getDouble("amount");
                java.sql.Date startDate = rs.getDate("start_date");
                java.sql.Date endDate = rs.getDate("end_date");

                // Verlinkte Kategorien für das Budget abrufen
                List<String> categoryIds = new ArrayList<>();
                try (PreparedStatement categoryStmt = connection.prepareStatement(categorySql)) {
                    categoryStmt.setString(1, id);
                    ResultSet categoryRs = categoryStmt.executeQuery();
                    while (categoryRs.next()) {
                        categoryIds.add(categoryRs.getString("category_id"));
                    }
                }

                budgets.add(new Budget(id, userId, amount, startDate, endDate, categoryIds));
            }

            LoggerUtils.logInfo(BudgetRepository.class.getName(), "Budgets erfolgreich abgerufen für Benutzer: " + userId);

        } catch (SQLException e) {
            LoggerUtils.logError(BudgetRepository.class.getName(), "Fehler beim Abrufen der Budgets für Benutzer: " + userId, e);
        }

        return budgets;
    }

    /**
     * Aktualisiert ein bestehendes Budget, einschließlich der Aktualisierung der zugeordneten Kategorien.
     *
     * @param budget Das zu aktualisierende Budget.
     * @return true, wenn das Budget erfolgreich aktualisiert wurde, false bei einem Fehler.
     */
    public boolean updateBudget(Budget budget) {
        String budgetSql = "UPDATE budgets SET amount = ?, start_date = ?, end_date = ? WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";
        String insertBudgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Transaktion starten

            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setDouble(1, budget.getAmount());
                pstmt.setDate(2, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(3, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.setString(4, budget.getId());
                pstmt.executeUpdate();
            }

            // Alle Verknüpfungen mit Kategorien löschen
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetCategoriesSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }

            // Neue Kategorien verknüpfen
            try (PreparedStatement pstmt = connection.prepareStatement(insertBudgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
            LoggerUtils.logInfo(BudgetRepository.class.getName(), "Budget erfolgreich aktualisiert: " + budget.getId());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(BudgetRepository.class.getName(), "Fehler beim Aktualisieren des Budgets: " + budget.getId(), e);
            return false;
        }
    }

    /**
     * Löscht ein Budget und seine zugeordneten Kategorien aus der Datenbank.
     *
     * @param budgetId Die ID des zu löschenden Budgets.
     * @return true, wenn das Budget erfolgreich gelöscht wurde, false bei einem Fehler.
     */
    public boolean deleteBudget(String budgetId) {
        String deleteBudgetSql = "DELETE FROM budgets WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Transaktion starten

            // Verknüpfungen zwischen Budget und Kategorien löschen
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetCategoriesSql)) {
                pstmt.setString(1, budgetId);
                pstmt.executeUpdate();
            }

            // Budget löschen
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetSql)) {
                pstmt.setString(1, budgetId);
                pstmt.executeUpdate();
            }

            connection.commit();
            LoggerUtils.logInfo(BudgetRepository.class.getName(), "Budget erfolgreich gelöscht: " + budgetId);
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(BudgetRepository.class.getName(), "Fehler beim Löschen des Budgets: " + budgetId, e);
            return false;
        }
    }
}
