package myProject.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * Die Account-Klasse stellt ein Konto dar, das einem Benutzer zugeordnet ist.
 * Sie enthält Informationen wie die ID des Kontos, die ID des Benutzers, den Namen des Kontos und den Kontostand.
 * Die Klasse verwendet JavaFX Properties, um eine Datenbindung in der Benutzeroberfläche zu unterstützen.
 */
public class Account {

    private final StringProperty id;  // Eindeutige ID des Kontos
    private final StringProperty userId;  // ID des Benutzers, dem das Konto gehört
    private final StringProperty name;  // Name des Kontos
    private final DoubleProperty balance;  // Kontostand

    /**
     * Konstruktor ohne ID. Eine neue ID wird automatisch generiert.
     *
     * @param userId  Die ID des Benutzers, dem das Konto gehört.
     * @param name    Der Name des Kontos.
     * @param balance Der anfängliche Kontostand.
     */
    public Account(String userId, String name, double balance) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString());
        this.userId = new SimpleStringProperty(userId);
        this.name = new SimpleStringProperty(name);
        this.balance = new SimpleDoubleProperty(balance);
    }

    /**
     * Konstruktor mit ID. Wird verwendet, wenn ein Konto aus der Datenbank geladen wird.
     *
     * @param id      Die eindeutige ID des Kontos.
     * @param userId  Die ID des Benutzers, dem das Konto gehört.
     * @param name    Der Name des Kontos.
     * @param balance Der anfängliche Kontostand.
     */
    public Account(String id, String userId, String name, double balance) {
        this.id = new SimpleStringProperty(id);
        this.userId = new SimpleStringProperty(userId);
        this.name = new SimpleStringProperty(name);
        this.balance = new SimpleDoubleProperty(balance);
    }

    // Getter und Setter für die Eigenschaften


    public StringProperty idProperty() {
        return id;
    }

    public String getId() {
        return id.get();
    }


    public void setId(String id) {
        this.id.set(id);
    }


    public StringProperty userIdProperty() {
        return userId;
    }


    public String getUserId() {
        return userId.get();
    }


    public void setUserId(String userId) {
        this.userId.set(userId);
    }


    public StringProperty nameProperty() {
        return name;
    }


    public String getName() {
        return name.get();
    }


    public void setName(String name) {
        this.name.set(name);
    }


    public DoubleProperty balanceProperty() {
        return balance;
    }


    public double getBalance() {
        return balance.get();
    }


    public void setBalance(double balance) {
        this.balance.set(balance);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id.get(), account.id.get());  // Vergleiche basierend auf der ID
    }


    @Override
    public int hashCode() {
        return Objects.hash(id.get());  // Hash basierend auf der ID
    }
}
