package myProject.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class Account {

    private StringProperty id;
    private StringProperty userId;
    private StringProperty name;
    private DoubleProperty balance;

    // Constructor without an ID (auto-generate ID)
    public Account(String userId, String name, double balance) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString());
        this.userId = new SimpleStringProperty(userId);
        this.name = new SimpleStringProperty(name);
        this.balance = new SimpleDoubleProperty(balance);
    }

    // Constructor with an ID (used when loading from the database)
    public Account(String id, String userId, String name, double balance) {
        this.id = new SimpleStringProperty(id);
        this.userId = new SimpleStringProperty(userId);
        this.name = new SimpleStringProperty(name);
        this.balance = new SimpleDoubleProperty(balance);
    }

    public Account(String accountId) {
    }

    // Getters and Setters
    public StringProperty idProperty() { return id; }
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }

    public StringProperty userIdProperty() { return userId; }
    public String getUserId() { return userId.get(); }
    public void setUserId(String userId) { this.userId.set(userId); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public DoubleProperty balanceProperty() { return balance; }
    public double getBalance() { return balance.get(); }
    public void setBalance(double balance) { this.balance.set(balance); }
}
