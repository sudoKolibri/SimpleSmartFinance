package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import myProject.controller.CategoryController;
import myProject.controller.TransactionController;
import myProject.model.Category;
import myProject.view.detail.CategoryDetailView;
import myProject.view.util.ViewUtils;

import java.util.Comparator;
import java.util.List;

import static myProject.view.util.ViewUtils.getCategoryBudget;

public class CategoryView {

    private final CategoryController categoryController;
    private final TransactionController transactionController;
    private final String currentUserId;
    private VBox mainLayout;  // Main layout for displaying content
    private BorderPane root;

    // Constructor to initialize the view with necessary dependencies
    public CategoryView(String currentUserId, CategoryController categoryController, TransactionController transactionController) {
        this.currentUserId = currentUserId;
        this.categoryController = categoryController;
        this.transactionController = transactionController;
    }

    private Button createCategoryButton;  // Declare the button at the class level

    // Load the main content into the provided root pane
    public void loadIntoPane(BorderPane root) {
        if (root == null) {
            System.err.println("Error: root layout is null. Cannot load the CategoryView.");
            return;
        }

        this.root = root;
        mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        showCategories();

        // Initialize and configure the "Add Category" button
        createCategoryButton = new Button("+ Add Category");
        createCategoryButton.setPrefWidth(150);
        createCategoryButton.setMaxWidth(200);
        createCategoryButton.setOnAction(e -> {
            createCategoryButton.setVisible(false); // Hide the button when form is shown
            showCreateCategoryForm(root);
        });

        mainLayout.getChildren().add(createCategoryButton);
        this.root.setCenter(mainLayout);
    }

    // Show all categories (standard and custom) as cards within the main layout
    private void showCategories() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        List<Category> categories = categoryController.getAllCategoriesForUser(currentUserId)
                .stream()
                .sorted(Comparator.comparing(Category::isStandard).reversed())
                .toList();

        int row = 0, col = 0;
        for (Category category : categories) {
            VBox categoryCard = createCategoryCard(category);
            gridPane.add(categoryCard, col, row);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
        mainLayout.getChildren().add(gridPane);
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
        card.getChildren().add(nameLabel);

        if (category.getBudget() != null && category.getBudget() > 0) {
            Label budgetLabel = new Label("$" + category.getBudget() + " Budget");
            budgetLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #8be9fd;");
            card.getChildren().add(budgetLabel);

            double spent = Math.abs(transactionController.getSpentAmountForCategory(category));  // Use absolute value
            Label spentLabel = new Label("$" + spent + " Spent");  // Display spent without negative sign
            spentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff79c6;");
            card.getChildren().add(spentLabel);

            ProgressBar progressBar = new ProgressBar(spent / category.getBudget());  // Use absolute value for progress calculation

            progressBar.setPrefWidth(150);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setStyle("-fx-accent: " + ViewUtils.getProgressBarColor(spent, category.getBudget()) + ";");
            card.getChildren().add(progressBar);
        } else {
            Label noBudgetLabel = new Label("No Budget Set");
            noBudgetLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff79c6;");
            card.getChildren().add(noBudgetLabel);
        }

        card.setOnMouseClicked(e -> showCategoryDetailView(category));
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

    // Show detailed view of the selected category
    private void showCategoryDetailView(Category category) {
        if (root == null) {
            System.err.println("Error: root layout is null. Cannot display CategoryDetailView.");
            return;
        }

        CategoryDetailView categoryDetailView = new CategoryDetailView(categoryController, transactionController, root);
        categoryDetailView.showCategoryDetailView(category);
    }




    // Show the category creation form within the main layout
    private void showCreateCategoryForm(BorderPane root) {
        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(20));
        formCard.setAlignment(Pos.CENTER);
        formCard.getStyleClass().add("account-card");
        formCard.setMaxWidth(300);

        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");
        nameField.setMaxWidth(250);

        TextField budgetField = new TextField();
        budgetField.setPromptText("Budget");
        budgetField.setMaxWidth(250);

        Button submitButton = createSubmitButton(nameField, budgetField, root);
        submitButton.getStyleClass().add("button");

        // Create the "Cancel" button to hide the form and show the "Add Category" button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            mainLayout.getChildren().remove(formCard);  // Remove the form
            createCategoryButton.setVisible(true);  // Show the "Add Category" button again
        });

        formCard.getChildren().addAll(new Label("New Category"), nameField, budgetField, submitButton, cancelButton);
        mainLayout.getChildren().add(formCard);
    }

    // Create submit button for the form
    private Button createSubmitButton(TextField nameField, TextField budgetField, BorderPane root) {
        Button submitButton = new Button("Create Category");
        submitButton.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36;");

        submitButton.setOnAction(e -> {
            String categoryName = nameField.getText();
            Double categoryBudget = getCategoryBudget(budgetField);

            Category newCategory = new Category(null, categoryName, false, true, categoryBudget);
            categoryController.addCategory(newCategory, currentUserId);

            mainLayout.getChildren().clear();
            loadIntoPane(this.root);  // Reload the pane to refresh the categories and reset the state
        });

        return submitButton;
    }
}
