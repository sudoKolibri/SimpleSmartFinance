package myProject.model;

import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String password;

    // Constructor for creating a new user (with auto-generated ID)
    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();  // Auto-generate a unique ID
        this.username = username;
        this.password = password;
    }

    // Constructor for loading an existing user (with provided ID)
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
