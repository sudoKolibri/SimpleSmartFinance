package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Das TransactionRepository ist für die Datenbankzugriffe im Zusammenhang mit Transaktionen verantwortlich.
 * Hier werden Transaktionen in die Datenbank eingefügt, abgerufen, aktualisiert und gelöscht.
 */
public class TransactionRepository {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionRepository(AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Speichert eine neue Transaktion in der Datenbank.
     * @param transaction Die hinzuzufügende Transaktion.
     * @return true, wenn die Transaktion erfolgreich gespeichert wurde, false bei einem Fehler.
     */
    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (id, amount, date, time, description, category_id, type, account_id, status, recurring_transaction_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setDate(3, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(4, transaction.getTime());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setString(6, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(7, transaction.getType());
            pstmt.setString(8, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setString(9, transaction.getStatus());
            pstmt.setString(10, transaction.getRecurringTransactionId() != null ? transaction.getRecurringTransactionId() : null);
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktion erfolgreich gespeichert: " + transaction.getId());
            return true;
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Speichern der Transaktion: " + transaction.getId(), e);
            return false;
        }
    }

    /**
     * Speichert eine neue wiederkehrende Transaktion in der Datenbank.
     *
     * @param transaction Die hinzuzufügende wiederkehrende Transaktion.
     */
    public void saveRecurringTransaction(Transaction transaction) {
        String sql = "INSERT INTO recurring_transactions (id, amount, description, category_id, type, account_id, start_date, time, recurrence_interval, end_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(5, transaction.getType());
            pstmt.setString(6, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setDate(7, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(8, transaction.getTime());
            pstmt.setString(9, transaction.getRecurrenceInterval());
            pstmt.setDate(10, transaction.getEndDate() != null ? new java.sql.Date(transaction.getEndDate().getTime()) : null);
            pstmt.setString(11, transaction.getStatus());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Wiederkehrende Transaktion erfolgreich gespeichert: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Speichern der wiederkehrenden Transaktion: " + transaction.getId(), e);
        }
    }

    /**
     * Aktualisiert eine bestehende Transaktion in der Datenbank.
     *
     * @param transaction Die zu aktualisierende Transaktion.
     */
    public void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, time = ?, description = ?, category_id = ?, type = ?, account_id = ?, status = ?, recurring_transaction_id = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(3, transaction.getTime());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(6, transaction.getType());
            pstmt.setString(7, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setString(8, transaction.getStatus());
            pstmt.setString(9, transaction.getRecurringTransactionId() != null ? transaction.getRecurringTransactionId() : null);
            pstmt.setString(10, transaction.getId());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktion erfolgreich aktualisiert: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Aktualisieren der Transaktion: " + transaction.getId(), e);
        }
    }

    /**
     * Aktualisiert eine bestehende wiederkehrende Transaktion in der Datenbank.
     *
     * @param transaction Die zu aktualisierende wiederkehrende Transaktion.
     */
    public void updateRecurringTransaction(Transaction transaction) {
        String sql = "UPDATE recurring_transactions SET amount = ?, description = ?, category_id = ?, type = ?, account_id = ?, start_date = ?, recurrence_interval = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setString(3, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(4, transaction.getType());
            pstmt.setString(5, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setDate(6, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setString(7, transaction.getRecurrenceInterval());
            pstmt.setDate(8, transaction.getEndDate() != null ? new java.sql.Date(transaction.getEndDate().getTime()) : null);
            pstmt.setString(9, transaction.getStatus());
            pstmt.setString(10, transaction.getId());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Wiederkehrende Transaktion erfolgreich aktualisiert: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Aktualisieren der wiederkehrenden Transaktion: " + transaction.getId(), e);
        }
    }

    /**
     * Löscht eine Transaktion aus der Datenbank.
     *
     * @param transaction Die zu löschende Transaktion.
     */
    public void deleteTransaction(Transaction transaction) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktion erfolgreich gelöscht: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Löschen der Transaktion: " + transaction.getId(), e);
        }
    }

    /**
     * Löscht eine wiederkehrende Transaktion aus der Datenbank.
     *
     * @param transaction Die zu Löschende wiederkehrende Transaktion.
     */
    public void deleteRecurringTransaction(Transaction transaction) {
        String sql = "DELETE FROM recurring_transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Wiederkehrende Transaktion erfolgreich gelöscht: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Löschen der wiederkehrenden Transaktion: " + transaction.getId(), e);
        }
    }

    /**
     * Löscht ausstehende Transaktionen anhand der recurring_transaction_id aus der Datenbank.
     *
     * @param recurringTransactionId Die ID der wiederkehrenden Transaktion.
     */
    public void deletePendingTransactionsByRecurringId(String recurringTransactionId) {
        String sql = "DELETE FROM transactions WHERE recurring_transaction_id = ? AND status = 'pending'";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recurringTransactionId);
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Ausstehende Transaktionen erfolgreich gelöscht für ID: " + recurringTransactionId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Löschen ausstehender Transaktionen: " + recurringTransactionId, e);
        }
    }

    /**
     * Ruft alle Transaktionen aus der Datenbank ab.
     * @return Eine Liste aller Transaktionen.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection connection = DatabaseManager.getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs, false);
                transactions.add(transaction);
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Alle Transaktionen erfolgreich abgerufen.");
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen aller Transaktionen: " + e.getMessage(), e);
        }
        return transactions;
    }

    /**
     * Ruft die Transaktionen für ein bestimmtes Konto ab.
     * @param accountName Der Name des Kontos.
     * @return Eine Liste der Transaktionen für das Konto.
     */
    public List<Transaction> getTransactionsByAccount(String accountName) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = (SELECT id FROM accounts WHERE name = ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs, false);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktionen erfolgreich abgerufen für Konto: " + accountName);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen der Transaktionen für Konto: " + accountName, e);
        }
        return transactions;
    }

    /**
     * Ruft alle wiederkehrenden Transaktionen für ein bestimmtes Konto ab.
     * @param accountId Die ID des Kontos.
     * @return Eine Liste der wiederkehrenden Transaktionen für das Konto.
     */
    public List<Transaction> getRecurringTransactionsByAccount(String accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE account_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs, true);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Wiederkehrende Transaktionen erfolgreich abgerufen für Konto-ID: " + accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen wiederkehrender Transaktionen für Konto-ID: " + accountId, e);
        }
        return transactions;
    }

    /**
     * Ruft alle Transaktionen ab, die einer bestimmten Kategorie zugeordnet sind.
     * @param categoryId Die ID der Kategorie.
     * @return Eine Liste der Transaktionen für die Kategorie.
     */
    public List<Transaction> getTransactionsByCategory(String categoryId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE category_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs, false);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktionen erfolgreich abgerufen für Kategorie-ID: " + categoryId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen der Transaktionen für Kategorie-ID: " + categoryId, e);
            throw new RuntimeException("Fehler beim Speichern der Transaktion: " + e.getMessage(), e);
        }

        transactions.addAll(getRecurringTransactionsByCategory(categoryId));
        return transactions;
    }

    /**
     * Ruft alle wiederkehrenden Transaktionen ab, die einer bestimmten Kategorie zugeordnet sind.
     * @param categoryId Die ID der Kategorie.
     * @return Eine Liste der wiederkehrenden Transaktionen für die Kategorie.
     */
    public List<Transaction> getRecurringTransactionsByCategory(String categoryId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE category_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs, true);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Wiederkehrende Transaktionen erfolgreich abgerufen für Kategorie-ID: " + categoryId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen wiederkehrender Transaktionen für Kategorie-ID: " + categoryId, e);
            throw new RuntimeException("Fehler beim Speichern der Transaktion: " + e.getMessage(), e);
        }
        return transactions;
    }

    // Hilfsmethode zum Mapping eines ResultSet auf ein Transaction-Objekt, mit Berücksichtigung von wiederkehrenden Transaktionen.
    private Transaction mapResultSetToTransaction(ResultSet rs, boolean isRecurring) throws SQLException {
        Category category = categoryRepository.findCategoryById(rs.getString("category_id"));
        Account account = accountRepository.findAccountById(rs.getString("account_id"));
        Date date = rs.getDate(columnExists(rs, "start_date") ? "start_date" : "date");
        Time time = rs.getTime("time");

        Transaction transaction = new Transaction(rs.getString("description"), rs.getDouble("amount"), rs.getString("type"), null, account, category, date, time, rs.getString("status"));
        transaction.setId(rs.getString("id"));
        transaction.setRecurring(isRecurring);

        // Only set the recurrence interval and end date if the transaction is recurring
        if (isRecurring && columnExists(rs, "recurrence_interval")) {
            transaction.setRecurrenceInterval(rs.getString("recurrence_interval"));
            transaction.setEndDate(rs.getDate("end_date"));
        }

        // Map the recurring_transaction_id if it's a regular transaction
        if (!isRecurring && columnExists(rs, "recurring_transaction_id")) {
            transaction.setRecurringTransactionId(rs.getString("recurring_transaction_id"));
        }

        return transaction;
    }

    // Hilfsmethode, um zu prüfen, ob eine bestimmte Spalte in einem ResultSet existiert.
    private boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
