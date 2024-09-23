package myProject;

import myProject.db.DatabaseManager;
import myProject.view.WelcomeView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database
        initializeDatabase();

        // Show the WelcomeView
        showWelcomeView(primaryStage);
    }

    // Method to initialize the database
    private void initializeDatabase() {
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to show the WelcomeView
    private void showWelcomeView(Stage primaryStage) {
        WelcomeView welcomeView = new WelcomeView();
        try {
            welcomeView.start(primaryStage);
        } catch (Exception e) {
            System.err.println("Error loading WelcomeView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}
