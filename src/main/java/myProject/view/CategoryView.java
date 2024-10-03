package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import myProject.controller.AccountController;
import myProject.controller.CategoryController;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.view.detail.CategoryDetailView;
import myProject.view.util.ViewUtils;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import static myProject.view.util.ViewUtils.getCategoryBudget;

/**
 * Die CategoryView-Klasse ist verantwortlich für die Verwaltung und Anzeige von Kategorien,
 * einschließlich der Erstellung und Übersicht über bestehende Kategorien.
 */
public class CategoryView {

    private final CategoryController categoryController;
    private final TransactionController transactionController;
    private final AccountController accountController; // AccountController zur Verwaltung von Konten
    private final String currentUserId;
    private VBox mainLayout;  // Hauptlayout zur Anzeige des Inhalts
    private BorderPane root;
    private Label overallBalanceLabel; // Label zur Anzeige der Gesamtbilanz
    private Button createCategoryButton;  // Button zur Erstellung neuer Kategorien

    /**
     * Konstruktor zur Initialisierung der View mit den notwendigen Abhängigkeiten.
     *
     * @param currentUserId       Die ID des aktuellen Benutzers.
     * @param categoryController  Der Controller zur Verwaltung der Kategorien.
     * @param transactionController Der Controller zur Verwaltung der Transaktionen.
     * @param accountController   Der Controller zur Verwaltung der Konten.
     */
    public CategoryView(String currentUserId, CategoryController categoryController, TransactionController transactionController, AccountController accountController) {
        this.currentUserId = currentUserId;
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        this.accountController = accountController; // AccountController initialisieren
    }

    /**
     * Lädt den Hauptinhalt in das angegebene Root-Pane.
     *
     * @param root Das Root-Layout, in das der Inhalt geladen werden soll.
     * @throws SQLException Wenn ein Fehler beim Laden der Kategorien auftritt.
     */
    public void loadIntoPane(BorderPane root) throws SQLException {
        if (root == null) {
            LoggerUtils.logError(CategoryView.class.getName(), "Root-Layout ist null. Kann CategoryView nicht laden.", null);
            return;
        }

        this.root = root;
        mainLayout = new VBox(10);
        mainLayout.getStyleClass().add("main-layout");
        mainLayout.setAlignment(Pos.CENTER);

        // Header Label
        Label headerLabel = new Label("Categories & Budgets");
        headerLabel.getStyleClass().add("header-label");
        mainLayout.getChildren().add(headerLabel);

        // Erstelle und füge das Summary-Layout hinzu
        VBox summaryLayout = createSummaryLayout();
        mainLayout.getChildren().add(summaryLayout);

        // Zeige die Kategorien an
        showCategories();

        // Button zum Hinzufügen neuer Kategorien konfigurieren
        createCategoryButton = new Button("+ Add Category");
        createCategoryButton.getStyleClass().add("create-button");
        createCategoryButton.setOnAction(e -> {
            createCategoryButton.setVisible(false);
            showCreateCategoryForm(root);
        });

        mainLayout.getChildren().add(createCategoryButton);
        this.root.setCenter(mainLayout);
    }


    /**
     * Erstellt das Summary-Layout zur Anzeige der Gesamtbilanz und einzelner Kontenbilanzen.
     *
     * @return Das erstellte VBox-Layout.
     * @throws SQLException Wenn ein Fehler beim Abrufen der Kontodaten auftritt.
     */
    private VBox createSummaryLayout() throws SQLException {
        VBox summaryLayout = new VBox(10);
        summaryLayout.setAlignment(Pos.CENTER);

        // Gesamtbilanz anzeigen
        overallBalanceLabel = new Label();
        overallBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #50fa7b;");
        updateOverallBalance();
        summaryLayout.getChildren().add(overallBalanceLabel);

        // Einzelne Kontenbilanzen anzeigen
        List<Account> accounts = accountController.getAllAccountsForUser(currentUserId);
        for (Account account : accounts) {
            Label accountBalanceLabel = new Label(account.getName() + ": $" + String.format("%.2f", account.getBalance()));
            accountBalanceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #dd7a42;");
            summaryLayout.getChildren().add(accountBalanceLabel);
        }

        return summaryLayout;
    }

    /**
     * Aktualisiert die Gesamtbilanz für den aktuellen Benutzer.
     *
     * @throws SQLException Wenn ein Fehler beim Abrufen der Bilanz auftritt.
     */
    private void updateOverallBalance() throws SQLException {
        double totalBalance = accountController.getOverallBalanceForUser(currentUserId);
        overallBalanceLabel.setText("Total Balance: $" + String.format("%.2f", totalBalance));
        LoggerUtils.logInfo(CategoryView.class.getName(), "Gesamtbilanz aktualisiert: " + totalBalance);
    }

    /**
     * Zeigt alle Kategorien (Standard- und benutzerdefinierte) in einem Grid-Layout an.
     */
    private void showCategories() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        List<Category> categories = categoryController.getAllCategoriesForUser(currentUserId)
                .stream()
                .filter(category -> !category.getId().equals("no_category_id"))
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

    /**
     * Erstellt eine Kategorie-Karte mit Details wie Budget und Ausgaben.
     *
     * @param category Die Kategorie, für die die Karte erstellt wird.
     * @return Das erstellte VBox-Layout für die Kategorie-Karte.
     */
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

            double spent = Math.abs(transactionController.getSpentAmountForCategory(category));  // Betrag absolut anzeigen
            Label spentLabel = new Label("$" + spent + " Spent");  // Ausgaben anzeigen
            spentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff79c6;");
            card.getChildren().add(spentLabel);

            ProgressBar progressBar = new ProgressBar(spent / category.getBudget());  // Fortschritt basierend auf Ausgaben
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

    /**
     * Zeigt die Detailansicht einer Kategorie an.
     *
     * @param category Die Kategorie, die angezeigt werden soll.
     */
    private void showCategoryDetailView(Category category) {
        if (root == null) {
            LoggerUtils.logError(CategoryView.class.getName(), "Root-Layout ist null. Kann CategoryDetailView nicht anzeigen.", null);
            return;
        }

        // Überprüfen, ob die Kategorie die "No Category" Kategorie ist
        if (category.getId().equals("no_category_id")) {
            LoggerUtils.logInfo(CategoryView.class.getName(), "'No Category' kann nicht gelöscht oder bearbeitet werden.");
        }

        CategoryDetailView categoryDetailView = new CategoryDetailView(categoryController, transactionController, accountController, currentUserId, root);
        categoryDetailView.showCategoryDetailView(category);

        LoggerUtils.logInfo(CategoryView.class.getName(), "Detailansicht für Kategorie angezeigt: " + category.getName());
    }


    /**
     * Zeigt das Formular zur Erstellung einer neuen Kategorie an.
     *
     * @param root Das Root-Layout, in dem das Formular angezeigt werden soll.
     */
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

        // Erstelle den "Cancel"-Button zum Abbrechen des Formulars
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            mainLayout.getChildren().remove(formCard);  // Entferne das Formular
            createCategoryButton.setVisible(true);  // Zeige den "Add Category"-Button wieder an
        });

        formCard.getChildren().addAll(new Label("New Category"), nameField, budgetField, submitButton, cancelButton);
        mainLayout.getChildren().add(formCard);
    }

    /**
     * Erstellt den Submit-Button für das Formular zur Kategorie erstellung.
     *
     * @param nameField  Das Textfeld für den Kategorienamen.
     * @param budgetField Das Textfeld für das Budget.
     * @param root       Das Root-Layout, in dem die Kategorien angezeigt werden.
     * @return Der erstellte Button.
     */
    private Button createSubmitButton(TextField nameField, TextField budgetField, BorderPane root) {
        this.root = root;
        Button submitButton = new Button("Create");
        submitButton.setOnAction(e -> {
            String categoryName = nameField.getText();
            Double categoryBudget = getCategoryBudget(budgetField);

            Category newCategory = new Category(null, categoryName, false, true, categoryBudget);
            categoryController.addCategory(newCategory, currentUserId);
            LoggerUtils.logInfo(CategoryView.class.getName(), "Neue Kategorie erstellt: " + categoryName);

            mainLayout.getChildren().clear();
            try {
                loadIntoPane(this.root);  // Lade die Ansicht neu, um die Kategorien zu aktualisieren
            } catch (SQLException ex) {
                LoggerUtils.logError(CategoryView.class.getName(), "Fehler beim Neuladen der CategoryView nach dem Erstellen der Kategorie", ex);
                throw new RuntimeException(ex);
            }
        });

        return submitButton;
    }
}
