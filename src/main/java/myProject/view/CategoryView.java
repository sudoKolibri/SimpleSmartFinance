package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.CategoryController;
import myProject.model.Category;

import java.util.List;

public class CategoryView {

    private final CategoryController categoryController = new CategoryController();
    private final String currentUserId;  // To track user for budget-related operations

    public CategoryView(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    // Load the CategoryView into the dynamic content area
    public void loadIntoPane(BorderPane root) {
        VBox mainLayout = new VBox(30);  // Vertical layout for the category list
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setSpacing(40);

        // ==================== Category List ====================
        VBox categoryLayout = new VBox(20);
        categoryLayout.setPadding(new Insets(20));
        categoryLayout.setAlignment(Pos.CENTER);

        // Category cards container
        VBox categoryContainer = new VBox(20);
        categoryContainer.setAlignment(Pos.CENTER);

        // Load and display categories as cards
        List<Category> categories = categoryController.getAllCategories();
        for (Category category : categories) {
            HBox categoryCard = createCategoryCard(category);
            categoryContainer.getChildren().add(categoryCard);
        }

        // Button to create a new custom category
        Button createCategoryButton = new Button("+ Add Category");
        createCategoryButton.setPrefWidth(150);
        createCategoryButton.setMaxWidth(200);
        createCategoryButton.setOnAction(e -> showCreateCategoryForm(categoryLayout));

        // Add components to the layout
        categoryLayout.getChildren().addAll(createCategoryButton, categoryContainer);
        mainLayout.getChildren().addAll(categoryLayout);

        // Set the center content of MainView to CategoryView
        root.setCenter(mainLayout);
    }

    // Show a form to create a new custom category
    private void showCreateCategoryForm(VBox categoryLayout) {
        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(20));
        formCard.setAlignment(Pos.CENTER);
        formCard.setStyle("-fx-background-color: #44475a; -fx-border-color: #ff79c6; -fx-border-radius: 10px;");
        formCard.setMaxWidth(300);  // Limit the width of the form card

        // Fields for name and color
        Label nameLabel = new Label("Category Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f8f8f2;");
        // Similar for color, budget...

        // Submit and create a new category
        Button submitButton = new Button("Create Category");
        submitButton.setStyle("-fx-background-color: #50fa7b;");
        submitButton.setOnAction(e -> {
            // Logic to create category
            categoryLayout.getChildren().remove(formCard);
            refreshCategoryList(categoryLayout);  // Refresh list after adding category
        });

        formCard.getChildren().addAll(nameLabel, submitButton);
        categoryLayout.getChildren().add(0, formCard);  // Add the form at the top
    }

    // Create a visual card for a category
    private HBox createCategoryCard(Category category) {
        HBox card = new HBox();
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 200);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: " + category.getColor() + "; -fx-border-radius: 10px;");

        // Display category name and budget progress
        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f8f8f2;");

        ProgressBar progressBar = new ProgressBar(0);  // Example, connect this to the budget
        if (category.getBudget() > 0) {
            // Calculate the progress based on spending (dummy example)
            double progress = Math.random();  // Replace with actual budget progress logic
            progressBar.setProgress(progress);
        }

        VBox cardContent = new VBox(10);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.getChildren().addAll(nameLabel, progressBar);

        card.getChildren().add(cardContent);
        return card;
    }

    // Refresh the category list dynamically after adding or removing categories
    private void refreshCategoryList(VBox categoryLayout) {
        categoryLayout.getChildren().clear();
        List<Category> categories = categoryController.getAllCategories();
        for (Category category : categories) {
            HBox categoryCard = createCategoryCard(category);
            categoryLayout.getChildren().add(categoryCard);
        }
    }
}
