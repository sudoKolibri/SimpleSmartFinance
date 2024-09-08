package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.TransactionController;

import java.util.Objects;

public class MainView {

    private BorderPane root;
    private TransactionController transactionController;
    private String loggedInUserId;

    public MainView(TransactionController transactionController, String loggedInUserId) {
        this.transactionController = transactionController;
        this.loggedInUserId = loggedInUserId;
    }

    // Adjusted start method to accept both loggedInUserId and loggedInUsername
    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) {
        // Root layout as BorderPane (Left: Navbar, Center: Content Area)
        root = new BorderPane();

        // ==================== Left Navigation Bar ====================
        VBox navBar = new VBox(20);  // Vertical layout with spacing between buttons
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

        // Add navbar to root
        root.setLeft(navBar);

        // ==================== Button Actions for Dynamic Content ====================
        accountButton.setOnAction(e -> showAccountsView(loggedInUserId));  // Show accounts view
        transactionButton.setOnAction(e -> showTransactionsView(loggedInUserId));  // Show transactions view
        budgetButton.setOnAction(e -> showPlaceholderView("Budgets"));  // Placeholder for budgets
        categoryButton.setOnAction(e -> showCategoryView());  // Show category view
        reportButton.setOnAction(e -> showPlaceholderView("Reports"));  // Placeholder for reports
        profileButton.setOnAction(e -> showProfileView(loggedInUsername));  // Placeholder for profile

        // Set default view to AccountsView
        showAccountsView(loggedInUserId);

        // ==================== Set Scene ====================
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    // Method to show the Accounts view
    private void showAccountsView(String loggedInUserId) {
        AccountView accountView = new AccountView(loggedInUserId);
        accountView.loadIntoPane(root);
    }

    // Method to show the Transactions view
    private void showTransactionsView(String loggedInUserId) {
        GlobalTransactionsView transactionsView = new GlobalTransactionsView(transactionController, loggedInUserId);
        transactionsView.loadIntoPane(root);
    }

    // Method to show the Category view
    private void showCategoryView() {
        CategoryView categoryView = new CategoryView(loggedInUserId, root);
        categoryView.loadIntoPane();  // Load CategoryView dynamically
    }

    // Placeholder views for sections that are not implemented yet
    private void showPlaceholderView(String viewName) {
        Label placeholder = new Label("This is the " + viewName + " view.");
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox contentArea = new VBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(placeholder);
        contentArea.setStyle("-fx-background-color: #282a36;");
        contentArea.setPrefSize(600, 400);  // Adjust size for center area

        root.setCenter(contentArea);  // Set the placeholder in the center
    }

    // Placeholder for Profile view
    private void showProfileView(String loggedInUsername) {
        Label profileLabel = new Label("Profile for: " + loggedInUsername);
        profileLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox profileArea = new VBox();
        profileArea.setAlignment(Pos.CENTER);
        profileArea.getChildren().add(profileLabel);
        profileArea.setStyle("-fx-background-color: #282a36;");
        profileArea.setPrefSize(600, 400);  // Adjust size for profile area

        root.setCenter(profileArea);  // Set the profile view in the center
    }
}
