package myProject.db;

import myProject.util.LoggerUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Diese Klasse verwaltet die Datenbankverbindungen und die Initialisierung der Datenbanktabellen.
 * Sie stellt Methoden bereit, um eine Verbindung zur Datenbank herzustellen und die notwendigen Tabellen zu erstellen.
 */
public class DatabaseManager {

    // Datenbank-URL und Anmeldeinformationen
    private static final String DB_URL = "jdbc:h2:./db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /**
     * Stellt eine Verbindung zur Datenbank her und gibt diese zurück.
     * @return Connection Objekt, das die Verbindung zur Datenbank darstellt.
     * @throws SQLException Wenn ein Fehler bei der Verbindung auftritt.
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            LoggerUtils.logError(DatabaseManager.class.getName(), "Fehler bei der Verbindung zur Datenbank.", e);
            throw e;
        }
    }

    /**
     * Initialisiert die Datenbank, indem alle erforderlichen Tabellen erstellt werden.
     * Diese Methode wird beim Start der Anwendung aufgerufen, um sicherzustellen,
     * dass alle Tabellen vorhanden sind.
     */
    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            // Erstellen der Tabelle für Benutzer
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "username VARCHAR(255) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL)");

            // Erstellen der Tabelle für Kategorien
            stmt.execute("CREATE TABLE IF NOT EXISTS categories ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "color VARCHAR(10), "
                    + "is_standard BOOLEAN NOT NULL, "
                    + "is_custom BOOLEAN NOT NULL, "
                    + "budget DOUBLE DEFAULT NULL, "
                    + "user_id VARCHAR(255), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Standardkategorien einfügen, falls nicht bereits vorhanden
            stmt.execute("MERGE INTO categories (id, name, color, is_standard, is_custom, budget, user_id) KEY (id) "
                    + "VALUES ('1', 'Income', '#50fa7b', true, false, NULL, NULL),"
                    + "('2', 'Housing', '#ff5555', true, false, NULL, NULL),"
                    + "('3', 'Food', '#f1fa8c', true, false, NULL, NULL),"
                    + "('4', 'Transportation', '#8be9fd', true, false, NULL, NULL),"
                    + "('5', 'Healthcare', '#ff79c6', true, false, NULL, NULL)");

            // Erstellen der Tabelle für Konten
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "user_id VARCHAR(255), "
                    + "name VARCHAR(255) NOT NULL, "
                    + "balance DOUBLE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Erstellen der Tabelle für Transaktionen
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "amount DOUBLE NOT NULL, "
                    + "date DATE NOT NULL, "
                    + "time TIME NOT NULL, "
                    + "description VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "type VARCHAR(255), "
                    + "status VARCHAR(20) DEFAULT 'pending', "
                    + "account_id VARCHAR(255), "
                    + "recurring_transaction_id VARCHAR(255), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id), "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id))");

            // Erstellen der Tabelle für wiederkehrende Transaktionen
            stmt.execute("CREATE TABLE IF NOT EXISTS recurring_transactions ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "amount DOUBLE NOT NULL, "
                    + "description VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "type VARCHAR(255), "
                    + "account_id VARCHAR(255), "
                    + "start_date DATE NOT NULL, "
                    + "time TIME NOT NULL, "
                    + "status VARCHAR(20) DEFAULT 'pending', "
                    + "recurrence_interval VARCHAR(50) NOT NULL, "
                    + "end_date DATE, "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id), "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id))");

            // Erstellen der Tabelle für Budgets
            stmt.execute("CREATE TABLE IF NOT EXISTS budgets ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "user_id VARCHAR(255) NOT NULL, "
                    + "amount DOUBLE NOT NULL, "
                    + "start_date DATE NOT NULL, "
                    + "end_date DATE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Erstellen der Tabelle für Budget-Kategorien
            stmt.execute("CREATE TABLE IF NOT EXISTS budget_categories ("
                    + "budget_id VARCHAR(255), "
                    + "category_id VARCHAR(255), "
                    + "FOREIGN KEY (budget_id) REFERENCES budgets(id), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id))");

        } catch (SQLException e) {
            LoggerUtils.logError(DatabaseManager.class.getName(), "Fehler bei der Datenbankinitialisierung.", e);
        }
    }
}
