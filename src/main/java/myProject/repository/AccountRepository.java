package myProject.repository;

import myProject.model.Account;
import myProject.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    // Methode zum Hinzufügen eines neuen Kontos in die Datenbank.
    public boolean addAccount(Account account) {
        String sql = "INSERT INTO accounts (id, user_id, name, balance) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, account.getId());
            pstmt.setString(2, account.getUserId());
            pstmt.setString(3, account.getName());
            pstmt.setDouble(4, account.getBalance());

            pstmt.executeUpdate();

            System.out.println("Account added successfully for user: " + account.getUserId());

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode zum Aktualisieren eines bestehenden Kontos in der Datenbank.
    public boolean updateAccount(Account account) {
        String sql = "UPDATE accounts SET name = ?, balance = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, account.getName());
            pstmt.setDouble(2, account.getBalance());
            pstmt.setString(3, account.getId());

            pstmt.executeUpdate();

            System.out.println("Account updated successfully: " + account.getName());

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode zum Abrufen aller Kontonamen aus der Datenbank.
    public List<String> getAllAccountNames() throws SQLException {
        List<String> accountNames = new ArrayList<>();
        String sql = "SELECT name FROM accounts";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                accountNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching account names: " + e.getMessage());
            throw e;
        }
        return accountNames;
    }


    // Methode zum Abrufen aller Konten eines bestimmten Benutzers.
    public List<Account> getAllAccountsForUser(String userId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        }
        return accounts;
    }

    // Hilfsmethode zum Abrufen eines Kontos anhand seiner ID.
    public Account findAccountById(String accountId) throws SQLException {
        if (accountId == null) return null;
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }

    // Hilfsmethode zum Mapping eines ResultSet auf ein Account-Objekt.
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(rs.getString("id"), rs.getString("user_id"), rs.getString("name"), rs.getDouble("balance"));
    }

    
}
