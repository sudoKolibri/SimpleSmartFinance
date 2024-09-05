package myProject.repository;

import myProject.model.Category;
import myProject.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    // Add a new category to the database
    public boolean addCategory(Category category) {
        // Update the SQL to include 'is_custom' and 'budget'
        String sql = "INSERT INTO categories (id, name, color, is_standard, is_custom, budget) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getColor());
            pstmt.setBoolean(4, category.isStandard());
            pstmt.setBoolean(5, category.isCustom());  // Make sure to add the is_custom value
            pstmt.setDouble(6, category.getBudget());  // Set the budget

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Get all categories from the database
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
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
}
