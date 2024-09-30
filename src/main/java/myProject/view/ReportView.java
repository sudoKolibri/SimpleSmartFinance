package myProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import myProject.controller.ReportController;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.util.List;

public class ReportView {

    private BorderPane root; // Root layout for the report section
    private final ReportController reportController;

    // Constructor with ReportController injection
    public ReportView(ReportController reportController) {
        this.reportController = reportController;
        initializeView();
    }

    private void initializeView() {
        // Set up the main layout as a VBox
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Reports Overview section
        Label overviewLabel = new Label("Reports Overview");
        overviewLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Initialize charts
        PieChart categorySpendingChart = createCategorySpendingChart();
        BarChart<String, Number> budgetAnalysisChart = createBudgetAnalysisChart();
        BarChart<String, Number> incomeVsExpenseChart = createIncomeVsExpenseChart();
        LineChart<String, Number> balanceOverTimeChart = createBalanceOverTimeChart();

        // Navigation Buttons
        HBox navigationButtons = new HBox(10);
        navigationButtons.setAlignment(Pos.CENTER);
        Button backButton = new Button("Back");
        Button detailedReportButton = new Button("Detailed Report");
        navigationButtons.getChildren().addAll(backButton, detailedReportButton);

        // Layout for the charts section
        GridPane chartsLayout = new GridPane();
        chartsLayout.setAlignment(Pos.CENTER);
        chartsLayout.setHgap(20);
        chartsLayout.setVgap(20);
        chartsLayout.setPadding(new Insets(10));
        chartsLayout.add(categorySpendingChart, 0, 0);
        chartsLayout.add(budgetAnalysisChart, 1, 0);
        chartsLayout.add(incomeVsExpenseChart, 0, 1);
        chartsLayout.add(balanceOverTimeChart, 1, 1);

        // Add all components to the main layout
        mainLayout.getChildren().addAll(overviewLabel, chartsLayout, navigationButtons);

        // Set the main layout as the root
        this.root = new BorderPane();
        root.setCenter(mainLayout);
    }

    // Method to load the ReportView into a given BorderPane
    public void loadIntoPane(BorderPane parentPane) {
        parentPane.setCenter(root);
    }

    // Create Pie Chart for Category Spending
    private PieChart createCategorySpendingChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Category Spending Analysis");

        // Fetch data from ReportController
        reportController.getCategorySpendingData("userId").forEach((category, spending) -> {
            PieChart.Data slice = new PieChart.Data(category, spending);
            pieChart.getData().add(slice);
        });

        return pieChart;
    }

    // Create Bar Chart for Income vs. Expense Analysis
    private BarChart<String, Number> createIncomeVsExpenseChart() {
        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Income vs. Expense");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        reportController.getIncomeVsExpenseData("userId").forEach((type, amount) -> {
            XYChart.Data<String, Number> bar = new XYChart.Data<>(type, amount);
            series.getData().add(bar);
        });

        barChart.getData().add(series);
        return barChart;
    }

    // Create Line Chart for Balance Over Time Analysis
    private LineChart<String, Number> createBalanceOverTimeChart() {
        LineChart<String, Number> lineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        lineChart.setTitle("Balance Over Time");
        lineChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Double> balanceData = reportController.getBalanceOverTimeData("userId");
        for (int i = 0; i < balanceData.size(); i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), balanceData.get(i)));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    // Create Bar Chart for Budget Analysis
    private BarChart<String, Number> createBudgetAnalysisChart() {
        // Create the axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Budget Difference");

        // Create the BarChart object
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Budget Analysis");
        barChart.setLegendVisible(false);

        // Create the data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Fetch data from the ReportController and add to the series
        reportController.getBudgetAnalysisData("userId").forEach((category, difference) -> {
            XYChart.Data<String, Number> bar = new XYChart.Data<>(category, difference);
            series.getData().add(bar);
        });

        // Add the series to the chart
        barChart.getData().add(series);
        return barChart;
    }

}
