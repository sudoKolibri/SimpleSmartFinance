package myProject.model;

import javafx.beans.property.*;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

/**
 * Die Transaction-Klasse repräsentiert eine finanzielle Transaktion,
 * die mit einem Benutzer, einem Konto und einer Kategorie verknüpft ist.
 * Eine Transaktion kann wiederkehrend sein und wird mit einem Datum und einer Uhrzeit erfasst.
 */
public class Transaction {

    private final StringProperty id;  // Eindeutige ID der Transaktion
    private final StringProperty description;  // Beschreibung der Transaktion
    private final ObjectProperty<Date> date;  // Datum der Transaktion
    private final ObjectProperty<Time> time;  // Uhrzeit der Transaktion
    private final DoubleProperty amount;  // Betrag der Transaktion
    private final StringProperty type;  // Typ der Transaktion (z. B. "Einnahme" oder "Ausgabe")

    // Verknüpfungen zu anderen Modellen
    private final ObjectProperty<User> user;  // Benutzer, der die Transaktion erstellt hat
    private final ObjectProperty<Account> account;  // Konto, dem die Transaktion zugeordnet ist
    private final ObjectProperty<Category> category;  // Kategorie der Transaktion

    // Wiederkehrende Transaktionen
    private final BooleanProperty isRecurring;  // Gibt an, ob die Transaktion wiederkehrend ist
    private StringProperty recurrenceInterval;  // Wiederholungsintervall (z. B. "monatlich")
    private final ObjectProperty<Date> endDate;  // Enddatum der wiederkehrenden Transaktion
    private final StringProperty recurringTransactionId;  // ID der ursprünglichen wiederkehrenden Transaktion

    private final StringProperty currency;  // Währung der Transaktion
    private final StringProperty status;  // Status der Transaktion (z. B. "abgeschlossen" oder "ausstehend")

    // Zeitstempel
    private final ObjectProperty<Date> createdAt;  // Zeitpunkt der Erstellung der Transaktion
    private final ObjectProperty<Date> updatedAt;  // Zeitpunkt der letzten Aktualisierung der Transaktion

    /**
     * Konstruktor für die Erstellung einer neuen Transaktion.
     *
     * @param description Beschreibung der Transaktion.
     * @param amount Betrag der Transaktion.
     * @param type Typ der Transaktion (z. B. "Einnahme" oder "Ausgabe").
     * @param user Benutzer, der die Transaktion erstellt hat.
     * @param account Konto, dem die Transaktion zugeordnet ist.
     * @param category Kategorie der Transaktion.
     * @param date Datum der Transaktion.
     * @param time Uhrzeit der Transaktion.
     * @param status Status der Transaktion (z. B. "abgeschlossen" oder "ausstehend").
     */
    public Transaction(String description, double amount, String type, User user, Account account, Category category, Date date, Time time, String status) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString());
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.type = new SimpleStringProperty(type);
        this.user = new SimpleObjectProperty<>(user);
        this.account = new SimpleObjectProperty<>(account);
        this.category = new SimpleObjectProperty<>(category);
        this.date = new SimpleObjectProperty<>(date);
        this.time = new SimpleObjectProperty<>(time);
        this.isRecurring = new SimpleBooleanProperty(false);
        this.recurrenceInterval = new SimpleStringProperty("");
        this.endDate = new SimpleObjectProperty<>(null);
        this.recurringTransactionId = new SimpleStringProperty(null);
        this.currency = new SimpleStringProperty("USD");
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(new Date());
        this.updatedAt = new SimpleObjectProperty<>(new Date());
    }

    // Getter- und Setter-Methoden für die Eigenschaften

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Date getDate() {
        return date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public Time getTime() {
        return time.get();
    }

    public void setTime(Time time) {
        this.time.set(time);
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public Account getAccount() {
        return account.get();
    }

    public void setAccount(Account account) {
        this.account.set(account);
    }

    public Category getCategory() {
        return category.get();
    }

    public void setCategory(Category category) {
        this.category.set(category);
    }

    public boolean isRecurring() {
        return isRecurring.get();
    }

    public void setRecurring(boolean isRecurring) {
        this.isRecurring.set(isRecurring);
    }

    public String getRecurrenceInterval() {
        return recurrenceInterval.get();
    }

    public void setRecurrenceInterval(String recurrenceInterval) {
        this.recurrenceInterval.set(recurrenceInterval);
    }

    public Date getEndDate() {
        return endDate.get();
    }

    public void setEndDate(Date endDate) {
        this.endDate.set(endDate);
    }

    public String getCurrency() {
        return currency.get();
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public Date getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt.set(createdAt);
    }

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public String getRecurringTransactionId() {
        return recurringTransactionId.get();
    }

    public void setRecurringTransactionId(String recurringTransactionId) {
        this.recurringTransactionId.set(recurringTransactionId);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty typeProperty() {
        return type;
    }

    /**
     * Markiert eine Transaktion als wiederkehrend und legt das Wiederholungsintervall und das Enddatum fest.
     *
     * @param interval Wiederholungsintervall (z. B. "monatlich").
     * @param endDate Enddatum der wiederkehrenden Transaktion.
     */
    public void markAsRecurring(String interval, Date endDate) {
        this.isRecurring.set(true);
        this.recurrenceInterval.set(interval);
        this.endDate.set(endDate);
    }
}
