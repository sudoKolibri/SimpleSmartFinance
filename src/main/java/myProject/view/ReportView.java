package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import myProject.controller.ReportController;
import myProject.model.Account;
import myProject.model.Category;
import myProject.util.LoggerUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Die ReportView-Klasse stellt die Benutzeroberfläche für den Finanzberichtsbereich dar.
 * Sie zeigt Diagramme und Informationen über Ausgaben, Einnahmen und Budgets an.
 */
public class ReportView {

    private final String loggedInUserId;
    private final ReportController reportController;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private BarChart<String, Number> transactionBarChart;
    private PieChart categorySpendingChart;
    private VBox dashboardInfo;

    /**
     * Konstruktor für die ReportView.
     * @param reportController Der Controller für Berichtsoperationen.
     * @param loggedInUserId Die ID des angemeldeten Benutzers.
     */
    public ReportView(ReportController reportController, String loggedInUserId) {
        this.reportController = reportController;
        this.loggedInUserId = loggedInUserId;
    }

    /**
     * Lädt die ReportView in das übergeordnete BorderPane.
     * @param parentPane Das übergeordnete BorderPane, in das die Ansicht geladen wird.
     */
    public void loadIntoPane(BorderPane parentPane) {
        VBox mainLayout = new VBox(20);
        mainLayout.getStyleClass().add("main-layout");

        // Header Label
        Label headerLabel = new Label("Financial Reports");
        headerLabel.getStyleClass().add("header-label");

        // Erstellen des Zeitfilter-Containers
        HBox filterBox = createFilterBox();
        filterBox.setMaxWidth(900);

        // Erstellen der Dashboard-Informationen (Total Balance, Accounts, etc.)
        dashboardInfo = new VBox(10);
        dashboardInfo.getStyleClass().add("dashboard-info");
        dashboardInfo.setMaxWidth(900);

        // Erstellen der zwei Diagramme (Tortendiagramm und Balkendiagramm)
        categorySpendingChart = createCategorySpendingChart();
        VBox pieChartBox = new VBox(10, new Label("Expenses by Category"), categorySpendingChart);
        pieChartBox.getStyleClass().add("chart-box");

        transactionBarChart = createTransactionBarChart();
        VBox barChartBox = new VBox(10, new Label("Monthly Income and Expenses"), transactionBarChart);
        barChartBox.getStyleClass().add("chart-box");

        // Horizontales Layout für die zwei Diagramme
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(pieChartBox, barChartBox);

        // Alles in das Hauptlayout einfügen
        mainLayout.getChildren().addAll(headerLabel, dashboardInfo, filterBox, chartsBox);

        // ScrollPane zur Unterstützung von Scrollen bei größerem Inhalt
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        parentPane.setCenter(scrollPane);

        // Initiale Filter anwenden, um die Daten zu laden
        applyFilters();
    }



    /**
     * Erstellt die Filterfunktionen für den Bericht.
     * @return Eine HBox mit Datumswählern und einem Anwenden-Button.
     */
    private HBox createFilterBox() {
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        endDatePicker = new DatePicker(LocalDate.now());

        Button applyFilterButton = new Button("Apply Filter");
        applyFilterButton.getStyleClass().add("apply-button");

        HBox filterBox = new HBox(10, new Label("Start Date:"), startDatePicker, new Label("End Date:"), endDatePicker, applyFilterButton);
        filterBox.getStyleClass().add("filter-box");
        filterBox.setAlignment(Pos.CENTER);

        return filterBox;
    }

    /**
     * Erstellt das Tortendiagramm für Kategorieausgaben.
     * @return Ein PieChart-Objekt für Kategorieausgaben.
     */
    private PieChart createCategorySpendingChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Category Spending");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setStyle("-fx-pie-label-visible: false; -fx-legend-side: right; -fx-legend-item-text-fill: #f8f8f2;");
        return pieChart;
    }

    /**
     * Erstellt das Balkendiagramm für monatliche Einnahmen und Ausgaben.
     * @return Ein BarChart-Objekt für monatliche Transaktionen.
     */
    private BarChart<String, Number> createTransactionBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Income and Expenses");
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount");
        barChart.setLegendSide(Side.RIGHT);
        barChart.getStyleClass().add("custom-bar-chart");
        return barChart;
    }

    /**
     * Wendet die ausgewählten Filter an und aktualisiert die Ansicht.
     */
    private void applyFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        try {
            Map<String, Double> categoryExpenses = reportController.getCategoryExpenses(loggedInUserId, startDate, endDate);
            Map<String, Map<String, Double>> monthlyData = reportController.getMonthlyIncomeAndExpenses(loggedInUserId, startDate, endDate);

            updatePieChart(categoryExpenses);
            updateBarChart(monthlyData);
            updateDashboardInfo(startDate, endDate);
        } catch (Exception e) {
            LoggerUtils.logError(ReportView.class.getName(), "Fehler beim Anwenden der Filter: " + e.getMessage(), e);
            showErrorAlert();
        }
    }

    /**
     * Aktualisiert das Tortendiagramm mit den Kategorieausgaben.
     * @param categoryExpenses Die Ausgaben pro Kategorie.
     */
    private void updatePieChart(Map<String, Double> categoryExpenses) {
        categorySpendingChart.getData().clear();

        if (!categoryExpenses.isEmpty()) {
            categoryExpenses.forEach((category, amount) -> {
                PieChart.Data slice = new PieChart.Data(category, amount);
                categorySpendingChart.getData().add(slice);
            });
        } else {
            categorySpendingChart.getData().add(new PieChart.Data("No Expenses", 1));
        }

        categorySpendingChart.getData().forEach(data -> {
            Tooltip tooltip = new Tooltip(String.format("%s: $%.2f", data.getName(), data.getPieValue()));
            Tooltip.install(data.getNode(), tooltip);
        });
    }

    /**
     * Aktualisiert das Balkendiagramm mit den monatlichen Einnahmen und Ausgaben.
     * @param monthlyData Die monatlichen Einnahmen und Ausgaben.
     */
    @SuppressWarnings("unchecked")

    private void updateBarChart(Map<String, Map<String, Double>> monthlyData) {
        transactionBarChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        monthlyData.get("income").forEach((month, amount) -> incomeSeries.getData().add(new XYChart.Data<>(month, amount)));
        monthlyData.get("expense").forEach((month, amount) -> expenseSeries.getData().add(new XYChart.Data<>(month, amount)));

        transactionBarChart.getData().addAll(incomeSeries, expenseSeries);
    }

    /**
     * Aktualisiert die Dashboard-Informationen.
     * @param startDate Das Startdatum des Berichtszeitraums.
     * @param endDate Das Enddatum des Berichtszeitraums.
     */
    private void updateDashboardInfo(LocalDate startDate, LocalDate endDate) {
        dashboardInfo.getChildren().clear();

        double totalBalance = reportController.getTotalBalance(loggedInUserId);
        Label totalBalanceLabel = new Label(String.format("Total Balance: $%.2f", totalBalance));
        totalBalanceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #50fa7b;");

        List<Account> userAccounts = reportController.getUserAccounts(loggedInUserId);
        VBox accountsInfo = new VBox(5);
        Label accountsLabel = new Label("Accounts:");
        accountsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8f8f2;");
        accountsInfo.getChildren().add(accountsLabel);
        for (Account account : userAccounts) {
            Label accountLabel = new Label(String.format("- %s: $%.2f", account.getName(), account.getBalance()));
            accountLabel.setStyle("-fx-text-fill: #f8f8f2;");
            accountsInfo.getChildren().add(accountLabel);
        }

        Category mostSpentCategory = reportController.getMostSpentCategory(loggedInUserId, startDate, endDate);
        Label mostSpentCategoryLabel = new Label("Most spent category: " + (mostSpentCategory != null ? mostSpentCategory.getName() : "N/A"));
        mostSpentCategoryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        VBox budgetProgressInfo = new VBox(5);
        Label budgetProgressLabel = new Label("Budget Progress:");
        budgetProgressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8f8f2;");
        budgetProgressInfo.getChildren().add(budgetProgressLabel);
        Map<Category, Double> budgetProgress = reportController.getCategoryBudgetProgress(loggedInUserId);
        for (Map.Entry<Category, Double> entry : budgetProgress.entrySet()) {
            ProgressBar progressBar = new ProgressBar(entry.getValue());
            progressBar.setStyle("-fx-accent: " + getProgressBarColor(entry.getValue()));
            progressBar.setPrefWidth(200);
            Label categoryLabel = new Label(entry.getKey().getName() + ": " + String.format("%.0f%%", entry.getValue() * 100));
            categoryLabel.setStyle("-fx-text-fill: #f8f8f2;");
            budgetProgressInfo.getChildren().addAll(categoryLabel, progressBar);
        }

        dashboardInfo.getChildren().addAll(totalBalanceLabel, accountsInfo, mostSpentCategoryLabel, budgetProgressInfo);
    }

    /**
     * Bestimmt die Farbe für den Fortschrittsbalken basierend auf dem Fortschritt.
     * @param progress Der Fortschrittswert zwischen 0 und 1.
     * @return Der Farbcode für den Fortschrittsbalken.
     */
    private String getProgressBarColor(double progress) {
        if (progress < 0.5) return "#50fa7b";
        else if (progress < 0.75) return "#f1fa8c";
        else if (progress < 0.9) return "#ffb86c";
        else return "#ff5555";
    }

    /**
     * Zeigt eine Fehlermeldung an.
     */
    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("An error occurred while applying filters. Please try again.");
        alert.showAndWait();
    }
}