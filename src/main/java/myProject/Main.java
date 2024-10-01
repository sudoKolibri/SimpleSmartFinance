package myProject;

import myProject.db.DatabaseManager;
import myProject.view.WelcomeView;
import javafx.application.Application;
import javafx.stage.Stage;

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
            System.out.println("Main.initializeDatabase: Initializing database.");
            DatabaseManager.initializeDatabase();
            System.out.println("Main.initializeDatabase: Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Main.initializeDatabase: Error during database initialization - " + e.getMessage());
        }
    }

    // Methode zur Anzeige der WelcomeView
    private void showWelcomeView(Stage primaryStage) {
        WelcomeView welcomeView = new WelcomeView();
        try {
            System.out.println("Main.showWelcomeView: Displaying WelcomeView.");
            welcomeView.start(primaryStage);
            System.out.println("Main.showWelcomeView: WelcomeView displayed successfully.");
        } catch (Exception e) {
            System.err.println("Main.showWelcomeView: Error loading WelcomeView - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Startet die JavaFX-Anwendung
        launch(args);
    }
}
