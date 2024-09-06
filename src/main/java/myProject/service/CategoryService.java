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
        return categoryRepository.addCategory(category, userId);
    }

    public List<Category> getGlobalCategories() {
        return categoryRepository.getGlobalCategories();
    }

    public List<Category> getCustomCategoriesForUser(String userId) {
        return categoryRepository.getCustomCategoriesForUser(userId);
    }
}
