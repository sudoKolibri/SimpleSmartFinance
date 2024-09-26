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
        System.out.println("TransactionRepository.saveTransaction: Saving transaction - " + transaction);
        String sql = "INSERT INTO transactions (id, amount, date, time, description, category_id, type, account_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    // Save a new recurring transaction
    public void saveRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.saveRecurringTransaction: Saving recurring transaction - " + transaction);
        String sql = "INSERT INTO recurring_transactions (id, amount, description, category_id, type, account_id, start_date, time, recurrence_interval, end_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    // Update an existing transaction
    public void updateTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.updateTransaction: Updating transaction - " + transaction);
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
            System.out.println("TransactionRepository.updateTransaction: Transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.updateTransaction: Error updating transaction - " + e.getMessage());
            throw e;
        }
    }

    // Update an existing recurring transaction
    public void updateRecurringTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.updateRecurringTransaction: Updating recurring transaction - " + transaction);
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
            System.out.println("TransactionRepository.updateRecurringTransaction: Recurring transaction updated successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.updateRecurringTransaction: Error updating recurring transaction - " + e.getMessage());
            throw e;
        }
    }

    // Delete a transaction
    public void deleteTransaction(Transaction transaction) throws SQLException {
        System.out.println("TransactionRepository.deleteTransaction: Deleting transaction - " + transaction);
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
            System.out.println("TransactionRepository.deleteTransaction: Transaction deleted successfully.");
        } catch (SQLException e) {
            System.err.println("TransactionRepository.deleteTransaction: Error deleting transaction - " + e.getMessage());
            throw e;
        }
    }

    // Fetch all transactions
    public List<Transaction> getAllTransactions() throws SQLException {
        System.out.println("TransactionRepository.getAllTransactions: Fetching all transactions.");
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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

    // Fetch transactions filtered by account
    public List<Transaction> getTransactionsByAccount(String accountName) throws SQLException {
        System.out.println("TransactionRepository.getTransactionsByAccount: Fetching transactions for account - " + accountName);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = (SELECT id FROM accounts WHERE name = ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    // Fetch all recurring transactions for a specific account
    public List<Transaction> getRecurringTransactionsByAccount(String accountId) throws SQLException {
        System.out.println("TransactionRepository.getRecurringTransactionsByAccount: Fetching recurring transactions for account ID - " + accountId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE account_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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



    // Fetch transactions by category
    public List<Transaction> getTransactionsByCategory(String categoryId) throws SQLException {
        System.out.println("TransactionRepository.getTransactionsByCategory: Fetching transactions for category ID - " + categoryId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE category_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

        // Fetch recurring transactions as well
        transactions.addAll(getRecurringTransactionsByCategory(categoryId));
        System.out.println("TransactionRepository.getTransactionsByCategory: Fetched transactions - " + transactions);
        return transactions;
    }


    // Fetch recurring transactions by category
    public List<Transaction> getRecurringTransactionsByCategory(String categoryId) throws SQLException {
        System.out.println("TransactionRepository.getRecurringTransactionsByCategory: Fetching recurring transactions for category ID - " + categoryId);
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE category_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
    private Transaction mapResultSetToTransaction(ResultSet rs, boolean isRecurring) throws SQLException {
        Category category = findCategoryById(rs.getString("category_id"));
        Account account = findAccountById(rs.getString("account_id"));
        Date date = rs.getDate(columnExists(rs, "start_date") ? "start_date" : "date");
        Time time = rs.getTime("time");

        // Initialize the Transaction object
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
        transaction.setRecurring(isRecurring);

        // Only set the recurrence interval and end date if the transaction is recurring
        if (isRecurring && columnExists(rs, "recurrence_interval")) {
            transaction.setRecurrenceInterval(rs.getString("recurrence_interval"));
            transaction.setEndDate(rs.getDate("end_date"));
        }

        return transaction;
    }




    // Helper method to check if a column exists in the ResultSet
    private boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
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
