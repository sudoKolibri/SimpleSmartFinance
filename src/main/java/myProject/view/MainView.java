package myProject.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import myProject.controller.TransactionController;
import myProject.controller.AccountController;
import myProject.controller.CategoryController;
import myProject.controller.ReportController;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Die MainView-Klasse verwaltet die Hauptansicht der Anwendung, einschließlich Navigation und Anzeige
 * der Accounts-, Kategorien-, Report- und Help-Ansichten.
 */
public class MainView {

    private final BorderPane root;
    private final TransactionController transactionController;
    private final AccountController accountController;
    private final CategoryController categoryController;
    private final ReportController reportController;
    private final String loggedInUserId;

    /**
     * Konstruktor zur Initialisierung der MainView.
     *
     * @param transactionController Controller für Transaktionen.
     * @param accountController Controller für Konten.
     * @param categoryController Controller für Kategorien.
     * @param reportController Controller für Berichte.
     * @param loggedInUserId ID des eingeloggten Benutzers.
     */
    public MainView(TransactionController transactionController, AccountController accountController, CategoryController categoryController, ReportController reportController, String loggedInUserId) {
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.reportController = reportController;
        this.loggedInUserId = loggedInUserId;
        this.root = new BorderPane();
    }

    /**
     * Startet die Hauptansicht.
     * @param primaryStage Die Hauptbühne der Anwendung.
     * @param loggedInUserId Die ID des eingeloggten Benutzers.
     * @param loggedInUsername Der Benutzername des eingeloggten Benutzers.
     * @throws SQLException Wenn ein Fehler bei der Anzeige der Ansichten auftritt.
     */
    public void start(Stage primaryStage, String loggedInUserId, String loggedInUsername) throws SQLException {
        LoggerUtils.logInfo(MainView.class.getName(), "Starten der MainView für Benutzer: " + loggedInUsername + " mit UserID: " + loggedInUserId);

        // Navigation und Standardansicht einrichten
        setupNavigationBar();
        showAccountsView();

        // ScrollPane für das Root-Layout hinzufügen
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Szene setzen und anzeigen
        Scene scene = new Scene(scrollPane, 800, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Finance - Main View");
        primaryStage.show();
    }

    /**
     * Richtet die Navigationsleiste ein und fügt die entsprechenden Buttons hinzu.
     */
    private void setupNavigationBar() {
        LoggerUtils.logInfo(MainView.class.getName(), "Navigationsleiste wird eingerichtet.");

        VBox navBar = new VBox(20);
        navBar.setPadding(new Insets(20, 10, 20, 10));
        navBar.getStyleClass().add("nav-bar");

        navBar.setPrefWidth(150);

        // Navigation-Buttons erstellen
        Button accountButton = new Button("Finance");
        Button categoryButton = new Button("Limits");
        Button reportButton = new Button("Reports");
        Button helpButton = new Button("Help");

        // Buttons zur Navigationsleiste hinzufügen
        navBar.getChildren().addAll(accountButton, categoryButton, reportButton, helpButton);

        // Navbar zum Root-Layout hinzufügen
        root.setLeft(navBar);



        accountButton.setOnAction(e -> {
            try {
                LoggerUtils.logInfo(MainView.class.getName(), "Accounts-Button geklickt.");
                showAccountsView();
            } catch (SQLException ex) {
                LoggerUtils.logError(MainView.class.getName(), "Fehler beim Anzeigen der AccountsView: " + ex.getMessage(), ex);
            }
        });

        categoryButton.setOnAction(e -> {
            try {
                LoggerUtils.logInfo(MainView.class.getName(), "Categories-Button geklickt.");
                showCategoryView();
            } catch (SQLException ex) {
                LoggerUtils.logError(MainView.class.getName(), "Fehler beim Anzeigen der CategoryView: " + ex.getMessage(), ex);
            }
        });

        reportButton.setOnAction(e -> {
            LoggerUtils.logInfo(MainView.class.getName(), "Reports-Button geklickt.");
            showReportView();
        });

        helpButton.setOnAction(e -> {
            LoggerUtils.logInfo(MainView.class.getName(), "Help-Button geklickt.");
            showHelpView();
        });
    }

    /**
     * Zeigt die AccountsView an.
     * @throws SQLException Wenn ein Fehler beim Laden der AccountsView auftritt.
     */
    private void showAccountsView() throws SQLException {
        LoggerUtils.logInfo(MainView.class.getName(), "AccountsView wird angezeigt.");
        AccountView accountView = new AccountView(loggedInUserId, accountController, transactionController);
        accountView.loadIntoPane(root);
    }

    /**
     * Zeigt die CategoryView an.
     * @throws SQLException Wenn ein Fehler beim Laden der CategoryView auftritt.
     */
    private void showCategoryView() throws SQLException {
        LoggerUtils.logInfo(MainView.class.getName(), "CategoryView wird angezeigt.");
        CategoryView categoryView = new CategoryView(loggedInUserId, categoryController, transactionController, accountController);
        categoryView.loadIntoPane(root);
    }

    /**
     * Zeigt die ReportView an.
     */
    private void showReportView() {
        LoggerUtils.logInfo(MainView.class.getName(), "ReportView wird angezeigt für Benutzer-ID: " + loggedInUserId);

        ReportView reportView = new ReportView(reportController, loggedInUserId);
        reportView.loadIntoPane(root);
    }

    /**
     * Zeigt die HelpView an, die dem Benutzer erklärt, wie die Funktionen der Anwendung genutzt werden.
     */
    private void showHelpView() {
        LoggerUtils.logInfo(MainView.class.getName(), "HelpView wird angezeigt.");

        HelpView helpView = new HelpView(root);
        helpView.loadIntoPane();
    }
}
