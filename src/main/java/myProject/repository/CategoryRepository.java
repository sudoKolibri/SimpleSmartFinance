package myProject.repository;

import myProject.model.Category;
import myProject.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    // Add a new category to the database
    public boolean addCategory(Category category, String userId) {
        String sql = "INSERT INTO categories (id, name, is_standard, is_custom, budget, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setBoolean(3, category.isStandard());
            pstmt.setBoolean(4, category.isCustom());

            // Check if budget is null, then set it as 0.0 (or another default value)
            if (category.getBudget() != null) {
                pstmt.setDouble(5, category.getBudget());
            } else {
                pstmt.setNull(5, java.sql.Types.DOUBLE);  // Store null in the budget field if no budget is provided
            }

            // Set user_id only for custom categories
            pstmt.setString(6, category.isCustom() ? userId : null);

            pstmt.executeUpdate();

            System.out.println("Category added successfully");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("New Category with name " + category.getName() + " and ID: " + category.getId() + " failed to add to the repository.");
            return false;
        }
    }

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

            System.out.println("Category updated successfully");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // public boolean deleteCategory(Category category(){}

    // Get all global categories
    public List<Category> getGlobalCategories() {
        String sql = "SELECT * FROM categories WHERE user_id IS NULL AND is_custom = false";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                boolean isStandard = rs.getBoolean("is_standard");
                boolean isCustom = rs.getBoolean("is_custom");

                // Handle the budget being nullable in the result set
                Double budget = rs.getObject("budget") != null ? rs.getDouble("budget") : null;

                categories.add(new Category(id, name, isStandard, isCustom, budget));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    // Get custom categories for a specific user
    public List<Category> getCustomCategoriesForUser(String userId) {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND is_custom = true";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    boolean isStandard = rs.getBoolean("is_standard");
                    boolean isCustom = rs.getBoolean("is_custom");

                    // Handle nullable budget
                    Double budget = rs.getObject("budget") != null ? rs.getDouble("budget") : null;

                    categories.add(new Category(id, name, isStandard, isCustom, budget));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

}
