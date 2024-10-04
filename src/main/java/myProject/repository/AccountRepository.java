package myProject.repository;

import myProject.model.Account;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Das AccountRepository ist für die Datenbankzugriffe im Zusammenhang mit Konten verantwortlich.
 * Hier werden Konten in die Datenbank eingefügt, abgerufen, aktualisiert und gelöscht.
 */
public class AccountRepository {

    /**
     * Methode zum Hinzufügen eines neuen Kontos in die Datenbank.
     *
     * @param account Das hinzuzufügende Konto.
     * @return true, wenn das Konto erfolgreich hinzugefügt wurde, false bei einem Fehler.
     */
    public boolean addAccount(Account account) {
        String sql = "INSERT INTO accounts (id, user_id, name, balance) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, account.getId());
            pstmt.setString(2, account.getUserId());
            pstmt.setString(3, account.getName());
            pstmt.setDouble(4, account.getBalance());

            pstmt.executeUpdate();

            LoggerUtils.logInfo(AccountRepository.class.getName(), "Account erfolgreich hinzugefügt: " + account.getUserId());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Fehler beim hinzufügen des Accounts:: " + account.getUserId(), e);
            return false;
        }
    }

    /**
     * Methode zum Aktualisieren eines bestehenden Kontos in der Datenbank.
     *
     * @param account Das zu aktualisierende Konto.
     */
    public void updateAccount(Account account) {
        String sql = "UPDATE accounts SET name = ?, balance = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, account.getName());
            pstmt.setDouble(2, account.getBalance());
            pstmt.setString(3, account.getId());

            pstmt.executeUpdate();

            LoggerUtils.logInfo(AccountRepository.class.getName(), "Account erfolgreich aktualisiert: " + account.getName());

        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Fehler beim aktualisieren des Accounts: " + account.getName(), e);
        }
    }

    /**
     * Löscht einen Account mit dieser spezifischen ID
     *
     * @param accountId ID des zu löschenden Accounts
     * @throws SQLException Error Exception
     */
    public void deleteAccount(String accountId) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.executeUpdate();
            LoggerUtils.logInfo(AccountRepository.class.getName(), "Deleted account: " + accountId);
        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Error deleting account: " + accountId, e);
            throw e;
        }
    }

    /**
     * Methode zum Abrufen aller Kontonamen aus der Datenbank.
     *
     * @return Eine Liste aller Kontonamen.
     * @throws SQLException bei einem Fehler im Datenbankzugriff.
     */
    public List<String> getAllAccountNames() throws SQLException {
        List<String> accountNames = new ArrayList<>();
        String sql = "SELECT name FROM accounts";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                accountNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Fehler beim abrufen der Accountnamen", e);
            throw e;
        }
        return accountNames;
    }

    /**
     * Methode zum Abrufen eines Kontos anhand seines Namens für einen bestimmten Benutzer.
     *
     * @param userId      Die ID des Benutzers.
     * @param accountName Der Name des Kontos.
     * @return Das gefundene Konto oder null, wenn kein Konto gefunden wurde.
     * @throws SQLException bei einem Fehler im Datenbankzugriff.
     */
    public Account findAccountByName(String userId, String accountName) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ? AND name = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, accountName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LoggerUtils.logInfo(AccountRepository.class.getName(), "Account gefunden von Benutzer: " + userId + " und Accountname: " + accountName);
                    return mapResultSetToAccount(rs);
                }
            }

        }

        return null;
    }

    /**
     * Methode zum Abrufen aller Konten eines bestimmten Benutzers.
     *
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Konten des Benutzers.
     * @throws SQLException bei einem Fehler im Datenbankzugriff.
     */
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

        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Fehler beim Abrufen der Accounts von Benutzer: " + userId, e);
            throw e;
        }
        return accounts;
    }

    /**
     * Methode zum Abrufen eines Kontos anhand seiner ID.
     *
     * @param accountId Die ID des Kontos.
     * @return Das gefundene Konto oder null, wenn kein Konto gefunden wurde.
     * @throws SQLException bei einem Fehler im Datenbankzugriff.
     */
    public Account findAccountById(String accountId) throws SQLException {
        if (accountId == null) return null;
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LoggerUtils.logInfo(AccountRepository.class.getName(), "Account gefunden mit ID: " + accountId);
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtils.logError(AccountRepository.class.getName(), "Fehler beim Account abrufen mit Account ID: " + accountId, e);
            throw e;
        }
        LoggerUtils.logInfo(AccountRepository.class.getName(), "Kein Account gefunden mit der ID: " + accountId);
        return null;
    }

    /**
     * Hilfsmethode zum Mapping eines ResultSet auf ein Account-Objekt.
     *
     * @param rs Das ResultSet, das die Account-Daten enthält.
     * @return Ein Account-Objekt.
     * @throws SQLException bei einem Fehler beim Abrufen der Daten.
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(rs.getString("id"), rs.getString("user_id"), rs.getString("name"), rs.getDouble("balance"));
    }

}
