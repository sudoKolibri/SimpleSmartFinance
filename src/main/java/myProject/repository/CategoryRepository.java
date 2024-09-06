package myProject.repository;

import myProject.model.Category;
import myProject.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    // Add a new category to the database
    public boolean addCategory(Category category, String userId) {
        String sql = "INSERT INTO categories (id, name, color, is_standard, is_custom, budget, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getColor());
            pstmt.setBoolean(4, category.isStandard());
            pstmt.setBoolean(5, category.isCustom());
            pstmt.setDouble(6, category.getBudget());
            pstmt.setString(7, category.isCustom() ? userId : null);  // Set user_id only for custom categories

            pstmt.executeUpdate();

            System.out.println("Category added successfully");

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
                String color = rs.getString("color");
                boolean isStandard = rs.getBoolean("is_standard");
                boolean isCustom = rs.getBoolean("is_custom");
                double budget = rs.getDouble("budget");

                categories.add(new Category(id, name, color, isStandard, isCustom, budget));
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
                    String color = rs.getString("color");
                    boolean isStandard = rs.getBoolean("is_standard");
                    boolean isCustom = rs.getBoolean("is_custom");
                    double budget = rs.getDouble("budget");

                    categories.add(new Category(id, name, color, isStandard, isCustom, budget));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }
}
