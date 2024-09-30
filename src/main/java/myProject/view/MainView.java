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
import myProject.controller.AccountController;
import myProject.controller.CategoryController;
import myProject.controller.ReportController;

import java.sql.SQLException;
import java.util.Objects;

public class MainView {

    private final BorderPane root;
    private final TransactionController transactionController;
    private final AccountController accountController;
    private final CategoryController categoryController;
    private final ReportController reportController;
    private final String loggedInUserId;

    // Updated constructor to accept all necessary controllers
    public MainView(TransactionController transactionController, AccountController accountController, CategoryController categoryController,ReportController reportController, String loggedInUserId) {
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.reportController = reportController;
        this.loggedInUserId = loggedInUserId;
        this.root = new BorderPane();  // Initialize root layout here
    }

    // Adjusted start method to accept both loggedInUserId and loggedInUsername
    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) throws SQLException {
        // Root layout as BorderPane (Left: Navbar, Center: Content Area)
        setupNavigationBar();

        // Set default view to AccountsView
        showAccountsView();

        // ==================== Set Scene ====================
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    // Sets up the navigation bar with buttons for different sections
    private void setupNavigationBar() {
        VBox navBar = new VBox(20);  // Vertical layout with spacing between buttons
        navBar.setPadding(new Insets(20, 10, 20, 10));
        navBar.setStyle("-fx-background-color: #44475a;");
        navBar.setPrefWidth(150);

        // Create navigation buttons
        Button accountButton = new Button("Accounts");
        Button budgetButton = new Button("Budgets");
        Button categoryButton = new Button("Categories");
        Button reportButton = new Button("Reports");
        Button profileButton = new Button("Profile");

        // Add buttons to the navbar
        navBar.getChildren().addAll(accountButton,budgetButton, categoryButton, reportButton, profileButton);

        // Add navbar to root
        root.setLeft(navBar);

        // ==================== Button Actions for Dynamic Content ====================
        accountButton.setOnAction(e -> {
            try {
                showAccountsView();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });  // Show accounts view
        budgetButton.setOnAction(e -> showPlaceholderView());  // Placeholder for budgets
        categoryButton.setOnAction(e -> {
            try {
                showCategoryView();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });  // Show category view
        reportButton.setOnAction(e -> showReportView());  // Show the reports view
        profileButton.setOnAction(e -> showProfileView(loggedInUserId));  // Show profile view
    }

    // Method to show the Accounts view
    private void showAccountsView() throws SQLException {
        AccountView accountView = new AccountView(loggedInUserId, accountController, transactionController);
        accountView.loadIntoPane(root);
    }


    // Method to show the Category view
    private void showCategoryView() throws SQLException {
        // Pass CategoryController explicitly to the CategoryView
        CategoryView categoryView = new CategoryView(loggedInUserId, categoryController,transactionController, accountController);
        categoryView.loadIntoPane(root);
    }

    // Add the showReportView() method to MainView
    private void showReportView() {
        ReportView reportView = new ReportView(reportController);
        reportView.loadIntoPane(root);  // Load the ReportView layout into the center of MainView
    }

    // Placeholder views for sections that are not implemented yet
    private void showPlaceholderView() {
        Label placeholder = new Label("This is the " + "Budgets" + " view.");
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
