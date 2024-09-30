package myProject.repository;

import myProject.model.Budget;
import myProject.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {

    // Methode zum Hinzufügen eines neuen Budgets und Verknüpfen mit mehreren Kategorien in der Datenbank.
    public boolean addBudget(Budget budget) {
        String budgetSql = "INSERT INTO budgets (id, user_id, amount, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        String budgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction


            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.setString(2, budget.getUserId());
                pstmt.setDouble(3, budget.getAmount());
                pstmt.setDate(4, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(5, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.executeUpdate();
            }

            // Budget mit Kategorie verbinden
            try (PreparedStatement pstmt = connection.prepareStatement(budgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode zum Abrufen aller Budgets eines bestimmten Benutzers aus der Datenbank.
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

                // verlinkte Kategorien für ein Budget aufrufen
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return budgets;
    }

    // Methode zum Aktualisieren eines bestehenden Budgets, inklusive der Aktualisierung der zugeordneten Kategorien.
    public boolean updateBudget(Budget budget) {
        String budgetSql = "UPDATE budgets SET amount = ?, start_date = ?, end_date = ? WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";
        String insertBudgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction


            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setDouble(1, budget.getAmount());
                pstmt.setDate(2, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(3, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.setString(4, budget.getId());
                pstmt.executeUpdate();
            }

            // alle Verbindungen mit Kategorien löschen
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetCategoriesSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }

            // neue Kategorien verbinden
            try (PreparedStatement pstmt = connection.prepareStatement(insertBudgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode zum Löschen eines Budgets und seiner zugeordneten Kategorien aus der Datenbank.
    public boolean deleteBudget(String budgetId) {
        String deleteBudgetSql = "DELETE FROM budgets WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction

            // Verbindungen zwischen Budget und Kategorien löschen
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
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}

