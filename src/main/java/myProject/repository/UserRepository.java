package myProject.repository;

import myProject.model.User;
import myProject.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    // Methode zum Hinzufügen eines neuen Benutzers in die Datenbank.
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());  // Unique user ID
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());  // You may want to hash the password here

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode zum Suchen eines Benutzers in der Datenbank anhand des Benutzernamens. Gibt optional den Benutzer zurück, wenn gefunden.
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("id");
                String password = rs.getString("password");
                return Optional.of(new User(id, username, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
