package myProject.service;

import myProject.model.User;
import myProject.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;
    private User loggedInUser;  // Store the logged-in user after authentication

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // Authenticate user and return boolean
    public boolean authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            this.loggedInUser = user;  // Store the logged-in user
            return true;
        }
        return false;
    }

    // Get the currently logged-in user
    public User getLoggedInUser() {
        return loggedInUser;
    }

    // Register a new user
    public boolean registerUser(String username, String password) {
        // Check if the username is already taken
        if (userRepository.findByUsername(username).isPresent()) {
            return false;  // Username is already taken
        }
        // Create and add the new user
        User newUser = new User(username, password);
        return userRepository.addUser(newUser);
    }
}
