package myProject.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility-Klasse für das Logging von Informationen und Fehlern.
 * Diese Klasse soll in verschiedenen Teilen des Projekts verwendet werden,
 * um konsistentes Logging zu gewährleisten.
 */
public class LoggerUtils {

    // Statischer Logger, der in der gesamten Anwendung verwendet wird
    private static final Logger LOGGER = Logger.getLogger(LoggerUtils.class.getName());

    // Privater Konstruktor, um die Instanziierung dieser Utility-Klasse zu verhindern
    private LoggerUtils() {
        // Privater Konstruktor zur Verhinderung der Instanziierung
    }

    /**
     * Methode zum Loggen von allgemeinen Informationen.
     * @param className Der Name der Klasse, aus der die Log-Nachricht kommt
     * @param message Die Log-Nachricht
     */
    public static void logInfo(String className, String message) {
        LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{className, message});
    }

    /**
     * Methode zum Loggen von Fehlern.
     * @param className Der Name der Klasse, aus der der Fehler kommt
     * @param message Die Fehlermeldung
     * @param e Die Ausnahme, die den Fehler verursacht hat
     */
    public static void logError(String className, String message, Exception e) {
        LOGGER.log(Level.SEVERE, className + ": " + message, e);
    }
}
