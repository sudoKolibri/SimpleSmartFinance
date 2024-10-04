package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.time.LocalDate;
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
     *
     * @param transaction Die hinzuzufügende Transaktion.
     */
    public void saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (id, amount, date, time, description, category_id, type, account_id) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setDate(3, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(4, transaction.getTime());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setString(6, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(7, transaction.getType());
            pstmt.setString(8, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktion erfolgreich gespeichert: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Speichern der Transaktion: " + transaction.getId(), e);
        }
    }


    /**
     * Aktualisiert eine bestehende Transaktion in der Datenbank.
     *
     * @param transaction Die zu aktualisierende Transaktion.
     */
    public void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, time = ?, description = ?, category_id = ?, type = ?, account_id = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(3, transaction.getTime());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(6, transaction.getType());
            pstmt.setString(7, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setString(8, transaction.getId());
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktion erfolgreich aktualisiert: " + transaction.getId());
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Aktualisieren der Transaktion: " + transaction.getId(), e);
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
     * Löscht alle Transaktionen die mit einem bestimmten Account in Verbindung stehen
     *
     * @param accountId ID des Accounts dessen Transaktionen gelöscht werden
     * @throws SQLException Error Exception
     */
    public void deleteTransactionsByAccount(String accountId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE account_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.executeUpdate();
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Deleted all transactions for account: " + accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Error deleting transactions for account: " + accountId, e);
            throw e;
        }
    }


    /**
     * Ruft alle Transaktionen für einen bestimmten Benutzer ab.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste der Transaktionen für den Benutzer.
     */
    public List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id IN (SELECT id FROM accounts WHERE user_id = ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktionen erfolgreich abgerufen für Benutzer-ID: " + userId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen der Transaktionen für Benutzer-ID: " + userId, e);
        }
        return transactions;
    }


    /**
     * Ruft die Transaktionen für ein bestimmtes Konto ab.
     *
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
                    Transaction transaction = mapResultSetToTransaction(rs);
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
     * Ruft alle Transaktionen ab, die einer bestimmten Kategorie zugeordnet sind.
     *
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
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktionen erfolgreich abgerufen für Kategorie-ID: " + categoryId);
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen der Transaktionen für Kategorie-ID: " + categoryId, e);
            throw new RuntimeException("Fehler beim Speichern der Transaktion: " + e.getMessage(), e);
        }

        return transactions;
    }


    // Hilfsmethode zum Mapping eines ResultSet auf ein Transaction-Objekt, mit Berücksichtigung von wiederkehrenden Transaktionen.
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Category category = categoryRepository.findCategoryById(rs.getString("category_id"));
        Account account = accountRepository.findAccountById(rs.getString("account_id"));
        Date date = rs.getDate("date");  // Korrigiert, um immer "date" zu verwenden
        Time time = rs.getTime("time");

        Transaction transaction = new Transaction(rs.getString("description"), rs.getDouble("amount"), rs.getString("type"), null, account, category, date, time);
        transaction.setId(rs.getString("id"));

        return transaction;
    }

    public List<Transaction> getTransactionsByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id IN (SELECT id FROM accounts WHERE user_id = ?) " +
                "AND date >= ? AND date <= ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transactions.add(transaction);
                }
            }
            LoggerUtils.logInfo(TransactionRepository.class.getName(), "Transaktionen erfolgreich für Zeitraum abgerufen.");
        } catch (SQLException e) {
            LoggerUtils.logError(TransactionRepository.class.getName(), "Fehler beim Abrufen der Transaktionen für Zeitraum", e);
        }
        return transactions;
    }


}
