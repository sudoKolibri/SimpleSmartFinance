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
import myProject.repository.TransactionRepository;
import myProject.service.TransactionService;
import myProject.repository.AccountRepository;
import myProject.service.AccountService;
import myProject.repository.CategoryRepository;
import myProject.service.CategoryService;

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
        loginButton.setOnAction(event -> handleLogin(primaryStage, usernameField, passwordField));
        return loginButton;
    }

    // Handle the login action
    private void handleLogin(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean loginSuccessful = userController.login(username, password);

        if (loginSuccessful) {
            String loggedInUsername = userController.getLoggedInUser().getUsername();
            String loggedInUserId = userController.getLoggedInUser().getId();

            // Initialize necessary controllers with the appropriate services and repositories
            AccountController accountController = new AccountController(new AccountService(new AccountRepository()));
            TransactionController transactionController = new TransactionController(new TransactionService(new TransactionRepository()));
            CategoryController categoryController = new CategoryController(new CategoryService(new CategoryRepository()));

            // Pass the correct parameters to MainView
            MainView mainView = new MainView(transactionController, accountController, categoryController, loggedInUserId);
            mainView.start(primaryStage, loggedInUserId, loggedInUsername);  // Pass the user ID and username to MainView

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
                loginButton.fire();  // Simulate login button click
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                loginButton.fire();  // Simulate login button click
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
