package myProject.controller;

import myProject.model.User;
import myProject.service.UserService;
import myProject.util.LoggerUtils;

/**
 * Der UserController ist für die Verwaltung der Benutzerinteraktionen verantwortlich.
 * Er bietet Funktionen für die Benutzeranmeldung, -registrierung und den Abruf des aktuell angemeldeten Benutzers.
 */
public class UserController {

    private final UserService userService;

    // Konstruktor, der den UserService initialisiert
    public UserController() {
        this.userService = new UserService();
    }

    /**
     * Methode zur Handhabung des Login-Vorgangs.
     *
     * @param username Der Benutzername des Benutzers.
     * @param password Das Passwort des Benutzers.
     * @return true, wenn die Authentifizierung erfolgreich war, false andernfalls.
     */
    public boolean login(String username, String password) {
        boolean isAuthenticated = userService.authenticateUser(username, password);
        if (isAuthenticated) {
            LoggerUtils.logInfo(UserController.class.getName(), "Benutzer erfolgreich angemeldet: " + username);
        } else {
            LoggerUtils.logError(UserController.class.getName(), "Fehlerhafte Anmeldung für Benutzer: " + username, null);
        }
        return isAuthenticated;
    }

    /**
     * Methode zum Abrufen des aktuell angemeldeten Benutzers.
     *
     * @return Das User-Objekt des angemeldeten Benutzers.
     */
    public User getLoggedInUser() {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            LoggerUtils.logInfo(UserController.class.getName(), "Aktuell angemeldeter Benutzer abgerufen: " + loggedInUser.getUsername());
        } else {
            LoggerUtils.logError(UserController.class.getName(), "Kein Benutzer ist aktuell angemeldet.", null);
        }
        return loggedInUser;
    }

    /**
     * Methode zur Handhabung der Benutzerregistrierung.
     *
     * @param username Der gewünschte Benutzername des neuen Benutzers.
     * @param password Das Passwort des neuen Benutzers.
     * @return true, wenn die Registrierung erfolgreich war, false bei einem Fehler (z. B. wenn der Benutzername bereits vergeben ist).
     */
    public boolean register(String username, String password) {
        boolean isRegistered = userService.registerUser(username, password);
        if (isRegistered) {
            LoggerUtils.logInfo(UserController.class.getName(), "Benutzer erfolgreich registriert: " + username);
        } else {
            LoggerUtils.logError(UserController.class.getName(), "Registrierung fehlgeschlagen für Benutzer: " + username, null);
        }
        return isRegistered;
    }
}
