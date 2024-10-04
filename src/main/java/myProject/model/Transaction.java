package myProject.model;

import javafx.beans.property.*;
import java.sql.Time;  // Verwende java.sql.Time für Zeit
import java.util.UUID;

/**
 * Die Transaction-Klasse repräsentiert eine finanzielle Transaktion,
 * die mit einem Benutzer, einem Konto und einer Kategorie verknüpft ist.
 */
public class Transaction {

    private final StringProperty id;  // Eindeutige ID der Transaktion
    private final StringProperty description;  // Beschreibung der Transaktion
    private final ObjectProperty<java.sql.Date> date;  // Verwende java.sql.Date anstelle von java.util.Date
    private final ObjectProperty<Time> time;  // Uhrzeit der Transaktion
    private final DoubleProperty amount;  // Betrag der Transaktion
    private final StringProperty type;  // Typ der Transaktion (z. B. "Einnahme" oder "Ausgabe")

    // Verknüpfungen zu anderen Modellen
    private final ObjectProperty<User> user;  // Benutzer, der die Transaktion erstellt hat
    private final ObjectProperty<Account> account;  // Konto, dem die Transaktion zugeordnet ist
    private final ObjectProperty<Category> category;  // Kategorie der Transaktion

    // Zeitstempel
    private final ObjectProperty<java.sql.Date> createdAt;  // Zeitpunkt der Erstellung der Transaktion
    private final ObjectProperty<java.sql.Date> updatedAt;  // Zeitpunkt der letzten Aktualisierung der Transaktion

    /**
     * Konstruktor für die Erstellung einer neuen Transaktion.
     *
     * @param description Beschreibung der Transaktion.
     * @param amount      Betrag der Transaktion.
     * @param type        Typ der Transaktion (z. B. "Einnahme" oder "Ausgabe").
     * @param user        Benutzer, der die Transaktion erstellt hat.
     * @param account     Konto, dem die Transaktion zugeordnet ist.
     * @param category    Kategorie der Transaktion.
     * @param date        Datum der Transaktion.
     * @param time        Uhrzeit der Transaktion.
     */
    public Transaction(String description, double amount, String type, User user, Account account, Category category, java.sql.Date date, Time time) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString());
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.type = new SimpleStringProperty(type);
        this.user = new SimpleObjectProperty<>(user);
        this.account = new SimpleObjectProperty<>(account);
        this.category = new SimpleObjectProperty<>(category);
        this.date = new SimpleObjectProperty<>(date);
        this.time = new SimpleObjectProperty<>(time);

        this.createdAt = new SimpleObjectProperty<>(new java.sql.Date(System.currentTimeMillis()));
        this.updatedAt = new SimpleObjectProperty<>(new java.sql.Date(System.currentTimeMillis()));
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

    public java.sql.Date getDate() {
        return date.get();
    }

    public void setDate(java.sql.Date date) {
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

    public StringProperty descriptionProperty() {
        return description;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<java.sql.Date> dateProperty() {
        return date;
    }

    public StringProperty typeProperty() {
        return type;
    }

}
