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
            throw new IllegalArgumentException("Root layout cannot be null.");
        }
        this.root = root;
    }

    // Show detailed view of a specific category
    public void showCategoryDetailView(Category category) {

        VBox detailView = new VBox(20);
        detailView.getStyleClass().add("detail-view");

        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("detail-label");
        detailView.getChildren().add(nameLabel);

        if (category.getBudget() != null && category.getBudget() > 0) {
            Label budgetLabel = new Label("Budget: $" + category.getBudget());
            budgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");
            detailView.getChildren().add(budgetLabel);

            double spent = Math.abs(transactionController.getSpentAmountForCategory(category));  // Use absolute value
            Label spentLabel = new Label("Already Spent: $" + spent);  // Display spent without negative sign
            spentLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff79c6;");
            detailView.getChildren().add(spentLabel);

            double budgetValue = category.getBudget() != null ? category.getBudget() : 1;
            ProgressBar progressBar = new ProgressBar(Math.abs(spent) / budgetValue);  // Use absolute value for progress calculation

            progressBar.setPrefWidth(600);  // Adjust width as necessary
            progressBar.setStyle("-fx-accent: " + ViewUtils.getProgressBarColor(spent, budgetValue) + ";");
            detailView.getChildren().add(progressBar);
        } else {
            Label noBudgetLabel = new Label("No Budget Set");
            noBudgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffb86c;");
            detailView.getChildren().add(noBudgetLabel);
        }

        Button editButton = new Button("Edit");
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
        VBox editView = new VBox(20);
        editView.getStyleClass().add("detail-view");

        // Convert the name and budget labels to TextFields when editing
        TextField nameField = new TextField(category.getName());
        nameField.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        editView.getChildren().add(nameField);

        // Format the budget value to ensure it displays with two decimal places
        String formattedBudget = (category.getBudget() != null) ?
                String.format("%.2f", category.getBudget()) : "";

        TextField budgetField = new TextField(formattedBudget);
        budgetField.setPromptText("No Budget Set");
        budgetField.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffb86c;");
        editView.getChildren().add(budgetField);


        if (category.getBudget() != null && category.getBudget() > 0) {
            double spent = Math.abs(transactionController.getSpentAmountForCategory(category));
            Label spentLabel = new Label(String.format("Already Spent: $%.2f", spent));
            spentLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff79c6;");
            editView.getChildren().add(spentLabel);

            // Handle potential null budget when calculating the progress bar
            double budgetValue = category.getBudget() != null ? category.getBudget() : 1; // Prevent division by zero
            ProgressBar progressBar = new ProgressBar(spent / budgetValue);
            progressBar.setPrefWidth(600);
            progressBar.setStyle("-fx-accent: " + ViewUtils.getProgressBarColor(spent, budgetValue) + ";");
            editView.getChildren().add(progressBar);
        }

        // Save and Cancel buttons
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> saveCategoryChanges(category, nameField, budgetField));

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button");
        cancelButton.setOnAction(e -> showCategoryDetailView(category));

        // VBox to hold Save and Cancel buttons
        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        editView.getChildren().add(buttonBox);

        root.setCenter(editView);
    }

    // Save the changes made to the category
    private void saveCategoryChanges(Category category, TextField nameField, TextField budgetField) {
        try {
            String newName = nameField.getText();

            // Attempt to parse the budget field value correctly
            Double newBudget = null;
            if (!budgetField.getText().isEmpty()) {
                try {
                    newBudget = Double.parseDouble(budgetField.getText());
                } catch (NumberFormatException e) {
                    // Show an error alert if the input is not a valid number
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid budget amount. Please enter a valid number.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            // Check for duplicate category names
            if (categoryController.isCategoryNameDuplicate(newName, category.getId())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Category name already exists.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Update the category with the new values
            category.setName(newName);
            category.setBudget(newBudget);

            // Update category in the controller
            categoryController.updateCategory(category);
            System.out.println("Category updated successfully: " + category.getName());

            // Reload the detailed view with updated information
            showCategoryDetailView(category);

        } catch (Exception ex) {
            System.err.println("Failed to update category: " + ex.getMessage());
            ex.printStackTrace();
        }
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
