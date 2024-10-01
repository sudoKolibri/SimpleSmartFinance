package myProject.repository;

import myProject.model.User;
import myProject.db.DatabaseManager;
import myProject.util.LoggerUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Das UserRepository ist für die Datenbankzugriffe im Zusammenhang mit Benutzern verantwortlich.
 * Hier werden Benutzer in die Datenbank eingefügt und nach Benutzern anhand ihres Benutzernamens gesucht.
 */
public class UserRepository {

    /**
     * Methode zum Hinzufügen eines neuen Benutzers in die Datenbank.
     * @param user Der Benutzer, der hinzugefügt werden soll.
     * @return true, wenn das Hinzufügen erfolgreich war, false bei einem Fehler.
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());  // Eindeutige Benutzer-ID
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());  // Passwort sollte normalerweise gehasht werden

            pstmt.executeUpdate();
            LoggerUtils.logInfo(UserRepository.class.getName(), "Benutzer erfolgreich hinzugefügt: " + user.getUsername());
            return true;

        } catch (SQLException e) {
            LoggerUtils.logError(UserRepository.class.getName(), "Fehler beim Hinzufügen des Benutzers: " + user.getUsername(), e);
            return false;
        }
    }

    /**
     * Methode zum Suchen eines Benutzers anhand seines Benutzernamens in der Datenbank.
     * @param username Der Benutzername, nach dem gesucht werden soll.
     * @return Ein Optional-Objekt, das den Benutzer enthält, falls er gefunden wurde.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String password = rs.getString("password");
                    LoggerUtils.logInfo(UserRepository.class.getName(), "Benutzer erfolgreich gefunden: " + username);
                    return Optional.of(new User(id, username, password));
                }
            }

        } catch (SQLException e) {
            LoggerUtils.logError(UserRepository.class.getName(), "Fehler beim Suchen des Benutzers: " + username, e);
        }

        return Optional.empty();
    }
}
