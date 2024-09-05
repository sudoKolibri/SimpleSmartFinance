package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.UserController;

import java.util.Objects;

public class WelcomeView {

    private final UserController userController = new UserController();  // Controller to handle login/register

    public void start(Stage primaryStage) {
        // Set up the main VBox layout
        VBox vbox = new VBox(20);  // Spacing between elements
        vbox.setPadding(new Insets(20, 50, 20, 50));
        vbox.setAlignment(Pos.CENTER);  // Center all elements

        // Welcome label
        Label welcomeLabel = new Label("Welcome to Smart Finance");
        welcomeLabel.getStyleClass().add("welcome-label");

        // Username TextField
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        // Password PasswordField
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        // Add components to the layout
        vbox.getChildren().addAll(welcomeLabel, usernameField, passwordField,
                createLoginButton(primaryStage, usernameField, passwordField),
                createRegisterButton(usernameField, passwordField));

        // Set the scene and apply styles
        Scene scene = new Scene(vbox, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
    }

    // Method to create the Login button and its action
    private Button createLoginButton(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            boolean loginSuccessful = userController.login(username, password);

            if (loginSuccessful) {
                String loggedInUsername = userController.getLoggedInUser().getUsername();  // Get username
                String loggedInUserId = userController.getLoggedInUser().getId();  // Get userId
                new MainView().start(primaryStage, loggedInUserId, loggedInUsername);  // Pass both userId and username to MainView
            } else {
                showAlert("Login Failed", "Incorrect username or password.");
            }
        });
        return loginButton;
    }

    // Method to create the Register button and its action
    private Button createRegisterButton(TextField usernameField, PasswordField passwordField) {
        Button registerButton = new Button("Register");
        registerButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (userController.register(username, password)) {
                showAlert("Registration Successful", "You can now log in.");
            } else {
                showAlert("Registration Failed", "Username is already taken.");
            }
        });
        return registerButton;
    }

    // Helper method to show alert dialog
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
