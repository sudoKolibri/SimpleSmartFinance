package myProject.model;

import java.util.Date;
import java.util.List;

/**
 * Die Budget-Klasse stellt ein Budget dar, das einem Benutzer zugeordnet ist.
 * Ein Budget definiert einen bestimmten Geldbetrag, der für einen festgelegten Zeitraum
 * und für eine oder mehrere Kategorien zugewiesen ist.
 */
public class Budget {
    private String id;  // Eindeutige ID des Budgets
    private String userId;  // ID des Benutzers, dem das Budget gehört
    private double amount;  // Betrag des Budgets
    private Date startDate;  // Startdatum des Budgets
    private Date endDate;  // Enddatum des Budgets
    private List<String> categoryIds;  // Liste der Kategorie-IDs, die mit diesem Budget verknüpft sind

    /**
     * Konstruktor für die Erstellung eines Budgets.
     *
     * @param id Die eindeutige ID des Budgets.
     * @param userId Die ID des Benutzers, dem das Budget gehört.
     * @param amount Der Betrag des Budgets.
     * @param startDate Das Startdatum des Budgets.
     * @param endDate Das Enddatum des Budgets.
     * @param categoryIds Die Liste der Kategorie-IDs, die mit diesem Budget verknüpft sind.
     */
    public Budget(String id, String userId, double amount, Date startDate, Date endDate, List<String> categoryIds) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryIds = categoryIds;
    }

    // Getter und Setter für die Eigenschaften
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
