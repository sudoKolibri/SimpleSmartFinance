package myProject.repository;

import myProject.model.Account;
import myProject.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    // Add a new account to the database
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

    // Get all accounts for a specific user
    public List<Account> getAllAccountsForUser(String userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");

                    accounts.add(new Account(id, userId, name, balance));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }
}
