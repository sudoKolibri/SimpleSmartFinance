package myProject.view.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import myProject.util.LoggerUtils;

import java.util.Objects;

/**
 * Die Klasse ViewUtils stellt verschiedene Hilfsfunktionen für die Benutzeroberfläche zur Verfügung,
 * wie z.B. das Festlegen von ProgressBar-Farben, die Überprüfung von Eingaben und das Anzeigen von Alerts.
 */
public class ViewUtils {

    /**
     * Bestimmt die Farbe einer ProgressBar basierend auf dem Verhältnis von Ausgaben zum Budget.
     *
     * @param spent  Der ausgegebene Betrag.
     * @param budget Das festgelegte Budget.
     * @return Die Farbe als String, abhängig vom Verhältnis der Ausgaben zum Budget.
     */
    public static String getProgressBarColor(double spent, double budget) {
        if (budget <= 0 || Double.isNaN(spent) || Double.isNaN(budget) || Double.isInfinite(spent) || Double.isInfinite(budget)) {
            LoggerUtils.logInfo(ViewUtils.class.getName(), "Ungültiges Budget oder Ausgaben für ProgressBarColor. Verwende Standardfarbe.");
            return "#50fa7b";  // Standardfarbe für ungültige Fälle
        }

        double ratio = spent / budget;
        ratio = Math.max(0, Math.min(1, ratio));  // Sicherstellen, dass das Verhältnis zwischen 0 und 1 liegt

        if (ratio < 0.5) {
            return "#50fa7b";
        } else if (ratio < 0.75) {
            return "#f1fa8c";
        } else if (ratio < 0.9) {
            return "#ffb86c";
        } else {
            return "#ff5555";
        }
    }

    /**
     * Hilfsmethode, um das optionale Budget einer Kategorie aus einem Textfeld zu ermitteln.
     *
     * @param budgetField Das Textfeld, in dem das Budget eingegeben wurde.
     * @return Das Budget als Double-Wert oder null, wenn die Eingabe ungültig ist.
     */
    public static Double getCategoryBudget(TextField budgetField) {
        Double categoryBudget = null;
        if (!budgetField.getText().isEmpty()) {
            try {
                categoryBudget = Double.parseDouble(budgetField.getText());
                LoggerUtils.logInfo(ViewUtils.class.getName(), "Kategorie-Budget erfolgreich geparst: " + categoryBudget);
            } catch (NumberFormatException ex) {
                LoggerUtils.logError(ViewUtils.class.getName(), "Ungültiges Budgetformat eingegeben.", ex);
                categoryBudget = null;
            }
        }
        return categoryBudget;
    }

    /**
     * Formatiert einen Double-Wert als Währungsstring.
     *
     * @param amount Der zu formatierende Betrag.
     * @return Der formatierte String im Währungsformat.
     */
    public static String formatCurrency(double amount) {
        LoggerUtils.logInfo(ViewUtils.class.getName(), "Formatiere Betrag als Währung: " + amount);
        return String.format("$%.2f", amount);
    }

    /**
     * Zeigt einen benutzerdefinierten Alert an.
     *
     * @param alertType Der Typ des Alerts (z.B. ERROR, INFORMATION).
     * @param message   Die anzuzeigende Nachricht.
     */
    public static void showAlert(Alert.AlertType alertType, String message) {
        LoggerUtils.logInfo(ViewUtils.class.getName(), "Zeige Alert: " + alertType + " - " + message);
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.UTILITY);


        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(ViewUtils.class.getResource("/styles.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");


        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getRoot().setStyle("-fx-background-color: #282a36;");
        stage.showAndWait();
    }
}
