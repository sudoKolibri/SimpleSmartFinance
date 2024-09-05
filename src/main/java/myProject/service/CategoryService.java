package myProject.service;

import myProject.model.Category;
import myProject.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public boolean addCategory(String name, String color, boolean isStandard, boolean isCustom, double budget) {
        Category category = new Category(java.util.UUID.randomUUID().toString(), name, color, isStandard, isCustom, budget);
        return categoryRepository.addCategory(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }
}
