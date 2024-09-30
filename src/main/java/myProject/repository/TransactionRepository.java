package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;

import myProject.repository.AccountRepository;
import myProject.repository.CategoryRepository;

import myProject.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionRepository(AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    // Methode zum Speichern einer neuen Transaktion in der Datenbank.
    public void saveTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.saveTransaction: Saving transaction - " + transaction);
        String sql = "INSERT INTO transactions (id, amount, date, time, description, category_id, type, account_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.saveTransaction: Transaction saved successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.saveTransaction: Error saving transaction - " + e.getMessage());
            throw e;
        }
    }

    // Methode zum Speichern einer neuen wiederkehrenden Transaktion in der Datenbank.
    public void saveRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.saveRecurringTransaction: Saving recurring transaction - " + transaction);
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
            System.out.println("TransactionRepository.saveRecurringTransaction: Recurring transaction saved successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.saveRecurringTransaction: Error saving recurring transaction - " + e.getMessage());
            throw e;
        }
    }

    // Methode zum Aktualisieren einer bestehenden Transaktion in der Datenbank.
    public void updateTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.updateTransaction: Updating transaction - " + transaction);
        String sql = "UPDATE transactions SET amount = ?, date = ?, time = ?, description = ?, category_id = ?, type = ?, account_id = ?, status = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setTime(3, transaction.getTime());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(6, transaction.getType());
            pstmt.setString(7, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setString(8, transaction.getStatus());
            pstmt.setString(9, transaction.getId());
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.updateTransaction: Transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.updateTransaction: Error updating transaction - " + e.getMessage());
            throw e;
        }
    }

    // Methode zum Aktualisieren einer bestehenden wiederkehrenden Transaktion in der Datenbank.
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.updateRecurringTransaction: Updating recurring transaction - " + transaction);
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
            System.out.println("TransactionRepository.updateRecurringTransaction: Recurring transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.updateRecurringTransaction: Error updating recurring transaction - " + e.getMessage());
            throw e;
        }
    }

    // Methode zum Löschen einer Transaktion aus der Datenbank.
    public void deleteTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.deleteTransaction: Deleting transaction - " + transaction);
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.deleteTransaction: Transaction deleted successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.deleteTransaction: Error deleting transaction - " + e.getMessage());
            throw e;
        }
    }


    // Methode zum Löschen einer wiederkehrenden Transaktion aus der Datenbank.
    public void deleteRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.deleteRecurringTransaction: Deleting recurring transaction - " + transaction);
        String sql = "DELETE FROM recurring_transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.deleteRecurringTransaction: Recurring transaction deleted successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.deleteRecurringTransaction: Error deleting recurring transaction - " + e.getMessage());
            throw e;
        }
    }

    // Methode zum Löschen aller anstehenden Transaktionen, die mit einer bestimmten wiederkehrenden Transaktion verknüpft sind.
    public void deletePendingTransactionsByRecurringId(String recurringTransactionId) throws SQLException {
        System.out.println("TransactionRepository.deletePendingTransactionsByRecurringId: Deleting pending transactions for recurring ID - " + recurringTransactionId);
        String sql = "DELETE FROM transactions WHERE id = ? AND status = 'pending'";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recurringTransactionId);
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.deletePendingTransactionsByRecurringId: Pending transactions deleted successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.deletePendingTransactionsByRecurringId: Error deleting pending transactions - " + e.getMessage());
            throw e;
        }
    }


    // Methode zum Abrufen aller Transaktionen aus der Datenbank.
    public List<Transaction> getAllTransactions() throws SQLException {
        System.out.println("TransactionRepository.getAllTransactions: Fetching all transactions.");
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection connection = DatabaseManager.getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs, false);
                transactions.add(transaction);
            }
            System.out.println("TransactionRepository.getAllTransactions: Fetched transactions - " + transactions);
        } catch (SQLException e) {
            System.err.println("TransactionRepository.getAllTransactions: Error fetching transactions - " + e.getMessage());
            throw e;
        }
        return transactions;
    }

    // Methode zum Abrufen aller Transaktionen für ein bestimmtes Konto.
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        System.out.println("TransactionRepository.getTransactionsByAccount: Fetching transactions for account - " + accountName);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = (SELECT id FROM accounts WHERE name = ?)";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Set isRecurring to false when mapping regular transactions
                    Transaction transaction = mapResultSetToTransaction(rs, false);  // Updated line 133
                    transactions.add(transaction);
                }
            }
            System.out.println("TransactionRepository.getTransactionsByAccount: Fetched transactions - " + transactions);
        } catch (SQLException e) {
            System.err.println("TransactionRepository.getTransactionsByAccount: Error fetching transactions - " + e.getMessage());
            throw e;
        }
        return transactions;
    }

    // Methode zum Abrufen aller wiederkehrenden Transaktionen für ein bestimmtes Konto.
    public List<Transaction> getRecurringTransactionsByAccount(String accountId) throws SQLException {
        System.out.println("TransactionRepository.getRecurringTransactionsByAccount: Fetching recurring transactions for account ID - " + accountId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE account_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Set isRecurring to true when mapping recurring transactions
                    Transaction transaction = mapResultSetToTransaction(rs, true);  // Updated line 202
                    transactions.add(transaction);
                }
            }
            System.out.println("TransactionRepository.getRecurringTransactionsByAccount: Fetched recurring transactions - " + transactions);
        } catch (SQLException e) {
            System.err.println("TransactionRepository.getRecurringTransactionsByAccount: Error fetching recurring transactions - " + e.getMessage());
            throw e;
        }
        return transactions;
    }


    // Methode zum Abrufen aller Transaktionen, die einer bestimmten Kategorie zugeordnet sind.
    public List<Transaction> getTransactionsByCategory(String categoryId) throws SQLException {
        System.out.println("TransactionRepository.getTransactionsByCategory: Fetching transactions for category ID - " + categoryId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE category_id = ?";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Set isRecurring to false when mapping regular transactions
                    Transaction transaction = mapResultSetToTransaction(rs, false);
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("TransactionRepository.getTransactionsByCategory: Error fetching transactions - " + e.getMessage());
            throw e;
        }


        transactions.addAll(getRecurringTransactionsByCategory(categoryId));
        System.out.println("TransactionRepository.getTransactionsByCategory: Fetched transactions - " + transactions);
        return transactions;
    }


    // Methode zum Abrufen aller wiederkehrenden Transaktionen, die einer bestimmten Kategorie zugeordnet sind.
    public List<Transaction> getRecurringTransactionsByCategory(String categoryId) throws SQLException {
        System.out.println("TransactionRepository.getRecurringTransactionsByCategory: Fetching recurring transactions for category ID - " + categoryId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE category_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map the ResultSet to a Transaction object with isRecurring set to true
                    Transaction transaction = mapResultSetToTransaction(rs, true);
                    transactions.add(transaction);
                }
            }
            System.out.println("TransactionRepository.getRecurringTransactionsByCategory: Fetched recurring transactions - " + transactions);
        } catch (SQLException e) {
            System.err.println("TransactionRepository.getRecurringTransactionsByCategory: Error fetching recurring transactions - " + e.getMessage());
            throw e;
        }
        return transactions;
    }










    // Hilfsmethode zum Mapping eines ResultSet auf ein Transaction-Objekt, mit Berücksichtigung von wiederkehrenden Transaktionen.
    private Transaction mapResultSetToTransaction(ResultSet rs, boolean isRecurring) throws SQLException {
        Category category = categoryRepository.findCategoryById(rs.getString("category_id"));
        Account account = accountRepository.findAccountById(rs.getString("account_id"));
        Date date = rs.getDate(columnExists(rs, "start_date") ? "start_date" : "date");
        Time time = rs.getTime("time");

        // Initialize the Transaction object
        Transaction transaction = new Transaction(rs.getString("description"), rs.getDouble("amount"), rs.getString("type"), null, account, category, date, time, rs.getString("status"));
        transaction.setId(rs.getString("id"));
        transaction.setRecurring(isRecurring);

        // Only set the recurrence interval and end date if the transaction is recurring
        if (isRecurring && columnExists(rs, "recurrence_interval")) {
            transaction.setRecurrenceInterval(rs.getString("recurrence_interval"));
            transaction.setEndDate(rs.getDate("end_date"));
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
