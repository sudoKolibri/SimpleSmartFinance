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
import myProject.controller.ReportController;
import myProject.repository.TransactionRepository;
import myProject.service.TransactionService;
import myProject.repository.AccountRepository;
import myProject.service.AccountService;
import myProject.repository.CategoryRepository;
import myProject.service.CategoryService;
import myProject.repository.BudgetRepository;
import myProject.service.BudgetService;

import java.sql.SQLException;
import java.util.Objects;

public class WelcomeView {

    // Instanz des UserControllers zur Handhabung von Login- und Registrierungsaktionen
    private final UserController userController = new UserController();

    public void start(Stage primaryStage) {
        // Hauptlayout (VBox) erstellen und Eigenschaften setzen
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20, 50, 20, 50));
        vbox.setAlignment(Pos.CENTER);

        // Begrüßungstext erstellen und formatieren
        Label welcomeLabel = new Label("Welcome to Smart Finance");
        welcomeLabel.getStyleClass().add("welcome-label");

        // Eingabefelder für Benutzername und Passwort erstellen
        TextField usernameField = createUsernameField();
        PasswordField passwordField = createPasswordField();

        // Login- und Registrierungsbuttons erstellen
        Button loginButton = createLoginButton(primaryStage, usernameField, passwordField);
        Button registerButton = createRegisterButton(usernameField, passwordField);

        // **Enter-KeyTrigger für Login aktivieren**
        setEnterKeyTriggers(usernameField, passwordField, loginButton);  // Aufruf der Methode hier

        // Eingabefelder und Buttons zum Layout hinzufügen
        vbox.getChildren().addAll(welcomeLabel, usernameField, passwordField, loginButton, registerButton);

        // VBox in eine ScrollPane einfügen
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);  // Passt den Inhalt an die Breite des ScrollPane an
        scrollPane.setFitToHeight(true); // Passt den Inhalt an die Höhe des ScrollPane an

        // Szene und Layout erstellen
        Scene scene = new Scene(scrollPane, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        // Szene anzeigen
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
    }

    // Methode zum Erstellen des Eingabefeldes für den Benutzernamen
    private TextField createUsernameField() {
        TextField textField = new TextField();
        textField.setPromptText("Username");
        textField.setMaxWidth(250);
        return textField;
    }

    // Methode zum Erstellen des Eingabefeldes für das Passwort
    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);
        return passwordField;
    }

    // Methode zum Erstellen des Login-Buttons und der entsprechenden Aktion
    private Button createLoginButton(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            try {
                handleLogin(primaryStage, usernameField, passwordField);
            } catch (SQLException e) {
                System.err.println("WelcomeView.createLoginButton: Error during login - " + e.getMessage());
            }
        });
        return loginButton;
    }

    // Methode zum Initialisieren der MainView
    private MainView createMainView(String loggedInUserId, String loggedInUsername) {
        // Repositories initialisieren
        AccountRepository accountRepository = new AccountRepository();
        CategoryRepository categoryRepository = new CategoryRepository();
        TransactionRepository transactionRepository = new TransactionRepository(accountRepository, categoryRepository);
        BudgetRepository budgetRepository = new BudgetRepository();

        // Services initialisieren
        CategoryService categoryService = new CategoryService(categoryRepository);
        TransactionService transactionService = new TransactionService(transactionRepository, categoryService, accountRepository);
        AccountService accountService = new AccountService(accountRepository, transactionService);
        BudgetService budgetService = new BudgetService(budgetRepository);

        // Controller initialisieren
        AccountController accountController = new AccountController(accountService);
        TransactionController transactionController = new TransactionController(transactionService);
        CategoryController categoryController = new CategoryController(categoryService);
        ReportController reportController = new ReportController(transactionService, accountService, categoryService, budgetService);

        // MainView zurückgeben, initialisiert mit den benötigten Controllern
        return new MainView(transactionController, accountController, categoryController, reportController, loggedInUserId);
    }

    // Methode zur Handhabung des Logins
    private void handleLogin(Stage primaryStage, TextField usernameField, PasswordField passwordField) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean loginSuccessful = userController.login(username, password);

        if (loginSuccessful) {
            String loggedInUsername = userController.getLoggedInUser().getUsername();
            String loggedInUserId = userController.getLoggedInUser().getId();

            // MainView initialisieren und die Hauptanwendung starten
            MainView mainView = createMainView(loggedInUserId, loggedInUsername);
            mainView.start(primaryStage, loggedInUserId, loggedInUsername);
        } else {
            showAlert("Login Failed", "Incorrect username or password.");
        }
    }

    // Methode zum Erstellen des Registrierungsbuttons und der entsprechenden Aktion
    private Button createRegisterButton(TextField usernameField, PasswordField passwordField) {
        Button registerButton = new Button("Register");
        registerButton.setOnAction(event -> handleRegister(usernameField, passwordField));
        return registerButton;
    }

    // Methode zur Handhabung der Registrierung
    private void handleRegister(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userController.register(username, password)) {
            showAlert("Registration Successful", "You can now log in.");
        } else {
            showAlert("Registration Failed", "Username is already taken.");
        }
    }

    // Methode, um "Enter"-Tastenereignisse zum Auslösen des Logins zu setzen
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

    // Hilfsmethode zur Anzeige von Alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
