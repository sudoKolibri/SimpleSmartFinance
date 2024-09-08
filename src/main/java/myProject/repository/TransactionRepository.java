package myProject.repository;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.model.User;
import myProject.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    // Save a regular transaction
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, amount, description, category_id, type, account_id, date, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setString(4, transaction.getCategory().getId());
            stmt.setString(5, transaction.getType());  // Income or Expense
            stmt.setString(6, transaction.getAccount().getId());
            stmt.setDate(7, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(8, transaction.getUser().getId());
            stmt.executeUpdate();
        }
    }


    // Save a recurring transaction
    public void saveRecurringTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO recurring_transactions (id, amount, description, category_id, account_id, start_date, recurrence_interval, type, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setString(4, transaction.getCategory().getId());
            stmt.setString(5, transaction.getAccount().getId());
            stmt.setDate(6, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(7, transaction.getRecurrenceInterval());
            stmt.setString(8, transaction.getType());
            stmt.setString(9, transaction.getUser().getId());
            stmt.executeUpdate();
        }
    }

    // Update a regular transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount = ?, description = ?, category_id = ?, account_id = ?, date = ?, type = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getDescription());
            stmt.setString(3, transaction.getCategory().getId());
            stmt.setString(4, transaction.getAccount().getId());
            stmt.setDate(5, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(6, transaction.getType());
            stmt.setString(7, transaction.getId());
            stmt.executeUpdate();
        }
    }

    // Update a recurring transaction
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE recurring_transactions SET amount = ?, description = ?, category_id = ?, account_id = ?, start_date = ?, recurrence_interval = ?, type = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getDescription());
            stmt.setString(3, transaction.getCategory().getId());
            stmt.setString(4, transaction.getAccount().getId());
            stmt.setDate(5, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(6, transaction.getRecurrenceInterval());
            stmt.setString(7, transaction.getType());
            stmt.setString(8, transaction.getId());
            stmt.executeUpdate();
        }
    }

    // Delete a regular transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.executeUpdate();
        }
    }

    // Delete a recurring transaction
    public void deleteRecurringTransaction(Transaction transaction) throws SQLException {
        String sql = "DELETE FROM recurring_transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.executeUpdate();
        }
    }

    // Fetch all regular transactions for a specific user
    public List<Transaction> getAllTransactionsForUser(String userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ?";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    // Fetch all recurring transactions for a specific user
    public List<Transaction> getRecurringTransactionsForUser(String userId) throws SQLException {
        String sql = "SELECT * FROM recurring_transactions WHERE user_id = ?";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    // Helper method to map a ResultSet to a Transaction object (regular and recurring)
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        // Assuming the rest of the mapping is correct

        // Map the category using just the category ID (other fields can be fetched or set later)
        Category category = new Category(rs.getString("category_id"));

        // Similarly, map the account and user (if necessary)
        Account account = new Account(rs.getString("account_id"));
        User user = new User(rs.getString("user_id"), null, null);  // Assuming you'll load the full user details later

        return new Transaction(
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("type"),   // "income" or "expense"
                user,                   // Pass the user object
                account,                // Pass the account object
                category,               // Pass the category object
                rs.getDate("date"),     // Transaction date
                rs.getString("recurrence_interval")  // For recurring transactions
        );
    }


}
