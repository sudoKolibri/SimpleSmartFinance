package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.AccountController;
import myProject.controller.CategoryController;
import myProject.controller.TransactionController;
import myProject.controller.UserController;
import myProject.service.AccountService;
import myProject.service.CategoryService;
import myProject.service.TransactionService;

import java.util.Objects;

public class MainView {

    private final BorderPane root;
    private final TransactionController transactionController;
    private final CategoryController categoryController;
    private final AccountController accountController;
    private final UserController userController; // Add UserController to manage the logged-in user
    private final String loggedInUserId;

    // Modify the constructor to accept UserController
    public MainView(TransactionService transactionService, CategoryService categoryService, AccountService accountService, UserController userController, String loggedInUserId) {
        this.transactionController = new TransactionController(transactionService);
        this.categoryController = new CategoryController(categoryService);
        this.accountController = new AccountController(accountService);
        this.userController = userController;  // Set the instance of UserController
        this.loggedInUserId = loggedInUserId;
        this.root = new BorderPane(); // Initialize the root layout
    }

    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) {
        VBox navBar = new VBox(20); // Vertical layout with spacing between buttons
        navBar.setPadding(new Insets(20, 10, 20, 10));
        navBar.setStyle("-fx-background-color: #44475a;");
        navBar.setPrefWidth(150);

        // Create navigation buttons
        Button accountButton = new Button("Accounts");
        Button transactionButton = new Button("Transactions");
        Button budgetButton = new Button("Budgets");
        Button categoryButton = new Button("Categories");
        Button reportButton = new Button("Reports");
        Button profileButton = new Button("Profile");

        // Add buttons to the navbar
        navBar.getChildren().addAll(accountButton, transactionButton, budgetButton, categoryButton, reportButton, profileButton);
        root.setLeft(navBar);

        // Set actions for buttons
        accountButton.setOnAction(e -> showAccountsView());
        transactionButton.setOnAction(e -> showTransactionsView());
        budgetButton.setOnAction(e -> showPlaceholderView("Budgets"));
        categoryButton.setOnAction(e -> showCategoryView());
        reportButton.setOnAction(e -> showPlaceholderView("Reports"));
        profileButton.setOnAction(e -> showProfileView(loggedInUsername));

        // Default view: show accounts
        showAccountsView();

        // Set scene and show stage
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    // Method to show the Accounts view
    private void showAccountsView() {
        AccountView accountView = new AccountView(accountController, loggedInUserId);
        accountView.loadIntoPane(root);
    }

    // Method to show the Transactions view
    private void showTransactionsView() {
        // Pass the userController to GlobalTransactionsView so that it can fetch the logged-in user
        GlobalTransactionsView transactionsView = new GlobalTransactionsView(
                transactionController, categoryController, accountController, userController, loggedInUserId);
        transactionsView.loadIntoPane(root);
    }

    // Method to show the Category view
    private void showCategoryView() {
        CategoryView categoryView = new CategoryView(loggedInUserId, root, categoryController);
        categoryView.loadIntoPane(); // Load CategoryView dynamically
    }

    // Placeholder views for sections that are not implemented yet
    private void showPlaceholderView(String viewName) {
        Label placeholder = new Label("This is the " + viewName + " view.");
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox contentArea = new VBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(placeholder);
        contentArea.setStyle("-fx-background-color: #282a36;");
        contentArea.setPrefSize(600, 400); // Adjust size for center area

        root.setCenter(contentArea); // Set the placeholder in the center
    }

    // Placeholder for Profile view
    private void showProfileView(String loggedInUsername) {
        Label profileLabel = new Label("Profile for: " + loggedInUsername);
        profileLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox profileArea = new VBox();
        profileArea.setAlignment(Pos.CENTER);
        profileArea.getChildren().add(profileLabel);
        profileArea.setStyle("-fx-background-color: #282a36;");
        profileArea.setPrefSize(600, 400); // Adjust size for profile area

        root.setCenter(profileArea); // Set the profile view in the center
    }
}
