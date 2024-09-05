package myProject.model;

import java.util.UUID;

public class Account {

    private String id;
    private String userId;  // This is used to associate the account with a specific user
    private String name;
    private double balance;

    // Constructor without an ID, used when creating a new account (auto-generate ID)
    public Account(String userId, String name, double balance) {
        this.id = UUID.randomUUID().toString();  // Auto-generate a unique ID
        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }

    // Constructor with an ID, used when loading from the database
    public Account(String id, String userId, String name, double balance) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }

    // Getters and Setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
