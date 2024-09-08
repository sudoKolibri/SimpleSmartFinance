package myProject.repository;

import myProject.model.Budget;
import myProject.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {

    // Add a new budget and link it to multiple categories
    public boolean addBudget(Budget budget) {
        String budgetSql = "INSERT INTO budgets (id, user_id, amount, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        String budgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction

            // Insert the budget
            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.setString(2, budget.getUserId());
                pstmt.setDouble(3, budget.getAmount());
                pstmt.setDate(4, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(5, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.executeUpdate();
            }

            // Link the budget to multiple categories
            try (PreparedStatement pstmt = connection.prepareStatement(budgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();  // Execute all category links
            }

            connection.commit();  // Commit the transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all budgets for a user
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

                // Fetch linked categories for this budget
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

    // Update an existing budget
    public boolean updateBudget(Budget budget) {
        String budgetSql = "UPDATE budgets SET amount = ?, start_date = ?, end_date = ? WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";
        String insertBudgetCategorySql = "INSERT INTO budget_categories (budget_id, category_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction

            // Update the budget itself
            try (PreparedStatement pstmt = connection.prepareStatement(budgetSql)) {
                pstmt.setDouble(1, budget.getAmount());
                pstmt.setDate(2, new java.sql.Date(budget.getStartDate().getTime()));
                pstmt.setDate(3, new java.sql.Date(budget.getEndDate().getTime()));
                pstmt.setString(4, budget.getId());
                pstmt.executeUpdate();
            }

            // Delete all existing category links for the budget
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetCategoriesSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }

            // Insert new category links for the updated budget
            try (PreparedStatement pstmt = connection.prepareStatement(insertBudgetCategorySql)) {
                for (String categoryId : budget.getCategoryIds()) {
                    pstmt.setString(1, budget.getId());
                    pstmt.setString(2, categoryId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();  // Commit the transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a budget by its ID
    public boolean deleteBudget(String budgetId) {
        String deleteBudgetSql = "DELETE FROM budgets WHERE id = ?";
        String deleteBudgetCategoriesSql = "DELETE FROM budget_categories WHERE budget_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);  // Start transaction

            // Delete budget categories first
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetCategoriesSql)) {
                pstmt.setString(1, budgetId);
                pstmt.executeUpdate();
            }

            // Now delete the budget itself
            try (PreparedStatement pstmt = connection.prepareStatement(deleteBudgetSql)) {
                pstmt.setString(1, budgetId);
                pstmt.executeUpdate();
            }

            connection.commit();  // Commit the transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}

