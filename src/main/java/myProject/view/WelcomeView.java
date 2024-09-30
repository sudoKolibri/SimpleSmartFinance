package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.TransactionController;
import myProject.controller.UserController;
import myProject.controller.AccountController;
import myProject.controller.CategoryController;
import myProject.controller.ReportController;  // Import ReportController
import myProject.repository.TransactionRepository;
import myProject.service.TransactionService;
import myProject.repository.AccountRepository;
import myProject.service.AccountService;
import myProject.repository.CategoryRepository;
import myProject.service.CategoryService;
import myProject.repository.BudgetRepository;  // Import BudgetRepository
import myProject.service.BudgetService;       // Import BudgetService

import java.sql.SQLException;
import java.util.Objects;

public class WelcomeView {

    // UserController instance to handle login and register actions
    private final UserController userController = new UserController();

    public void start(Stage primaryStage) {
        // Create the main layout (VBox) and set properties
        VBox vbox = new VBox(20);  // Vertical box with spacing
        vbox.setPadding(new Insets(20, 50, 20, 50));
        vbox.setAlignment(Pos.CENTER);  // Center all elements

        // Create and style the welcome label
        Label welcomeLabel = new Label("Welcome to Smart Finance");
        welcomeLabel.getStyleClass().add("welcome-label");

        // Create input fields for username and password
        TextField usernameField = createUsernameField();
        PasswordField passwordField = createPasswordField();

        // Create login and register buttons
        Button loginButton = createLoginButton(primaryStage, usernameField, passwordField);
        Button registerButton = createRegisterButton(usernameField, passwordField);

        // Add input fields and buttons to the layout
        vbox.getChildren().addAll(welcomeLabel, usernameField, passwordField, loginButton, registerButton);

        // Set up event handling for "Enter" key press
        setEnterKeyTriggers(usernameField, passwordField, loginButton);

        // Set up the scene and styles
        Scene scene = new Scene(vbox, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        // Display the scene
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
    }

    // Simplified method to create the username TextField
    private TextField createUsernameField() {
        TextField textField = new TextField();
        textField.setPromptText("Username");
        textField.setMaxWidth(250);
        return textField;
    }

    // Simplified method to create the password PasswordField
    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);
        return passwordField;
    }

    // Method to create the Login button and handle its action
    private Button createLoginButton(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            try {
                handleLogin(primaryStage, usernameField, passwordField);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return loginButton;
    }

    // New method to initialize MainView
    private MainView createMainView(String loggedInUserId, String loggedInUsername) {
        // Initialize repositories
        AccountRepository accountRepository = new AccountRepository();
        CategoryRepository categoryRepository = new CategoryRepository();
        TransactionRepository transactionRepository = new TransactionRepository(accountRepository, categoryRepository);
        BudgetRepository budgetRepository = new BudgetRepository();

        // Initialize services
        // Pass the transactionService to accountService
        CategoryService categoryService = new CategoryService(categoryRepository);
        TransactionService transactionService = new TransactionService(transactionRepository, categoryService, accountRepository);
        AccountService accountService = new AccountService(accountRepository, transactionService);
        BudgetService budgetService = new BudgetService(budgetRepository);

        // Initialize necessary controllers
        AccountController accountController = new AccountController(accountService);
        TransactionController transactionController = new TransactionController(transactionService);
        CategoryController categoryController = new CategoryController(categoryService);
        ReportController reportController = new ReportController(transactionService, accountService, categoryService, budgetService);

        // Return MainView initialized with the required controllers
        return new MainView(transactionController, accountController, categoryController, reportController, loggedInUserId);
    }



    // Updated handleLogin method using the extracted method
    private void handleLogin(Stage primaryStage, TextField usernameField, PasswordField passwordField) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean loginSuccessful = userController.login(username, password);

        if (loginSuccessful) {
            String loggedInUsername = userController.getLoggedInUser().getUsername();
            String loggedInUserId = userController.getLoggedInUser().getId();

            // Initialize MainView with the necessary details and start the main application
            MainView mainView = createMainView(loggedInUserId, loggedInUsername);
            mainView.start(primaryStage, loggedInUserId, loggedInUsername); // Start MainView using primaryStage and loggedInUsername
        } else {
            showAlert("Login Failed", "Incorrect username or password.");
        }
    }



    // Method to create the Register button and handle its action
    private Button createRegisterButton(TextField usernameField, PasswordField passwordField) {
        Button registerButton = new Button("Register");
        registerButton.setOnAction(event -> handleRegister(usernameField, passwordField));
        return registerButton;
    }

    // Handle the register action
    private void handleRegister(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userController.register(username, password)) {
            showAlert("Registration Successful", "You can now log in.");
        } else {
            showAlert("Registration Failed", "Username is already taken.");
        }
    }

    // Set event handlers for "Enter" key press to trigger login
    private void setEnterKeyTriggers(TextField usernameField, PasswordField passwordField, Button loginButton) {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                loginButton.fire();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                loginButton.fire();
            }
        });
    }

    // Helper method to show alert dialogs
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
