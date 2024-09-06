package myProject.controller;

import myProject.model.Category;
import myProject.service.CategoryService;

import java.util.List;

public class CategoryController {
    private final CategoryService categoryService;

    // Constructor with dependency injection
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public boolean addCategory(Category category, String userId) {
        return categoryService.addCategory(category, userId);
    }

    public List<Category> getGlobalCategories() {
        return categoryService.getGlobalCategories();
    }

    public List<Category> getCustomCategoriesForUser(String userId) {
        return categoryService.getCustomCategoriesForUser(userId);
    }

    public List<Category> getAllCategoriesForUser(String userId) {
        // Fetch both global and custom categories
        List<Category> globalCategories = getGlobalCategories();
        List<Category> customCategories = getCustomCategoriesForUser(userId);

        // Combine both lists
        globalCategories.addAll(customCategories);

        // Return the combined list
        return globalCategories;
    }

    public Category getCategoryById(String categoryId, String userId) {
        List<Category> allCategories = getAllCategoriesForUser(userId);  // Pass userId here
        return allCategories.stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElse(null);  // Return null if category not found
    }



}
