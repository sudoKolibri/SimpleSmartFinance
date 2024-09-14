package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.CategoryController;
import myProject.model.Category;
import myProject.repository.CategoryRepository;
import myProject.service.CategoryService;

import java.util.Comparator;
import java.util.List;

public class CategoryView {

    private final CategoryController categoryController;
    private final String currentUserId;
    private HBox mainLayout;  // Horizontal layout for categories and form
    private final BorderPane root; // Reference to root layout for dynamic updates

    public CategoryView(String currentUserId, BorderPane root) {
        this.currentUserId = currentUserId;
        this.root = root;

        // Dependency injection for controller, service, and repository
        CategoryRepository categoryRepository = new CategoryRepository();
        CategoryService categoryService = new CategoryService(categoryRepository);
        this.categoryController = new CategoryController(categoryService);
    }

    // Load CategoryView into the dynamic content area of MainView
    public void loadIntoPane() {
        mainLayout = new HBox(40);  // Horizontal layout to hold categories and form
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);  // Center horizontally

        VBox categoryLayout = new VBox(20);  // Vertical layout for category grid and button
        categoryLayout.setAlignment(Pos.CENTER);

        loadCategories(categoryLayout);  // Pass layout to load categories

        // Create button for adding a new category
        Button createCategoryButton = new Button("+ Add Category");
        createCategoryButton.setPrefWidth(150);
        createCategoryButton.setMaxWidth(200);
        createCategoryButton.setOnAction(e -> showCreateCategoryForm());

        categoryLayout.getChildren().add(createCategoryButton);  // Add button below categories
        mainLayout.getChildren().add(categoryLayout);  // Add to main layout

        root.setCenter(mainLayout);  // Set main layout into the dynamic window
    }

    // Load and display both global and custom categories
    private void loadCategories(VBox categoryLayout) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        // Get and sort categories with standard categories first
        List<Category> categories = categoryController.getAllCategoriesForUser(currentUserId)
                .stream()
                .sorted(Comparator.comparing(Category::isStandard).reversed())
                .toList();

        int row = 0, col = 0;
        for (Category category : categories) {
            VBox categoryCard = createCategoryCard(category);
            gridPane.add(categoryCard, col, row);

            col++;
            if (col == 3) {  // Max 3 columns per row
                col = 0;
                row++;
            }
        }
        categoryLayout.getChildren().add(gridPane);
    }

    // Create a card for each category with progress bar and details
    private VBox createCategoryCard(Category category) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 200);
        card.getStyleClass().add("account-card");

        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        Label budgetLabel = new Label("$" + category.getBudget() + " Budget");
        budgetLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #8be9fd;");

        double spent = category.getBudget() * 0.4;  // Placeholder for spent calculation
        Label spentLabel = new Label("$" + spent + " Spent");
        spentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff79c6;");

        ProgressBar progressBar = new ProgressBar(spent / category.getBudget());
        progressBar.setPrefWidth(150);

        card.getChildren().addAll(nameLabel, budgetLabel, spentLabel, progressBar);

        // Add click handler for detailed view
        card.setOnMouseClicked(e -> showCategoryDetailView(category));

        // Add hover effect (scaling)
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }


    // Show detailed view of a specific category
    private void showCategoryDetailView(Category category) {
        VBox detailView = new VBox(20);
        detailView.setPadding(new Insets(40));
        detailView.setAlignment(Pos.TOP_LEFT);
        detailView.setStyle("-fx-background-color: #282a36;");

        Label nameLabel = new Label("Category: " + category.getName());
        nameLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        Label budgetLabel = new Label("Budget: $" + category.getBudget());
        budgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");

        double spent = category.getBudget() * 0.4;  // Placeholder for spent calculation
        Label spentLabel = new Label("Already Spent: $" + spent);
        spentLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff79c6;");

        ProgressBar progressBar = new ProgressBar(spent / category.getBudget());
        progressBar.setPrefWidth(600);  // Adjust the progress bar size
        progressBar.setStyle("-fx-accent: #50fa7b;");

        // Placeholder for recent transactions
        Label transactionsLabel = new Label("Recent Transactions (Placeholder)");
        transactionsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        detailView.getChildren().addAll(nameLabel, budgetLabel, spentLabel, progressBar, transactionsLabel);

        // Ensure the detail view is set in the main layout
        root.setCenter(detailView);
    }

    // Show the category creation form next to the category grid
    private void showCreateCategoryForm() {
        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(20));
        formCard.setAlignment(Pos.CENTER);
        formCard.setStyle("-fx-background-color: #44475a; -fx-border-color: #ff79c6; -fx-border-radius: 10px;");
        formCard.setMaxWidth(300);

        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");
        nameField.setMaxWidth(250);

        TextField colorField = new TextField();
        colorField.setPromptText("Category Color");
        colorField.setMaxWidth(250);

        TextField budgetField = new TextField();
        budgetField.setPromptText("Budget");
        budgetField.setMaxWidth(250);

        Button submitButton = createSubmitButton(nameField, colorField, budgetField);
        formCard.getChildren().addAll(new Label("New Category"), nameField, colorField, budgetField, submitButton);

        // Clear any existing form and add the new one
        if (mainLayout.getChildren().size() > 1) {
            mainLayout.getChildren().remove(1);
        }
        mainLayout.getChildren().add(formCard);
    }

    // Create submit button for the form
    private Button createSubmitButton(TextField nameField, TextField colorField, TextField budgetField) {
        Button submitButton = new Button("Create Category");
        submitButton.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36;");
        submitButton.setOnAction(e -> {
            String categoryName = nameField.getText();
            String categoryColor = colorField.getText().isEmpty() ? "#FFFFFF" : colorField.getText();
            double categoryBudget = Double.parseDouble(budgetField.getText());

            Category newCategory = new Category(null, categoryName, categoryColor, false, true, categoryBudget);
            categoryController.addCategory(newCategory, currentUserId);

            // Refresh the categories and display
            mainLayout.getChildren().clear();
            loadIntoPane();
        });
        return submitButton;
    }
}
