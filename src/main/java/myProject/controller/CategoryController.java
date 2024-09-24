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
        System.out.println("New Category with name " + category.getName() + " and ID: " + category.getId() + " is passed to CategoryService.");
        return categoryService.addCategory(category, userId);
    }

    public boolean updateCategory(Category category) {
        System.out.println("Updating Category with ID: " + category.getId());
        return categoryService.updateCategory(category);
    }

    public List<Category> getGlobalCategories() {
        return categoryService.getGlobalCategories();
    }

    public List<Category> getCustomCategoriesForUser(String userId) {
        return categoryService.getCustomCategoriesForUser(userId);
    }

    // Refactor: Delegate the call to the service layer
    public List<Category> getAllCategoriesForUser(String userId) {
        return categoryService.getAllCategoriesForUser(userId);
    }

    public Category getCategoryById(String categoryId, String userId) {
        List<Category> allCategories = getAllCategoriesForUser(userId);  // Pass userId here
        return allCategories.stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElse(null);  // Return null if category not found
    }

    // Method to check if a category name is duplicate within the user's categories
    public boolean isCategoryNameDuplicate(String name, String currentCategoryId) {
        List<Category> allCategories = categoryService.getAllCategoriesForUser(null); // Adjust to pass the correct user ID if necessary
        return allCategories.stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(name) && !category.getId().equals(currentCategoryId));
    }
}
