package myProject.view.detail;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.controller.TransactionController;
import myProject.controller.CategoryController;
import myProject.view.util.ViewUtils;

public class CategoryDetailView {
    private final CategoryController categoryController;
    private final TransactionController transactionController;
    private final BorderPane root;

    // Constructor to initialize controllers and layout
    public CategoryDetailView(CategoryController categoryController, TransactionController transactionController, BorderPane root) {
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        if (root == null) {
            throw new IllegalArgumentException("Root layout cannot be null.");  // Immediate fail if root is not passed correctly
        }
        this.root = root;
    }

    // Show detailed view of a specific category
    public void showCategoryDetailView(Category category) {
        if (root == null) {
            System.err.println("Error: root layout is null. Cannot display category details.");
            return;
        }

        VBox detailView = new VBox(20);
        detailView.getStyleClass().add("detail-view");

        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("detail-label");
        detailView.getChildren().add(nameLabel);

        if (category.getBudget() != null && category.getBudget() > 0) {
            Label budgetLabel = new Label("Budget: $" + category.getBudget());
            budgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");
            detailView.getChildren().add(budgetLabel);

            double spent = transactionController.getSpentAmountForCategory(category);
            Label spentLabel = new Label("Already Spent: $" + spent);
            spentLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff79c6;");
            detailView.getChildren().add(spentLabel);

            // Calculate and set the progress bar with the correct color
            ProgressBar progressBar = new ProgressBar(spent / category.getBudget());
            progressBar.setPrefWidth(600);  // Adjust width as necessary
            progressBar.setStyle("-fx-accent: " + ViewUtils.getProgressBarColor(spent, category.getBudget()) + ";");
            detailView.getChildren().add(progressBar);
        } else {
            Label noBudgetLabel = new Label("No Budget Set");
            noBudgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffb86c;");
            detailView.getChildren().add(noBudgetLabel);
        }

        Button editButton = new Button("Edit Category");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> showEditCategoryForm(category));
        detailView.getChildren().add(editButton);

        Label transactionsLabel = new Label("Transactions for this category:");
        transactionsLabel.getStyleClass().add("transactions-label");
        detailView.getChildren().add(transactionsLabel);

        TableView<Transaction> transactionsTable = createTransactionsTable(category);
        detailView.getChildren().add(transactionsTable);

        root.setCenter(detailView);
    }




    // Show the edit form for a category
    private void showEditCategoryForm(Category category) {
        if (root == null) {
            System.err.println("Error: root layout is null. Cannot display edit form.");
            return;
        }

        VBox formCard = new VBox(10);
        formCard.getStyleClass().add("account-card");
        formCard.setMaxWidth(300);

        TextField nameField = new TextField(category.getName());
        nameField.setPromptText("Category Name");
        nameField.setMaxWidth(250);

        TextField budgetField = new TextField(category.getBudget() != null ? category.getBudget().toString() : "");
        budgetField.setPromptText("Budget");
        budgetField.setMaxWidth(250);

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("button");

        saveButton.setOnAction(e -> {
            try {
                category.setName(nameField.getText());
                Double newBudget = ViewUtils.getCategoryBudget(budgetField);

                if (category.isStandard() && newBudget != null) {
                    category.setBudget(newBudget);
                } else if (category.isCustom() && newBudget == null) {
                    category.setBudget(null);
                }

                categoryController.updateCategory(category);
                System.out.println("Category updated successfully: " + category.getName());
                showCategoryDetailView(category);

            } catch (Exception ex) {
                System.err.println("Failed to update category: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        formCard.getChildren().addAll(new Label("Edit Category"), nameField, budgetField, saveButton);
        root.setCenter(formCard);
    }

    // Create a transactions table for the given category
    private TableView<Transaction> createTransactionsTable(Category category) {
        TableView<Transaction> transactionsTable = new TableView<>();

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asString());

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty().asString());

        TableColumn<Transaction, String> accountColumn = new TableColumn<>("Account");
        accountColumn.setCellValueFactory(data -> {
            if (data.getValue().getAccount() != null) {
                return data.getValue().getAccount().nameProperty();
            } else {
                return new SimpleStringProperty("No Account");
            }
        });

        transactionsTable.getColumns().addAll(descriptionColumn, amountColumn, dateColumn, accountColumn);

        ObservableList<Transaction> allTransactions = transactionController.getAllTransactions();
        ObservableList<Transaction> filteredTransactions = allTransactions.filtered(transaction -> transaction.getCategory() != null && transaction.getCategory().getId().equals(category.getId()));

        transactionsTable.setItems(filteredTransactions);

        return transactionsTable;
    }
}
