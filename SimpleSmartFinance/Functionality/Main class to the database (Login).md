### 1. **Main Class**

The **Main** class is the entry point of your application and starts everything.

- **Method**: `start(Stage primaryStage)`
- **Functionality**:
    - Initializes the database by calling `DatabaseManager.initializeDatabase()`.
    - Shows the **WelcomeView**, which is the login and registration screen.

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();
        WelcomeView welcomeView = new WelcomeView();
        welcomeView.start(primaryStage);
    }
}
