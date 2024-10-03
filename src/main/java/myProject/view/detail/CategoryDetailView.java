package myProject.view.detail;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.controller.TransactionController;
import myProject.controller.CategoryController;
import myProject.controller.AccountController;
import myProject.util.LoggerUtils;
import myProject.view.CategoryView;
import myProject.view.util.ViewUtils;

import java.sql.SQLException;

/**
 * Die Klasse CategoryDetailView ist verantwortlich für die Anzeige, Bearbeitung und Verwaltung
 * der Details einer Kategorie sowie deren Transaktionen. Sie ermöglicht die Anzeige und
 * Bearbeitung von Budgets und das Handling der
 * Benutzerinteraktionen mit Kategorien.
 */
public class CategoryDetailView {
    private final CategoryController categoryController;
    private final TransactionController transactionController;
    private final AccountController accountController;
    private final String loggedInUserId;
    private final BorderPane root;


    /**
     * Konstruktor zur Initialisierung der benötigten Controller und des Layouts.
     *
     * @param categoryController    Der Controller für Kategorien.
     * @param transactionController Der Controller für Transaktionen.
     * @param root                  Das Root-Layout, in das die Ansicht eingefügt wird.
     */
    public CategoryDetailView(CategoryController categoryController, TransactionController transactionController, AccountController accountController, String loggedInUserId, BorderPane root) {
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        this.accountController = accountController; // AccountController hinzufügen
        this.loggedInUserId = loggedInUserId; // loggedInUserId hinzufügen
        if (root == null) {
            LoggerUtils.logError(CategoryDetailView.class.getName(), "Root-Layout darf nicht null sein.", null);
            throw new IllegalArgumentException("Root-Layout darf nicht null sein.");
        }
        this.root = root;
    }

    /**
     * Zeigt die Detailansicht einer bestimmten Kategorie an, inklusive Budget-Informationen
     * und zugehörigen Transaktionen.
     *
     * @param category Die Kategorie, deren Details angezeigt werden sollen.
     */
    public void showCategoryDetailView(Category category) {
        LoggerUtils.logInfo(CategoryDetailView.class.getName(), "Zeige Detailansicht für Kategorie: " + category.getName());

        VBox detailView = new VBox(20);
        detailView.getStyleClass().add("detail-view");

        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("detail-label");
        detailView.getChildren().add(nameLabel);

        if (category.getBudget() != null && category.getBudget() > 0) {
            Label budgetLabel = new Label("Budget: $" + category.getBudget());
            budgetLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #8be9fd;");
            detailView.getChildren().add(budgetLabel);

            double spent = Math.abs(transactionController.getSpentAmountForCategory(category));
            Label spentLabel = new Label("Already Spent: $" + spent);
            spentLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff79c6;");
            detailView.getChildren().add(spentLabel);

            double budgetValue = category.getBudget() != null ? category.getBudget() : 1;
            ProgressBar progressBar = new ProgressBar(Math.abs(spent) / budgetValue);
            progressBar.setPrefWidth(600);
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

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("edit-button");
        deleteButton.setOnAction(e -> {
            // Create a confirmation dialog
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Category");
            confirmationAlert.setHeaderText("Are you sure you want to delete the category: " + category.getName() + "?");
            confirmationAlert.setContentText("All transactions in this category will be moved to 'No Category'.");

            // Apply custom styling to the dialog pane
            DialogPane dialogPane = confirmationAlert.getDialogPane();
            dialogPane.getStyleClass().add("custom-alert");

            // Wait for the user's response
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = categoryController.deleteCategory(category.getId());

                    if (success) {
                        LoggerUtils.logInfo(CategoryDetailView.class.getName(), "Kategorie erfolgreich gelöscht: " + category.getId());
                        try {
                            CategoryView categoryView = new CategoryView(loggedInUserId, categoryController, transactionController, accountController);
                            categoryView.loadIntoPane(root); // Reload the CategoryView
                        } catch (SQLException ex) {
                            LoggerUtils.logError(CategoryDetailView.class.getName(), "Fehler beim Laden der CategoryView nach dem Löschen", ex);
                        }
                    } else {
                        LoggerUtils.logError(CategoryDetailView.class.getName(), "Fehler beim Löschen der Kategorie: " + category.getId(), null);
                    }
                }
            });
        });


// Füge den Button zu deiner VBox hinzu
        detailView.getChildren().add(deleteButton);


        Label transactionsLabel = new Label("Transactions for this category:");
        transactionsLabel.getStyleClass().add("transactions-label");
        detailView.getChildren().add(transactionsLabel);

        TableView<Transaction> transactionsTable = createTransactionsTable(category);
        detailView.getChildren().add(transactionsTable);

        root.setCenter(detailView);
    }

    /**
     * Zeigt das Formular zum Bearbeiten einer Kategorie an, sodass Name und Budget geändert werden können.
     *
     * @param category Die zu bearbeitende Kategorie.
     */
    private void showEditCategoryForm(Category category) {
        LoggerUtils.logInfo(CategoryDetailView.class.getName(), "Zeige Bearbeitungsformular für Kategorie: " + category.getName());

        VBox editView = new VBox(20);
        editView.getStyleClass().add("detail-view");

        TextField nameField = new TextField(category.getName());
        nameField.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");
        editView.getChildren().add(nameField);

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

            double budgetValue = category.getBudget() != null ? category.getBudget() : 1;
            ProgressBar progressBar = new ProgressBar(spent / budgetValue);
            progressBar.setPrefWidth(600);
            progressBar.setStyle("-fx-accent: " + ViewUtils.getProgressBarColor(spent, budgetValue) + ";");
            editView.getChildren().add(progressBar);
        }

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> saveCategoryChanges(category, nameField, budgetField));

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button");
        cancelButton.setOnAction(e -> showCategoryDetailView(category));

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        editView.getChildren().add(buttonBox);

        root.setCenter(editView);
    }

    /**
     * Speichert die Änderungen an der Kategorie, inklusive Name und Budget.
     *
     * @param category    Die zu aktualisierende Kategorie.
     * @param nameField   Das Textfeld für den neuen Namen der Kategorie.
     * @param budgetField Das Textfeld für das neue Budget der Kategorie.
     */
    private void saveCategoryChanges(Category category, TextField nameField, TextField budgetField) {
        try {
            String newName = nameField.getText();
            Double newBudget = null;

            if (!budgetField.getText().isEmpty()) {
                try {
                    newBudget = Double.parseDouble(budgetField.getText());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid budget amount. Please enter a valid number.", ButtonType.OK);
                    alert.showAndWait();
                    LoggerUtils.logError(CategoryDetailView.class.getName(), "Ungültiger Budgetbetrag: " + budgetField.getText(), e);
                    return;
                }
            }

            if (categoryController.isCategoryNameDuplicate(newName, category.getId())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Category name already exists.", ButtonType.OK);
                alert.showAndWait();
                LoggerUtils.logError(CategoryDetailView.class.getName(), "Kategorie-Name bereits vorhanden: " + newName, null);
                return;
            }

            category.setName(newName);
            category.setBudget(newBudget);

            categoryController.updateCategory(category);
            LoggerUtils.logInfo(CategoryDetailView.class.getName(), "Kategorie erfolgreich aktualisiert: " + category.getName());

            showCategoryDetailView(category);

        } catch (Exception ex) {
            LoggerUtils.logError(CategoryDetailView.class.getName(), "Fehler beim Aktualisieren der Kategorie: " + category.getName(), ex);
        }
    }

    /**
     * Erstellt eine Transaktionstabelle für die gegebene Kategorie.
     *
     * @param category Die Kategorie, deren Transaktionen angezeigt werden sollen.
     * @return Eine TableView mit den Transaktionen der Kategorie.
     */

    @SuppressWarnings("unchecked")
    private TableView<Transaction> createTransactionsTable(Category category) {
        LoggerUtils.logInfo(CategoryDetailView.class.getName(), "Erstelle Transaktionstabelle für Kategorie: " + category.getName());

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

        ObservableList<Transaction> filteredTransactions = FXCollections.observableArrayList(transactionController.getTransactionsByCategory(category));
        transactionsTable.setItems(filteredTransactions);


        transactionsTable.setItems(filteredTransactions);

        return transactionsTable;
    }
}
