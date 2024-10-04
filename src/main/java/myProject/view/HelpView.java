package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.util.LoggerUtils;

/**
 * Die HelpView-Klasse zeigt eine Anleitung zur Nutzung der Anwendung.
 * Diese Ansicht erklärt dem Benutzer die Funktionen der Hauptbereiche der Anwendung.
 */
public class HelpView {

    private final BorderPane root;

    /**
     * Konstruktor zur Initialisierung der HelpView.
     *
     * @param root Das Root-Layout, in das die HelpView eingefügt wird.
     */
    public HelpView(BorderPane root) {
        this.root = root;
    }

    /**
     * Lädt die Hilfsansicht in das Root-Layout.
     */
    public void loadIntoPane() {
        LoggerUtils.logInfo(HelpView.class.getName(), "HelpView wird geladen.");

        // VBox für die Hilfetexte
        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setSpacing(40);

        // Erstellen und Hinzufügen der drei großen Karten
        mainLayout.getChildren().addAll(
                createHelpCard("Account Section - Guide", this::showAccountHelp),
                createHelpCard("Category Section - Guide", this::showCategoryHelp),
                createHelpCard("Reports Section - Guide", this::showReportsHelp)
        );

        // Hinzufügen des Layouts zum Root
        root.setCenter(mainLayout);
    }

    /**
     * Erstellt eine große Karte für die Hilfsansicht.
     *
     * @param title Der Titel der Karte.
     * @param action Die Aktion, die beim Klicken auf die Karte ausgeführt wird.
     * @return Ein VBox-Element, das die Karte darstellt.
     */
    private VBox createHelpCard(String title, Runnable action) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("help-card");

        Label label = new Label(title);
        label.getStyleClass().add("help-card-label");

        card.getChildren().add(label);
        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    /**
     * Zeigt den Hilfetext für den Account-Bereich.
     */
    private void showAccountHelp() {
        VBox helpContent = new VBox(20);
        helpContent.setAlignment(Pos.CENTER);
        helpContent.setPadding(new Insets(20));
        helpContent.getStyleClass().add("help-content");

        Label helpTitle = new Label("Account Section - Guide");
        helpTitle.getStyleClass().add("help-title");

        Label helpText = new Label("""
                In the Accounts section, you can manage all your financial accounts. \
                This is where you can add, edit, or delete accounts, as well as view important details like the current balance of each account.
                
                Adding a new account is easy! Simply click on the 'Add Account' button, provide the account name \
                (for example, 'Savings Account' or 'Credit Card'), and set an initial balance.
                
                If you want to update an existing account, just click on the account in the list, make the necessary changes, \
                and save them. You can adjust balances, update account names, or delete an account you no longer use.
                
                The system automatically updates your balances as you record transactions. Each account’s balance reflects your \
                spending and income, helping you keep track of your finances effortlessly.""");
        helpText.getStyleClass().add("help-text");
        helpText.setWrapText(true);

        // Back Button
        Button backButton = new Button("Back to Help");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> loadIntoPane());

        helpContent.getChildren().addAll(helpTitle, helpText, backButton);

        // ScrollPane hinzufügen
        ScrollPane scrollPane = new ScrollPane(helpContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);
    }

    /**
     * Zeigt den Hilfetext für den Kategorie-Bereich.
     */
    private void showCategoryHelp() {
        VBox helpContent = new VBox(20);
        helpContent.setAlignment(Pos.CENTER);
        helpContent.setPadding(new Insets(20));
        helpContent.getStyleClass().add("help-content");

        Label helpTitle = new Label("Category Section - Guide");
        helpTitle.getStyleClass().add("help-title");

        Label helpText = new Label("""
                In the Categories section, you can organize your transactions by assigning them to categories \
                (e.g., 'Food', 'Rent', 'Entertainment').
                
                You can create new categories or edit existing ones, ensuring that your transactions are properly categorized for better analysis.""");
        helpText.getStyleClass().add("help-text");
        helpText.setWrapText(true);

        // Back Button to return to HelpView
        Button backButton = new Button("Back to Help");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> loadIntoPane());

        helpContent.getChildren().addAll(helpTitle, helpText, backButton);

        // ScrollPane hinzufügen
        ScrollPane scrollPane = new ScrollPane(helpContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);
    }

    /**
     * Zeigt den Hilfetext für den Berichts-Bereich.
     */
    private void showReportsHelp() {
        VBox helpContent = new VBox(20);
        helpContent.setAlignment(Pos.CENTER);
        helpContent.setPadding(new Insets(20));
        helpContent.getStyleClass().add("help-content");

        Label helpTitle = new Label("Reports Section - Guide");
        helpTitle.getStyleClass().add("help-title");

        Label helpText = new Label("""
                In the Reports section, you can view detailed analytics on your spending and income.
                
                You can generate reports to see how your money is being spent, track your progress towards financial goals, \
                and gain insights into your overall financial health.""");
        helpText.getStyleClass().add("help-text");
        helpText.setWrapText(true);

        // Back Button to return to HelpView
        Button backButton = new Button("Back to Help");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> loadIntoPane());

        helpContent.getChildren().addAll(helpTitle, helpText, backButton);

        // ScrollPane hinzufügen
        ScrollPane scrollPane = new ScrollPane(helpContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);
    }
}
