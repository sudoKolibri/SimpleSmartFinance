package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    // Save a new transaction
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, amount, date, time, description, category_id, type, account_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setDate(3, new java.sql.Date(transaction.getDate().getTime())); // Use java.sql.Date directly
            pstmt.setTime(4, transaction.getTime()); // Use java.sql.Time directly
            pstmt.setString(5, transaction.getDescription());
            pstmt.setString(6, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(7, transaction.getType());
            pstmt.setString(8, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setString(9, transaction.getStatus());
            pstmt.executeUpdate();
        }
    }

    // Save a new recurring transaction
    public void saveRecurringTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO recurring_transactions (id, amount, description, category_id, type, account_id, start_date, recurrence_interval, end_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getCategory() != null ? transaction.getCategory().getId() : null);
            pstmt.setString(5, transaction.getType());
            pstmt.setString(6, transaction.getAccount() != null ? transaction.getAccount().getId() : null);
            pstmt.setDate(7, new java.sql.Date(transaction.getDate().getTime()));
            pstmt.setString(8, transaction.getRecurrenceInterval());
            pstmt.setDate(9, transaction.getEndDate() != null ? new java.sql.Date(transaction.getEndDate().getTime()) : null);
            pstmt.setString(10, transaction.getStatus());
            pstmt.executeUpdate();
        }
    }

    // Update an existing transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount = ?, date = ?, time = ?, description = ?, category_id = ?, type = ?, account_id = ?, status = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        }
    }

    // Update an existing recurring transaction
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE recurring_transactions SET amount = ?, description = ?, category_id = ?, type = ?, account_id = ?, start_date = ?, recurrence_interval = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    // Fetch transactions by category
    public List<Transaction> getTransactionsByCategory(String categoryId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE category_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transactions.add(transaction);
                    // Log each transaction fetched
                    System.out.println("Fetched Transaction: " + transaction.getDescription() +
                            ", Amount: " + transaction.getAmount() +
                            ", Type: " + transaction.getType() +
                            ", Account ID: " + transaction.getAccount().getId() +
                            ", Category ID: " + transaction.getCategory().getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for category ID " + categoryId + ": " + e.getMessage());
            throw e;
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

    // Fetch all custom and standard categories for a specific user
    public List<Category> getAllCustomAndStandardCategories(String userId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        categories.addAll(getCustomCategoriesForUser(userId));
        categories.addAll(getStandardCategories());
        return categories;
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
                    categories.add(mapResultSetToCategory(rs));
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
                categories.add(mapResultSetToCategory(rs));
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
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        }
        return accounts;
    }

    // Map ResultSet to Transaction object
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Category category = findCategoryById(rs.getString("category_id"));
        Account account = findAccountById(rs.getString("account_id"));
        Date date = new java.sql.Date(rs.getDate("date").getTime()); // Use java.sql.Date directly
        Time time = rs.getTime("time");

        Transaction transaction = new Transaction(
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("type"),
                null,
                account,
                category,
                date,
                time,
                rs.getString("status")
        );
        transaction.setId(rs.getString("id"));
        return transaction;
    }

    // Map ResultSet to Category object
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getString("id"),
                rs.getString("name"),
                rs.getBoolean("is_standard"),
                rs.getBoolean("is_custom"),
                rs.getDouble("budget")
        );
    }

    // Map ResultSet to Account object
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getDouble("balance")
        );
    }

    // Helper method to fetch Category by ID
    private Category findCategoryById(String categoryId) throws SQLException {
        if (categoryId == null) return null;
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        return null;
    }

    // Helper method to fetch Account by ID
    private Account findAccountById(String accountId) throws SQLException {
        if (accountId == null) return null;
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }
}
