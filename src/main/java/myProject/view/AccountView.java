package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.AccountController;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Transaction;
import myProject.view.detail.AccountDetailView;
import myProject.view.util.ViewUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class AccountView {

    private final AccountController accountController;
    private final TransactionController transactionController;
    private final String currentUserId;
    private Button createAccountButton;
    private Label overallBalanceLabel;
    private BorderPane root;

    // Constructor with necessary controllers
    public AccountView(String currentUserId, AccountController accountController, TransactionController transactionController) {
        this.currentUserId = currentUserId;
        this.accountController = accountController;
        this.transactionController = transactionController;
    }

    // Load AccountView into the dynamic content area of MainView
    public void loadIntoPane(BorderPane root) throws SQLException {
        this.root = root;
        VBox mainLayout = new VBox(30);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setSpacing(40);

        // Top Section: Summary Panel
        VBox summaryLayout = new VBox(20);
        summaryLayout.setPadding(new Insets(20));
        summaryLayout.setAlignment(Pos.CENTER);

        // Prominent Overall Balance
        overallBalanceLabel = new Label();
        overallBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #50fa7b;");
        summaryLayout.getChildren().add(overallBalanceLabel);

        // Bottom Section: Account List
        VBox accountsLayout = new VBox(20);
        accountsLayout.setPadding(new Insets(20));
        accountsLayout.setAlignment(Pos.CENTER);
        accountsLayout.setMaxWidth(Double.MAX_VALUE);

        // Show accounts in a grid format
        showAccounts(accountsLayout);

        // Create the button at the class level
        createAccountButton = new Button("+ Add Account");
        createAccountButton.setPrefWidth(150);
        createAccountButton.setMaxWidth(200);
        createAccountButton.setOnAction(e -> showCreateAccountForm(accountsLayout));

        // Add components to the bottom layout
        accountsLayout.getChildren().add(createAccountButton);

        // Add Summary and Account List
        mainLayout.getChildren().addAll(summaryLayout, accountsLayout);

        // Set the center content of MainView to AccountView
        root.setCenter(mainLayout);

        // Update the balance initially
        updateOverallBalance();
    }

    private void updateOverallBalance() throws SQLException {
        double totalBalance = accountController.getAllAccountsForUser(currentUserId)
                .stream()
                .mapToDouble(account -> {
                    double balance = accountController.calculateUpdatedBalanceForCompletedTransactions(account);
                    System.out.println("Calculated balance for account " + account.getName() + ": " + balance);
                    return balance;
                })
                .sum();

        System.out.println("Total calculated balance: " + totalBalance);
        overallBalanceLabel.setText("Total Balance: $" + String.format("%.2f", totalBalance));
    }


    // Show all accounts in a grid layout
    private void showAccounts(VBox accountsLayout) throws SQLException {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        List<Account> accounts = accountController.getAllAccountsForUser(currentUserId);

        int row = 0, col = 0;
        for (Account account : accounts) {
            HBox accountCard = createAccountCard(account); // Correctly call without passing an unnecessary parameter
            gridPane.add(accountCard, col, row);

            col++;
            if (col == 3) {  // Adjust column count to fit your desired layout
                col = 0;
                row++;
            }
        }

        // Add the grid pane to the accounts layout
        accountsLayout.getChildren().add(gridPane);
    }

    // Show the account creation form below the account grid
    private void showCreateAccountForm(VBox accountsLayout) {

        // Hide the "Add Account" button when showing the form
        createAccountButton.setVisible(false);

        // Create the container for the form
        VBox formContainer = new VBox(10);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(10));

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setMaxWidth(200); // Set the width to match the account cards

        Label formTitle = new Label("Create New Account");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Input fields
        TextField accountNameField = new TextField();
        accountNameField.setPromptText("Account Name");
        accountNameField.setMaxWidth(Double.MAX_VALUE);

        TextField balanceField = new TextField();
        balanceField.setPromptText("Initial Balance");
        balanceField.setMaxWidth(Double.MAX_VALUE);

        // Save button with improved error handling
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSaveButtonClick(accountsLayout, accountNameField, balanceField, formContainer));

        // Cancel button that restores visibility of the "Add Account" button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            accountsLayout.getChildren().remove(formContainer);
            createAccountButton.setVisible(true);
        });

        // Button box layout
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add components to form layout
        formLayout.getChildren().addAll(formTitle, accountNameField, balanceField, buttonBox);
        formContainer.getChildren().add(formLayout);

        // Add the form container below the account grid
        accountsLayout.getChildren().add(formContainer);
    }

    // Methode zum Verarbeiten des Klicks auf den Speichern-Button beim Erstellen eines neuen Kontos
    private void handleSaveButtonClick(VBox accountsLayout, TextField accountNameField, TextField balanceField, VBox formContainer) {
        try {
            String accountName = accountNameField.getText();
            double initialBalance = Double.parseDouble(balanceField.getText());

            // Überprüfen, ob ein Konto mit dem gleichen Namen bereits existiert
            if (accountController.doesAccountExist(currentUserId, accountName)) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "An account with this name already exists. Please choose a different name.");
                return;
            }

            // Versuch, ein neues Konto hinzuzufügen
            boolean success = accountController.addAccount(currentUserId, accountName, 0.0);  // Balance auf 0 setzen, da sie durch die Transaktion verwaltet wird
            if (success) {
                // Lade das neue Konto anhand des Namens
                Account createdAccount = accountController.findAccountByName(currentUserId, accountName);

                // Erstelle eine Transaktion für den Startbetrag
                Transaction initialTransaction = new Transaction(
                        "Initial Balance",  // Beschreibung der Transaktion
                        initialBalance,     // Betrag (kann positiv oder negativ sein)
                        initialBalance >= 0 ? "income" : "expense",  // Einnahme oder Ausgabe basierend auf dem Betrag
                        null,               // Keine Kategorie notwendig
                        createdAccount,     // Verknüpft mit dem neuen Konto
                        null,               // Keine spezifische Kategorie
                        new Date(System.currentTimeMillis()),  // Aktuelles Datum
                        new Time(System.currentTimeMillis()),  // Aktuelle Uhrzeit
                        "completed"         // Status der Transaktion
                );

                // Transaktion speichern
                transactionController.createTransaction(initialTransaction);
                System.out.println("AccountView.handleSaveButtonClick: Initial balance transaction created for account - " + accountName + " " + initialTransaction);



                // Aktualisiere die Kontoübersicht und Gesamtbilanz
                refreshAccountList(accountsLayout);
                updateOverallBalance();
                createAccountButton.setVisible(true);
            } else {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to create account. Please try again.");
            }
        } catch (NumberFormatException ex) {
            // Zeige eine Warnung bei ungültigem Eingabewert für den Saldo
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid balance. Please enter a valid number.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // Entferne das Formular nach dem Verarbeiten
            accountsLayout.getChildren().remove(formContainer);
            createAccountButton.setVisible(true);
        }
    }


    private Button getButton(VBox accountsLayout, TextField accountNameField, TextField balanceField) {
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                String accountName = accountNameField.getText();
                double initialBalance = Double.parseDouble(balanceField.getText());

                // Check if account with the same name already exists for the user
                if (accountController.doesAccountExist(currentUserId, accountName)) {
                    // Show error message if account name is already in use
                    ViewUtils.showAlert(Alert.AlertType.ERROR, "An account with this name already exists. Please choose a different name.");
                    return; // Exit the method without creating the account
                }

                // Add account using the controller
                boolean success = accountController.addAccount(currentUserId, accountName, initialBalance);
                if (success) {
                    System.out.println("Account created successfully.");
                    refreshAccountList(accountsLayout); // Refresh the list to show the new account
                    updateOverallBalance(); // Ensure the balance is updated immediately
                    createAccountButton.setVisible(true);
                } else {
                    System.err.println("Failed to create account.");
                }
            } catch (NumberFormatException ex) {
                // Display an alert when the balance input is invalid
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid balance. Please enter a valid number.");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return saveButton;
    }


    // Refresh the account list
    private void refreshAccountList(VBox accountsLayout) throws SQLException {
        accountsLayout.getChildren().clear();
        showAccounts(accountsLayout);
        accountsLayout.getChildren().add(createAccountButton);
        updateOverallBalance();
    }

    // Create an account card
    private HBox createAccountCard(Account account) {
        HBox card = new HBox();
        card.getStyleClass().add("account-card");

        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(account.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f8f8f2;");

        // Berechne die aktualisierte Bilanz basierend auf abgeschlossenen Transaktionen
        double updatedBalance = accountController.calculateUpdatedBalanceForCompletedTransactions(account);

        // Zeige die aktualisierte Bilanz in der AccountCard an
        Label balanceLabel = new Label(String.format("%.2f", updatedBalance));
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8be9fd;");

        VBox cardContent = new VBox(10);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.getChildren().addAll(nameLabel, balanceLabel);

        card.getChildren().add(cardContent);

        // Öffne die Detailansicht des Kontos, wenn darauf geklickt wird
        card.setOnMouseClicked(e -> {
            AccountDetailView accountDetailView = new AccountDetailView(accountController, transactionController, root);
            accountDetailView.showAccountDetailView(account);
        });

        return card;
    }


}
