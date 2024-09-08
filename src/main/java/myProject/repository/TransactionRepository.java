package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    // Save a new transaction
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, amount, date, description, category_id, type, account_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setDate(3, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getCategory().getId());
            pstmt.setString(6, transaction.getType());
            pstmt.setString(7, transaction.getAccount().getId());
            pstmt.executeUpdate();
        }
    }

    // Update an existing transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount = ?, date = ?, description = ?, category_id = ?, type = ?, account_id = ? " +
                "WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getCategory().getId());
            pstmt.setString(5, transaction.getType());
            pstmt.setString(6, transaction.getAccount().getId());
            pstmt.setString(7, transaction.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete a transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
        }
    }

    // Fetch all transactions
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";

        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    // Fetch transactions filtered by account
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = (SELECT id FROM accounts WHERE name = ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    // Fetch all account names
    public List<String> getAllAccountNames() throws SQLException {
        List<String> accountNames = new ArrayList<>();
        String sql = "SELECT name FROM accounts";

        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accountNames.add(rs.getString("name"));
            }
        }
        return accountNames;
    }

    // Fetch custom categories for a specific user
    public List<Category> getCustomCategoriesForUser(String userId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? AND is_custom = true";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getBoolean("is_standard"),
                            rs.getBoolean("is_custom"),
                            rs.getDouble("budget")
                    );
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    // Fetch all standard categories
    public List<Category> getStandardCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_standard = true";

        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category category = new Category(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("color"),
                        rs.getBoolean("is_standard"),
                        rs.getBoolean("is_custom"),
                        rs.getDouble("budget")
                );
                categories.add(category);
            }
        }
        return categories;
    }

    // Fetch all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getDouble("balance")
                    );
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    // Map ResultSet to regular Transaction object
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("type"),
                null,
                null,
                null,
                rs.getDate("date")
        );
    }
}
