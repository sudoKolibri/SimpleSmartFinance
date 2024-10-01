package myProject.view.detail;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import myProject.controller.ReportController;
import myProject.model.Transaction;

import java.util.List;

public class ReportDetailView {

    private BorderPane root;
    private final ReportController reportController;
    private final String userId;

    // Constructor with ReportController injection
    public ReportDetailView(ReportController reportController, String userId) {
        this.reportController = reportController;
        this.userId = userId;
        initializeView();
    }

    private void initializeView() {
        // Set up main layout as a VBox
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Add a label to show the title
        Label titleLabel = new Label("Detailed Report");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Placeholder for detailed transaction list
        ListView<String> transactionListView = new ListView<>();
        transactionListView.setPlaceholder(new Label("Select a category to view detailed transactions."));

        // Navigation Button
        Button backButton = new Button("Back");

        // Add components to main layout
        mainLayout.getChildren().addAll(titleLabel, transactionListView, backButton);

        // Set layout to root
        this.root = new BorderPane();
        root.setCenter(mainLayout);

        // Load data for a specific category when initialized (for demonstration purposes)
        loadDetailedReportData("Food", transactionListView);
    }

    // Load the detailed report data into the ListView
    private void loadDetailedReportData(String category, ListView<String> transactionListView) {
        List<Transaction> transactions = reportController.getDetailedReportData(userId, category);
        transactions.forEach(transaction -> transactionListView.getItems().add(transaction.getDescription()));
    }

    // Method to load the ReportDetailView into a given BorderPane
    public void loadIntoPane(BorderPane parentPane) {
        parentPane.setCenter(root);
    }
}
