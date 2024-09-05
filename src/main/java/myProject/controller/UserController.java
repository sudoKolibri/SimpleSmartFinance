package myProject.controller;

import myProject.service.UserService;

public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    // Handle login request (returns boolean)
    public boolean login(String username, String password) {
        return userService.authenticateUser(username, password);  // Return true or false based on authentication
    }

    // Get the currently logged-in user
    public myProject.model.User getLoggedInUser() {
        return userService.getLoggedInUser();
    }

    // Handle registration request
    public boolean register(String username, String password) {
        return userService.registerUser(username, password);  // Register the user if the username isn't taken
    }
}
