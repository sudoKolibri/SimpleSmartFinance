package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import myProject.controller.AccountController;
import myProject.controller.TransactionController;
import myProject.model.Account;
import myProject.model.Transaction;
import myProject.view.detail.AccountDetailView;
import myProject.view.util.ViewUtils;
import myProject.util.LoggerUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Diese Klasse ist verantwortlich für die Anzeige und Verwaltung der Kontoübersicht und
 * ermöglicht dem Benutzer, neue Konten zu erstellen und vorhandene anzuzeigen.
 */
public class AccountView {

    private final AccountController accountController;
    private final TransactionController transactionController;
    private final String currentUserId;
    private Button createAccountButton;
    private Label overallBalanceLabel;
    private BorderPane root;

    /**
     * Konstruktor, um die notwendigen Controller zu initialisieren.
     *
     * @param currentUserId       Die ID des aktuellen Benutzers.
     * @param accountController   Der Controller zur Verwaltung von Konten.
     * @param transactionController Der Controller zur Verwaltung von Transaktionen.
     */
    public AccountView(String currentUserId, AccountController accountController, TransactionController transactionController) {
        this.currentUserId = currentUserId;
        this.accountController = accountController;
        this.transactionController = transactionController;
    }

    /**
     * Lädt die AccountView in den zentralen Bereich der MainView.
     *
     * @param root Das Root-Layout, in das die Ansicht geladen werden soll.
     * @throws SQLException bei einem Fehler beim Laden der Konten.
     */
    public void loadIntoPane(BorderPane root) throws SQLException {
        try {
            System.out.println("AccountView.loadIntoPane: Loading AccountView...");
            this.root = root;

            // Header bleibt oben
            Label headerLabel = new Label("Accounts & Transactions");
            headerLabel.getStyleClass().add("header-label");

            // Hauptlayout für die Ansicht, mit Header oben
            VBox mainLayout = new VBox();
            mainLayout.getStyleClass().add("main-layout");
            mainLayout.setAlignment(Pos.TOP_CENTER);

            // Layout für den zentralen Inhalt (Bilanz, Konten, Add-Button)
            VBox centerContent = new VBox(30);
            centerContent.setAlignment(Pos.CENTER);  // Inhalt zentrieren
            centerContent.setPadding(new Insets(50, 50, 50, 50)); // optionaler Abstand

            // Gesamtbilanz prominent anzeigen
            overallBalanceLabel = new Label();
            overallBalanceLabel.getStyleClass().add("balance-label");
            updateOverallBalance();

            // Layout für die Kontoliste
            VBox accountsLayout = new VBox(20);
            accountsLayout.setAlignment(Pos.CENTER);

            // Zeige Konten im Grid-Format
            showAccounts(accountsLayout);

            // Button zum Hinzufügen von Konten
            createAccountButton = new Button("+ Add Account");
            createAccountButton.getStyleClass().add("button");
            createAccountButton.setOnAction(e -> showCreateAccountForm(accountsLayout));

            // Füge den Button zur Kontoliste hinzu
            accountsLayout.getChildren().add(createAccountButton);

            // Füge Bilanz und Kontoliste zum zentralen Inhalt hinzu
            centerContent.getChildren().addAll(overallBalanceLabel, accountsLayout);

            // Füge Header oben und den zentrierten Inhalt darunter hinzu
            mainLayout.getChildren().addAll(headerLabel, centerContent);

            // Setze das Hauptlayout in die Mitte des Root-Panes
            root.setCenter(mainLayout);

            System.out.println("AccountView.loadIntoPane: AccountView loaded successfully.");
        } catch (SQLException e) {
            LoggerUtils.logError(AccountView.class.getName(), "Error while loading AccountView", e);
            throw e;
        }
    }



    /**
     * Aktualisiert die Gesamtbilanz des Benutzers basierend auf abgeschlossenen Transaktionen.
     *
     * @throws SQLException bei einem Fehler beim Berechnen der Bilanz.
     */
    private void updateOverallBalance() throws SQLException {
        try {
            double totalBalance = accountController.getAllAccountsForUser(currentUserId)
                    .stream()
                    .mapToDouble(account -> {
                        try {
                            return accountController.calculateUpdatedBalanceForCompletedTransactions(account);
                        } catch (SQLException e) {
                            LoggerUtils.logError(AccountView.class.getName(), "Error while calculating balance for account: " + account.getName(), e);
                            throw new RuntimeException(e);
                        }
                    })
                    .sum();

            overallBalanceLabel.setText("Total Balance: $" + String.format("%.2f", totalBalance));
        } catch (SQLException e) {
            LoggerUtils.logError(AccountView.class.getName(), "Error while updating overall balance for user: " + currentUserId, e);
            throw e;
        }
    }

    /**
     * Zeigt alle Konten des Benutzers in einem Grid-Layout an.
     *
     * @param accountsLayout Das Layout, in dem die Konten angezeigt werden sollen.
     * @throws SQLException bei einem Fehler beim Abrufen der Konten.
     */
    private void showAccounts(VBox accountsLayout) throws SQLException {
        try {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(20));
            gridPane.setHgap(20);
            gridPane.setVgap(20);
            gridPane.setAlignment(Pos.CENTER);

            List<Account> accounts = accountController.getAllAccountsForUser(currentUserId);

            int row = 0, col = 0;
            for (Account account : accounts) {
                HBox accountCard = createAccountCard(account);
                gridPane.add(accountCard, col, row);

                col++;
                if (col == 3) {  // Layoutanpassung
                    col = 0;
                    row++;
                }
            }

            // Füge das GridPane zum Layout hinzu
            accountsLayout.getChildren().add(gridPane);
        } catch (SQLException e) {
            LoggerUtils.logError(AccountView.class.getName(), "Error while displaying accounts for user: " + currentUserId, e);
            throw e;
        }
    }

    /**
     * Zeigt das Formular zum Erstellen eines neuen Kontos an.
     *
     * @param accountsLayout Das Layout, in dem das Formular angezeigt werden soll.
     */
    private void showCreateAccountForm(VBox accountsLayout) {
        createAccountButton.setVisible(false);

        VBox formContainer = new VBox(10);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(10));

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setMaxWidth(200);

        Label formTitle = new Label("Create New Account");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Eingabefelder
        TextField accountNameField = new TextField();
        accountNameField.setPromptText("Account Name");

        TextField balanceField = new TextField();
        balanceField.setPromptText("Initial Balance");

        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSaveButtonClick(accountsLayout, accountNameField, balanceField, formContainer));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            accountsLayout.getChildren().remove(formContainer);
            createAccountButton.setVisible(true);
        });

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        formLayout.getChildren().addAll(formTitle, accountNameField, balanceField, buttonBox);
        formContainer.getChildren().add(formLayout);

        accountsLayout.getChildren().add(formContainer);
    }

    /**
     * Verarbeitet das Klicken auf den Speichern-Button beim Erstellen eines neuen Kontos.
     *
     * @param accountsLayout Das Layout, in dem die Konten angezeigt werden.
     * @param accountNameField Textfeld für den Namen des neuen Kontos.
     * @param balanceField Textfeld für den Startbetrag des neuen Kontos.
     * @param formContainer Das Formularcontainer, das entfernt werden soll, wenn das Konto erstellt wurde.
     */
    private void handleSaveButtonClick(VBox accountsLayout, TextField accountNameField, TextField balanceField, VBox formContainer) {
        try {
            String accountName = accountNameField.getText();
            double initialBalance = Double.parseDouble(balanceField.getText());

            // Überprüfen, ob ein Konto mit demselben Namen bereits existiert
            if (accountController.doesAccountExist(currentUserId, accountName)) {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Account name already exists. Please chose a different name.");
                return;
            }

            // Versuch, ein neues Konto hinzuzufügen
            boolean success = accountController.addAccount(currentUserId, accountName, 0.0);
            if (success) {
                // Lade das neu erstellte Konto
                Account createdAccount = accountController.findAccountByName(currentUserId, accountName);

                // Erstelle eine Transaktion für den Startbetrag, nur wenn initialBalance != 0
                if (initialBalance != 0.0) {
                    Transaction initialTransaction = new Transaction(
                            "Initial Balance",
                            initialBalance,
                            initialBalance >= 0 ? "income" : "expense",
                            null,
                            createdAccount,
                            null,
                            new java.sql.Date(System.currentTimeMillis()),
                            new java.sql.Time(System.currentTimeMillis())
                    );

                    // Speichern der Transaktion
                    transactionController.createTransaction(initialTransaction);
                    System.out.println("AccountView.handleSaveButtonClick: Erste Transaktion abgeschlossen. - " + accountName + " " + initialTransaction);
                }

                // Aktualisiere die Kontenübersicht und Gesamtbilanz
                updateOverallBalance();
                refreshAccountList(accountsLayout);

                createAccountButton.setVisible(true);
            } else {
                ViewUtils.showAlert(Alert.AlertType.ERROR, "Account konnte nicht erzeugt werden. Versuche es erneut.");
            }
        } catch (NumberFormatException ex) {
            // Logge Fehler bei ungültigem Betrag
            LoggerUtils.logError(AccountView.class.getName(), "Falsches Format: " + balanceField.getText(), ex);
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Invalid balance. Please enter a valid number.");
        } catch (SQLException e) {
            // Logge Fehler beim Speichern des Kontos
            LoggerUtils.logError(AccountView.class.getName(), "Error while saving new account for user: " + currentUserId, e);
            ViewUtils.showAlert(Alert.AlertType.ERROR, "Failed to save account. Please try again.");
        } finally {
            // Entferne das Formular nach der Verarbeitung
            accountsLayout.getChildren().remove(formContainer);
            createAccountButton.setVisible(true);
        }
    }


    /**
     * Aktualisiert die Kontenliste und fügt den "Add Account"-Button erneut hinzu.
     *
     * @param accountsLayout Das Layout, in dem die Konten angezeigt werden.
     * @throws SQLException bei einem Fehler beim Abrufen der Konten.
     */
    private void refreshAccountList(VBox accountsLayout) throws SQLException {
        try {
            accountsLayout.getChildren().clear();
            showAccounts(accountsLayout);
            accountsLayout.getChildren().add(createAccountButton);
            updateOverallBalance();
        } catch (SQLException e) {
            // Logge Fehler beim Aktualisieren der Kontenliste
            LoggerUtils.logError(AccountView.class.getName(), "Fehler beim erneuern des Accountliste für user: " + currentUserId, e);
            throw e;
        }
    }

    /**
     * Erstellt eine visuelle Darstellung (Card) für ein Konto.
     *
     * @param account Das Konto, das angezeigt werden soll.
     * @return Eine HBox, die das Konto darstellt.
     * @throws SQLException bei einem Fehler beim Berechnen der Kontobilanz.
     */
    private HBox createAccountCard(Account account) throws SQLException {
        try {
            HBox card = new HBox();
            card.getStyleClass().add("account-card");
            card.setPadding(new Insets(20));
            card.setAlignment(Pos.CENTER);
            card.setSpacing(10);
            card.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(card, Priority.ALWAYS);

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
        } catch (SQLException e) {
            // Logge Fehler beim Erstellen der Kontoübersicht
            LoggerUtils.logError(AccountView.class.getName(), "Fehler beim kreieren des accounts: " + account.getName(), e);
            throw e;
        }
    }
}
