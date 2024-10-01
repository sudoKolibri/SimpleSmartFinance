package myProject.service;

import myProject.model.User;
import myProject.repository.UserRepository;
import myProject.util.LoggerUtils;

/**
 * Der UserService verwaltet die Geschäftslogik im Zusammenhang mit Benutzern.
 * Er bietet Funktionen zur Authentifizierung, Registrierung und zum Abrufen des aktuell angemeldeten Benutzers.
 */
public class UserService {

    private final UserRepository userRepository;
    private User loggedInUser;  // Speichert den angemeldeten Benutzer nach der Authentifizierung

    // Konstruktor, um das UserRepository zu initialisieren
    public UserService() {
        this.userRepository = new UserRepository();
    }

    /**
     * Authentifiziert einen Benutzer anhand seines Benutzernamens und Passworts.
     * @param username Der Benutzername des Benutzers.
     * @param password Das Passwort des Benutzers.
     * @return true, wenn die Authentifizierung erfolgreich war, false bei einem Fehler.
     */
    public boolean authenticateUser(String username, String password) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getPassword().equals(password)) {
                this.loggedInUser = user;  // Speichert den angemeldeten Benutzer
                LoggerUtils.logInfo(UserService.class.getName(), "Benutzer erfolgreich authentifiziert: " + username);
                return true;
            }
        } catch (Exception e) {
            LoggerUtils.logError(UserService.class.getName(), "Fehler bei der Authentifizierung des Benutzers: " + username, e);
        }
        return false;
    }

    /**
     * Gibt den aktuell angemeldeten Benutzer zurück.
     * @return Der aktuell angemeldete Benutzer oder null, falls kein Benutzer angemeldet ist.
     */
    public User getLoggedInUser() {
        if (loggedInUser != null) {
            LoggerUtils.logInfo(UserService.class.getName(), "Angemeldeter Benutzer abgerufen: " + loggedInUser.getUsername());
        } else {
            LoggerUtils.logError(UserService.class.getName(), "Kein Benutzer ist aktuell angemeldet.", null);
        }
        return loggedInUser;
    }

    /**
     * Registriert einen neuen Benutzer.
     * @param username Der gewünschte Benutzername des neuen Benutzers.
     * @param password Das Passwort des neuen Benutzers.
     * @return true, wenn die Registrierung erfolgreich war, false, wenn der Benutzername bereits vergeben ist.
     */
    public boolean registerUser(String username, String password) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                LoggerUtils.logError(UserService.class.getName(), "Registrierung fehlgeschlagen: Benutzername bereits vergeben - " + username, null);
                return false;  // Benutzername ist bereits vergeben
            }

            User newUser = new User(username, password);
            boolean success = userRepository.addUser(newUser);
            if (success) {
                LoggerUtils.logInfo(UserService.class.getName(), "Benutzer erfolgreich registriert: " + username);
            } else {
                LoggerUtils.logError(UserService.class.getName(), "Fehler bei der Registrierung des Benutzers: " + username, null);
            }
            return success;

        } catch (Exception e) {
            LoggerUtils.logError(UserService.class.getName(), "Fehler bei der Registrierung des Benutzers: " + username, e);
            return false;
        }
    }
}
