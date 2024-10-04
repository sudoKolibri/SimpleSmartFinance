package myProject.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import myProject.controller.BudgetController;
import myProject.controller.CategoryController;
import myProject.model.Budget;
import myProject.model.Category;
import myProject.repository.BudgetRepository;
import myProject.repository.CategoryRepository;
import myProject.service.BudgetService;
import myProject.service.CategoryService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BudgetView {

    private final BudgetController budgetController;
    private final CategoryController categoryController;
    private final String currentUserId;
    private VBox mainLayout;

    public BudgetView(String currentUserId) {
        this.currentUserId = currentUserId;

        // Initialize the controllers
        this.categoryController = new CategoryController(new CategoryService(new CategoryRepository()));
        this.budgetController = new BudgetController(new BudgetService(new BudgetRepository()));
    }

    // Helper method to add the "+ Add Budget" button
    private void addCreateBudgetButton() {
        Button createBudgetButton = new Button("+ Add Budget");
        createBudgetButton.setPrefWidth(150);
        createBudgetButton.setMaxWidth(200);
        createBudgetButton.setOnAction(e -> showCreateBudgetForm());
        mainLayout.getChildren().add(createBudgetButton);
    }

    // Load and display all budgets
    private void loadBudgets() {
        mainLayout.getChildren().clear();  // Clear the existing layout
        List<Budget> budgets = budgetController.getBudgetsForUser(currentUserId);

        if (budgets.isEmpty()) {
            Label noBudgetsLabel = new Label("No budgets created yet.");
            mainLayout.getChildren().add(noBudgetsLabel);
        } else {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(20));
            gridPane.setHgap(20);
            gridPane.setVgap(20);

            int row = 0;
            int col = 0;

            for (Budget budget : budgets) {
                VBox budgetCard = createBudgetCard(budget);
                gridPane.add(budgetCard, col, row);
                col++;
                if (col == 2) {
                    col = 0;
                    row++;
                }
            }
            mainLayout.getChildren().add(gridPane);
        }
        addCreateBudgetButton();  // Always add the "+ Add Budget" button at the bottom
    }

    // Create a reusable method for building budget cards
    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(250, 150);
        card.setStyle("-fx-background-color: #6272a4; -fx-border-color: #ff79c6; -fx-border-radius: 10px;");

        Label amountLabel = new Label("Budget: $" + budget.getAmount());
        Label dateLabel = new Label("From: " + budget.getStartDate() + " to: " + budget.getEndDate());

        // Convert category IDs to names
        List<String> categoryNames = budget.getCategoryIds().stream()
                .map(categoryId -> categoryController.getCategoryById(categoryId, currentUserId).getName())
                .collect(Collectors.toList());

        Label categoriesLabel = new Label("Categories: " + String.join(", ", categoryNames));
        ProgressBar progressBar = new ProgressBar(0.5);  // Placeholder progress bar

        HBox actionButtons = createActionButtons(budget);

        card.getChildren().addAll(amountLabel, dateLabel, categoriesLabel, progressBar, actionButtons);
        return card;
    }

    // Create reusable method for action buttons (Edit/Delete)
    private HBox createActionButtons(Budget budget) {
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> showEditBudgetForm(budget));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            budgetController.deleteBudget(budget.getId());
            loadBudgets();  // Reload the budget list after deletion
        });

        actionButtons.getChildren().addAll(editButton, deleteButton);
        return actionButtons;
    }

    // Helper method to clear and reload the budget form and budgets
    private void reloadBudgets() {
        mainLayout.getChildren().clear();  // Clear the form and previous content
        loadBudgets();  // Reload the updated list of budgets
    }

    // Show the budget creation form
    private void showCreateBudgetForm() {
        VBox formCard = createBudgetForm(null);
        mainLayout.getChildren().add(formCard);
    }

    // Show the budget editing form
    private void showEditBudgetForm(Budget budget) {
        VBox formCard = createBudgetForm(budget);
        mainLayout.getChildren().add(formCard);
    }

    // Method to create a budget form for both creating and editing
    private VBox createBudgetForm(Budget budget) {
        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(20));
        formCard.setAlignment(Pos.CENTER);
        formCard.setStyle("-fx-background-color: #44475a; -fx-border-color: #ff79c6; -fx-border-radius: 10px;");
        formCard.setMaxWidth(300);

        TextField amountField = new TextField(budget != null ? String.valueOf(budget.getAmount()) : "");
        amountField.setPromptText("Budget Amount");
        amountField.setMaxWidth(250);

        DatePicker startDatePicker = new DatePicker(budget != null ? convertToLocalDate(budget.getStartDate()) : null);
        DatePicker endDatePicker = new DatePicker(budget != null ? convertToLocalDate(budget.getEndDate()) : null);

        ListView<Category> categoryListView = new ListView<>();
        categoryListView.setItems(FXCollections.observableArrayList(categoryController.getAllCategoriesForUser(currentUserId)));
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (budget != null) {
            for (String categoryId : budget.getCategoryIds()) {
                categoryListView.getSelectionModel().select(categoryController.getCategoryById(categoryId, currentUserId));
            }
        }

        Button submitButton = new Button(budget != null ? "Save Changes" : "Create Budget");
        submitButton.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36;");
        submitButton.setOnAction(e -> handleBudgetFormSubmit(budget, amountField, startDatePicker, endDatePicker, categoryListView));

        formCard.getChildren().addAll(new Label(budget != null ? "Edit Budget" : "New Budget"),
                amountField, startDatePicker, endDatePicker, categoryListView, submitButton);

        mainLayout.getChildren().clear();  // Clear the layout while showing the form
        return formCard;
    }

    // Convert java.sql.Date to LocalDate
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Handle form submission logic for both creating and editing
    private void handleBudgetFormSubmit(Budget budget, TextField amountField, DatePicker startDatePicker, DatePicker endDatePicker, ListView<Category> categoryListView) {
        double amount = Double.parseDouble(amountField.getText());
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        List<Category> selectedCategories = categoryListView.getSelectionModel().getSelectedItems();

        if (amount > 0 && startDate != null && endDate != null && !selectedCategories.isEmpty()) {
            List<String> categoryIds = selectedCategories.stream().map(Category::getId).collect(Collectors.toList());

            if (budget == null) {
                String budgetId = UUID.randomUUID().toString();
                budget = new Budget(budgetId, currentUserId, amount, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate), categoryIds);
                budgetController.addBudget(budget);
            } else {
                budget.setAmount(amount);
                budget.setStartDate(java.sql.Date.valueOf(startDate));
                budget.setEndDate(java.sql.Date.valueOf(endDate));
                budget.setCategoryIds(categoryIds);
                budgetController.updateBudget(budget);
            }

            reloadBudgets();  // Reload after form submission
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields and select categories.");
            alert.show();
        }
    }
}
