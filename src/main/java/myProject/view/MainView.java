package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

    // Konstruktor, der alle benötigten Controller akzeptiert
    public MainView(TransactionController transactionController, AccountController accountController, CategoryController categoryController, ReportController reportController, String loggedInUserId) {
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.reportController = reportController;
        this.loggedInUserId = loggedInUserId;
        this.root = new BorderPane();  // Root-Layout initialisieren
    }

    // Startmethode, die sowohl die UserId als auch den Usernamen akzeptiert
    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) throws SQLException {
        System.out.println("MainView.start: Starting MainView for user " + loggedInUsername);

        // Root-Layout als BorderPane (Links: Navbar, Mitte: Content-Bereich)
        setupNavigationBar();

        // Standardansicht auf AccountsView setzen
        showAccountsView();

        // Root BorderPane in eine ScrollPane einbinden
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // Sicherstellen, dass der Inhalt die Breite anpasst
        scrollPane.setFitToHeight(true); // Sicherstellen, dass der Inhalt die Höhe anpasst

        // Szene mit ScrollPane setzen
        Scene scene = new Scene(scrollPane, 700, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    // Setzt die Navigationsleiste mit Buttons für verschiedene Bereiche
    private void setupNavigationBar() {
        System.out.println("MainView.setupNavigationBar: Setting up navigation bar.");

        VBox navBar = new VBox(20);  // Vertikales Layout mit Abständen zwischen den Buttons
        navBar.setPadding(new Insets(20, 10, 20, 10));
        navBar.getStyleClass().add("nav-bar");

        navBar.setPrefWidth(150);

        // Navigations-Buttons erstellen
        Button accountButton = new Button("Accounts");
        Button budgetButton = new Button("Budgets");
        Button categoryButton = new Button("Categories");
        Button reportButton = new Button("Reports");
        Button profileButton = new Button("Profile");

        // Buttons zur Navbar hinzufügen
        navBar.getChildren().addAll(accountButton, budgetButton, categoryButton, reportButton, profileButton);

        // Navbar zum Root-Layout hinzufügen
        root.setLeft(navBar);

        // ==================== Button-Aktionen für dynamische Inhalte ====================
        accountButton.setOnAction(e -> {
            try {
                System.out.println("MainView.setupNavigationBar: Accounts button clicked.");
                showAccountsView();
            } catch (SQLException ex) {
                System.err.println("MainView.setupNavigationBar: Error displaying AccountsView - " + ex.getMessage());
            }
        });

        budgetButton.setOnAction(e -> showPlaceholderView("Budgets"));  // Platzhalter für Budgets

        categoryButton.setOnAction(e -> {
            try {
                System.out.println("MainView.setupNavigationBar: Categories button clicked.");
                showCategoryView();
            } catch (SQLException ex) {
                System.err.println("MainView.setupNavigationBar: Error displaying CategoryView - " + ex.getMessage());
            }
        });

        reportButton.setOnAction(e -> {
            System.out.println("MainView.setupNavigationBar: Reports button clicked.");
            showReportView();
        });

        profileButton.setOnAction(e -> {
            System.out.println("MainView.setupNavigationBar: Profile button clicked for user " + loggedInUserId);
            showProfileView(loggedInUserId);
        });
    }

    // Methode zur Anzeige der AccountsView
    private void showAccountsView() throws SQLException {
        System.out.println("MainView.showAccountsView: Displaying AccountsView.");
        AccountView accountView = new AccountView(loggedInUserId, accountController, transactionController);
        accountView.loadIntoPane(root);
    }

    // Methode zur Anzeige der CategoryView
    private void showCategoryView() throws SQLException {
        System.out.println("MainView.showCategoryView: Displaying CategoryView.");
        CategoryView categoryView = new CategoryView(loggedInUserId, categoryController, transactionController, accountController);
        categoryView.loadIntoPane(root);
    }

    // Methode zur Anzeige der ReportView
    private void showReportView() {
        System.out.println("MainView.showReportView: Displaying ReportView.");
        ReportView reportView = new ReportView(reportController);
        reportView.loadIntoPane(root);  // ReportView-Layout in die Mitte des MainViews laden
    }

    // Platzhalteransicht für noch nicht implementierte Bereiche
    private void showPlaceholderView(String sectionName) {
        System.out.println("MainView.showPlaceholderView: Displaying placeholder for " + sectionName);

        Label placeholder = new Label("This is the " + sectionName + " view.");
        placeholder.getStyleClass().add("placeholder-label");

        VBox contentArea = new VBox();
        contentArea.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(placeholder);
        contentArea.getStyleClass().add("content-area");
        contentArea.setPrefSize(600, 400);  // Größe für den zentralen Bereich anpassen

        root.setCenter(contentArea);  // Platzhalter in der Mitte setzen
    }

    // Methode zur Anzeige der Profilansicht
    private void showProfileView(String loggedInUsername) {
        System.out.println("MainView.showProfileView: Displaying profile view for " + loggedInUsername);

        Label profileLabel = new Label("Profile for: " + loggedInUsername);
        profileLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        VBox profileArea = new VBox();
        profileArea.setAlignment(Pos.CENTER);
        profileArea.getChildren().add(profileLabel);
        profileArea.setStyle("-fx-background-color: #282a36;");
        profileArea.setPrefSize(600, 400);  // Größe für den Profilbereich anpassen

        root.setCenter(profileArea);  // Profilansicht in der Mitte setzen
    }
}
