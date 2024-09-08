package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.UserController;
import myProject.repository.TransactionRepository;
import myProject.repository.CategoryRepository;
import myProject.repository.AccountRepository;
import myProject.service.TransactionService;
import myProject.service.CategoryService;
import myProject.service.AccountService;

import java.util.Objects;

public class WelcomeView {

    private final UserController userController = new UserController();

    public void start(Stage primaryStage) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20, 50, 20, 50));
        vbox.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome to Smart Finance");
        welcomeLabel.getStyleClass().add("welcome-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> handleLogin(primaryStage, usernameField, passwordField));

        Button registerButton = new Button("Register");
        registerButton.setOnAction(event -> handleRegister(usernameField, passwordField));

        vbox.getChildren().addAll(welcomeLabel, usernameField, passwordField, loginButton, registerButton);

        Scene scene = new Scene(vbox, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
    }

    private void handleLogin(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean loginSuccessful = userController.login(username, password);

        if (loginSuccessful) {
            String loggedInUserId = userController.getLoggedInUser().getId();
            String loggedInUsername = userController.getLoggedInUser().getUsername();

            TransactionService transactionService = new TransactionService(new TransactionRepository());
            CategoryService categoryService = new CategoryService(new CategoryRepository());
            AccountService accountService = new AccountService(new AccountRepository());

            MainView mainView = new MainView(transactionService, categoryService, accountService, userController, loggedInUserId);  // Pass userController to MainView
            mainView.start(primaryStage, loggedInUserId, loggedInUsername);
        } else {
            showAlert("Login Failed", "Incorrect username or password.");
        }
    }


    private void handleRegister(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userController.register(username, password)) {
            showAlert("Registration Successful", "You can now log in.");
        } else {
            showAlert("Registration Failed", "Username is already taken.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
