package myProject.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // Database URL and credentials
    private static final String DB_URL = "jdbc:h2:./db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Initialize the database with tables (without dropping tables)
    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            // Create users table first
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "username VARCHAR(255) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL)");

            // Create categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "color VARCHAR(10), "
                    + "is_standard BOOLEAN NOT NULL, "
                    + "is_custom BOOLEAN NOT NULL, "
                    + "budget DOUBLE DEFAULT 0.0, "
                    + "user_id VARCHAR(255), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Insert standard categories
            String[] defaultCategories = {
                    "('1', 'Income', '#50fa7b', true, false, 0.0, NULL)",
                    "('2', 'Housing', '#ff5555', true, false, 0.0, NULL)",
                    "('3', 'Food', '#f1fa8c', true, false, 0.0, NULL)",
                    "('4', 'Transportation', '#8be9fd', true, false, 0.0, NULL)",
                    "('5', 'Healthcare', '#ff79c6', true, false, 0.0, NULL)"
            };

            for (String category : defaultCategories) {
                stmt.executeUpdate("MERGE INTO categories (id, name, color, is_standard, is_custom, budget, user_id) "
                        + "KEY(id) VALUES " + category);
            }

            // Create accounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "user_id VARCHAR(255), "
                    + "name VARCHAR(255) NOT NULL, "
                    + "balance DOUBLE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "amount DOUBLE NOT NULL, "
                    + "date DATE NOT NULL, "
                    + "description VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "type VARCHAR(255), "
                    + "account_id VARCHAR(255), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id), "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id))");


            // Create Second table for reoccurring transactions.

            // Create budgets table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS budgets ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "user_id VARCHAR(255) NOT NULL, "  // Add user_id column
                    + "amount DOUBLE NOT NULL, "
                    + "start_date DATE NOT NULL, "
                    + "end_date DATE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");  // Link user_id to users table


            // Create budget_categories table to link multiple categories to a single budget
            stmt.execute("CREATE TABLE IF NOT EXISTS budget_categories ("
                    + "budget_id VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "FOREIGN KEY (budget_id) REFERENCES budgets(id), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id))");

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
