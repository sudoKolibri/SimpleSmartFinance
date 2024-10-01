package myProject.model;

import java.util.UUID;

/**
 * Die User-Klasse repräsentiert einen Benutzer im System.
 * Jeder Benutzer hat eine eindeutige ID, einen Benutzernamen und ein Passwort.
 */
public class User {
    private String id;  // Eindeutige ID des Benutzers
    private String username;  // Benutzername des Benutzers
    private String password;  // Passwort des Benutzers

    /**
     * Konstruktor für die Erstellung eines neuen Benutzers mit einer automatisch generierten ID.
     *
     * @param username Der Benutzername des neuen Benutzers.
     * @param password Das Passwort des neuen Benutzers.
     */
    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();  // Automatisch generierte eindeutige ID
        this.username = username;
        this.password = password;
    }

    /**
     * Konstruktor für das Laden eines bestehenden Benutzers mit einer vorgegebenen ID.
     *
     * @param id Die eindeutige ID des Benutzers.
     * @param username Der Benutzername des Benutzers.
     * @param password Das Passwort des Benutzers.
     */
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Getter- und Setter-Methoden für die Eigenschaften

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
