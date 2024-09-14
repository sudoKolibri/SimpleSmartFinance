package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.AccountController;
import myProject.model.Account;

import java.util.List;

public class AccountView {

    private final AccountController accountController = new AccountController();
    private final String currentUserId;
    private Button createAccountButton;  // Declare createAccountButton at the class level
    private Label overallBalanceLabel;   // Declare overallBalanceLabel at the class level

    public AccountView(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    // Load AccountView into the dynamic content area of MainView (vertical split layout)
    public void loadIntoPane(BorderPane root) {
        VBox mainLayout = new VBox(30);  // Vertical layout for both the summary and account list
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);  // Center the content
        mainLayout.setSpacing(40);  // Space between the summary and account list

        // ==================== Top Section: Summary Panel ====================
        VBox summaryLayout = new VBox(20);  // Summary content centered
        summaryLayout.setPadding(new Insets(20));
        summaryLayout.setAlignment(Pos.CENTER);  // Center content

        // Prominent Overall Balance
        overallBalanceLabel = new Label();
        overallBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #50fa7b;");

        // Recent Transactions (Placeholder)
        Label recentTransactionsLabel = new Label("Recent Transactions (placeholder)");
        recentTransactionsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f8f8f2;");

        // Add summary components
        summaryLayout.getChildren().addAll(overallBalanceLabel, recentTransactionsLabel);

        // ==================== Bottom Section: Account List ====================
        VBox accountsLayout = new VBox(20);
        accountsLayout.setPadding(new Insets(20));
        accountsLayout.setAlignment(Pos.CENTER);  // Center content
        accountsLayout.setMaxWidth(Double.MAX_VALUE);  // Allow VBox to use full width

        // Account cards container
        VBox accountsContainer = new VBox(20);
        accountsContainer.setAlignment(Pos.CENTER);

        // Load and display accounts as square cards (bigger, centered)
        List<Account> accounts = accountController.getAllAccountsForUser(currentUserId);
        for (Account account : accounts) {
            HBox accountCard = createAccountCard(account);
            accountsContainer.getChildren().add(accountCard);
        }

        // Create the button at the class level
        createAccountButton = new Button("+ Add Account");
        createAccountButton.setPrefWidth(150);  // Set the preferred width to 150px
        createAccountButton.setMaxWidth(200);  // Allow the button to take its full width
        createAccountButton.setOnAction(e -> showCreateAccountForm(accountsLayout));  // Pass the accounts layout to show form in the same area

        // Add components to the bottom layout (account list)
        accountsLayout.getChildren().addAll(createAccountButton, accountsContainer);

        // ==================== Add Summary and Account List ====================
        mainLayout.getChildren().addAll(summaryLayout, accountsLayout);  // Add both sections to the vertical layout

        // Set the center content of MainView to AccountView
        root.setCenter(mainLayout);

        // Update the balance initially
        updateOverallBalance();
    }

    // Update the overall balance label
    private void updateOverallBalance() {
        double totalBalance = accountController.getOverallBalanceForUser(currentUserId);
        overallBalanceLabel.setText("Total Balance: $" + totalBalance);
    }


    // Show the account creation form within the dynamic area
    private void showCreateAccountForm(VBox accountsLayout) {
        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(20));
        formCard.setAlignment(Pos.CENTER);
        formCard.setStyle("-fx-background-color: #44475a; -fx-border-color: #ff79c6; -fx-border-radius: 10px;");
        formCard.setMaxWidth(300);  // Limit the width of the form card

        Label nameLabel = new Label("Account Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f8f8f2;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter account name");
        nameField.setMaxWidth(250);

        Label balanceLabel = new Label("Initial Balance:");
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f8f8f2;");

        TextField balanceField = new TextField();
        balanceField.setPromptText("Enter initial balance");
        balanceField.setMaxWidth(250);

        Button submitButton = new Button("Create Account");
        submitButton.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36;");
        submitButton.setOnAction(e -> {
            String accountName = nameField.getText();
            double initialBalance = Double.parseDouble(balanceField.getText());

            // Handle account creation
            if (accountController.addAccount(currentUserId, accountName, initialBalance)) {
                accountsLayout.getChildren().remove(formCard);  // Remove the form after submission
                refreshAccountList(accountsLayout);  // Refresh account list
                updateOverallBalance();  // Update the overall balance
            }
        });

        formCard.getChildren().addAll(nameLabel, nameField, balanceLabel, balanceField, submitButton);
        accountsLayout.getChildren().add(0, formCard);  // Add the form at the top of the account list
    }

    // Refresh the account list dynamically after adding a new account, but keep the "Add Account" button
    private void refreshAccountList(VBox accountsLayout) {
        accountsLayout.getChildren().clear();  // Clear existing account cards

        // Re-add the "Add Account" button at the top
        accountsLayout.getChildren().add(createAccountButton);

        // Load and display the updated list of accounts
        List<Account> accounts = accountController.getAllAccountsForUser(currentUserId);
        for (Account account : accounts) {
            HBox accountCard = createAccountCard(account);
            accountsLayout.getChildren().add(accountCard);
        }
    }

    private HBox createAccountCard(Account account) {
        HBox card = new HBox();
        card.getStyleClass().add("account-card");  // Apply the CSS class

        // Add padding and alignment
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(account.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f8f8f2;");

        Label balanceLabel = new Label("$" + account.getBalance());
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8be9fd;");

        VBox cardContent = new VBox(10);  // Vertical box for account name and balance, with spacing
        cardContent.setAlignment(Pos.CENTER);
        cardContent.getChildren().addAll(nameLabel, balanceLabel);

        card.getChildren().add(cardContent);

        // Add hover effect (scaling)
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }


}
