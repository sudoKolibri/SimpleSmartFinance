package myProject;

import myProject.db.DatabaseManager;
import myProject.view.WelcomeView;
import javafx.application.Application;
import javafx.stage.Stage;
import myProject.util.LoggerUtils;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Datenbank initialisieren
        initializeDatabase();

        // WelcomeView anzeigen
        showWelcomeView(primaryStage);
    }

    // Methode zur Initialisierung der Datenbank
    private void initializeDatabase() {
        try {
            LoggerUtils.logInfo(Main.class.getName(), "Datenbank wird initialisiert.");
            DatabaseManager.initializeDatabase();
            LoggerUtils.logInfo(Main.class.getName(), "Datenbank erfolgreich initialisiert.");
        } catch (Exception e) {
            LoggerUtils.logError(Main.class.getName(), "Fehler bei der Initialisierung der Datenbank: " + e.getMessage(), e);
        }
    }

    // Methode zur Anzeige der WelcomeView
    private void showWelcomeView(Stage primaryStage) {
        WelcomeView welcomeView = new WelcomeView();
        try {
            welcomeView.start(primaryStage);
            LoggerUtils.logInfo(Main.class.getName(), "WelcomeView erfolgreich angezeigt.");
        } catch (Exception e) {
            LoggerUtils.logError(Main.class.getName(), "Fehler beim Laden der WelcomeView: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        // Startet die JavaFX-Anwendung
        launch(args);
    }

}