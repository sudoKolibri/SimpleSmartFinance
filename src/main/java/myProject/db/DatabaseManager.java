package myProject.db;

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
                    + "budget DOUBLE DEFAULT NULL, "  // Allow null for the budget column
                    + "user_id VARCHAR(255), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Insert standard categories
            String[] defaultCategories = {
                    "('1', 'Income', '#50fa7b', true, false, NULL, NULL)",
                    "('2', 'Housing', '#ff5555', true, false, NULL, NULL)",
                    "('3', 'Food', '#f1fa8c', true, false, NULL, NULL)",
                    "('4', 'Transportation', '#8be9fd', true, false, NULL, NULL)",
                    "('5', 'Healthcare', '#ff79c6', true, false, NULL, NULL)"
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

            // Create transactions table for regular transactions
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "amount DOUBLE NOT NULL, "
                    + "date DATE NOT NULL, "
                    + "description VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "type VARCHAR(255), " // income or expense
                    + "account_id VARCHAR(255), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id), "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id))");

            // Create recurring transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS recurring_transactions ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "amount DOUBLE NOT NULL, "
                    + "description VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "type VARCHAR(255), " // income or expense
                    + "account_id VARCHAR(255), "
                    + "start_date DATE NOT NULL, "  // When the first transaction occurs
                    + "recurrence_interval VARCHAR(50) NOT NULL, " // daily, weekly, monthly
                    + "end_date DATE, "  // Optional, when the recurrence ends
                    + "FOREIGN KEY (category_id) REFERENCES categories(id), "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id))");

            // Create budgets table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS budgets ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "user_id VARCHAR(255) NOT NULL, "  // Add user_id column
                    + "amount DOUBLE NOT NULL, "
                    + "start_date DATE NOT NULL, "
                    + "end_date DATE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

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