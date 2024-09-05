package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class MainView {

    private BorderPane root;  // Root layout for navigation and dynamic content

    // Adjusted start method to accept both loggedInUserId and loggedInUsername
    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) {
        // Create the root layout as BorderPane (Left: Navbar, Center: Content Area)
        root = new BorderPane();

        // ==================== Left Navigation Bar ====================
        VBox navBar = new VBox(20);  // Vertical layout with spacing between buttons
        navBar.setPadding(new Insets(20, 10, 20, 10));
        navBar.setStyle("-fx-background-color: #44475a;");
        navBar.setPrefWidth(150);  // Set a fixed width for the navbar

        // Create buttons for navigation
        Button accountButton = new Button("Accounts");
        Button transactionButton = new Button("Transactions");
        Button budgetButton = new Button("Budgets");
        Button categoryButton = new Button("Categories");
        Button reportButton = new Button("Reports");
        Button profileButton = new Button("Profile");

        // Add buttons to the navbar
        navBar.getChildren().addAll(accountButton, transactionButton, budgetButton, categoryButton, reportButton, profileButton);

        // ==================== Right Dynamic Content Area ====================
        GridPane contentArea = new GridPane();
        contentArea.setAlignment(Pos.CENTER);  // Center the content
        contentArea.setStyle("-fx-background-color: #282a36;");
        contentArea.setPrefSize(500, 400);  // Set preferred size for the content area

        // Add navbar and content area to the root layout
        root.setLeft(navBar);
        root.setCenter(contentArea);

        // ==================== Button Actions for Dynamic Content ====================
        accountButton.setOnAction(e -> showAccountsView(loggedInUserId, loggedInUsername));  // Pass both userId and username
        transactionButton.setOnAction(e -> showPlaceholderView("Transactions"));
        budgetButton.setOnAction(e -> showPlaceholderView("Budgets"));
        categoryButton.setOnAction(e -> new CategoryView(loggedInUserId).loadIntoPane(root));  // Load CategoryView dynamically

        reportButton.setOnAction(e -> showPlaceholderView("Reports"));
        profileButton.setOnAction(e -> showPlaceholderView("Profile"));

        // Set default view to AccountsView after login
        showAccountsView(loggedInUserId, loggedInUsername);

        // ==================== Set Scene ====================
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    // Method to show AccountsView in the center pane, using userId for internal operations and username for display
    // Method to show AccountsView in the center pane
    private void showAccountsView(String loggedInUserId, String loggedInUsername) {
        AccountView accountView = new AccountView(loggedInUserId);  // Instantiate AccountView
        accountView.loadIntoPane(root);  // Call a method to load AccountView into the dynamic area
    }


    // Placeholder method for other views (like Transactions, Budgets, etc.)
    private void showPlaceholderView(String viewName) {
        Label placeholder = new Label("This is the " + viewName + " view.");
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox contentArea = new VBox();
        contentArea.setAlignment(Pos.CENTER);  // Center the content
        contentArea.getChildren().add(placeholder);
        contentArea.setStyle("-fx-background-color: #282a36;");
        contentArea.setPrefSize(500, 400);

        // Set the center of the root layout to the placeholder content
        root.setCenter(contentArea);
    }

}
