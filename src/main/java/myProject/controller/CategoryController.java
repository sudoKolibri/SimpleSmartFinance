package myProject.controller;

import myProject.model.Category;
import myProject.service.CategoryService;

import java.util.List;

public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    public boolean addCategory(String name, String color, boolean isStandard, boolean isCustom, double budget) {
        return categoryService.addCategory(name, color, isStandard, isCustom, budget);
    }

    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
