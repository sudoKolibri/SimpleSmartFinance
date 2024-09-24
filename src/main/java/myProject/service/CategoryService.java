package myProject.service;

import myProject.model.Category;
import myProject.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    // Constructor with dependency injection
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public boolean addCategory(Category category, String userId) {
        System.out.println("New Category with name " + category.getName() + " and ID: " + category.getId() + " is passed to CategoryRepository.");
        return categoryRepository.addCategory(category, userId);
    }

    public boolean updateCategory(Category category) {
        return categoryRepository.updateCategory(category);
    }

    public List<Category> getGlobalCategories() {
        return categoryRepository.getGlobalCategories();
    }

    public List<Category> getCustomCategoriesForUser(String userId) {
        return categoryRepository.getCustomCategoriesForUser(userId);
    }

    // Get all categories (both global and custom) for a specific user
    public List<Category> getAllCategoriesForUser(String userId) {
        // Fetch global categories
        List<Category> globalCategories = getGlobalCategories();

        // Fetch custom categories for the given user
        List<Category> customCategories = getCustomCategoriesForUser(userId);

        // Combine both lists
        globalCategories.addAll(customCategories);

        // Return the combined list
        return globalCategories;
    }
}
