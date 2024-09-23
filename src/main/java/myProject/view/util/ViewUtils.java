package myProject.view.util;

import javafx.scene.control.TextField;

public class ViewUtils {

    // Determine the progress bar color based on the spend/budget ratio
    public static String getProgressBarColor(double spent, double budget) {
        // Prevent division by zero or invalid ratios
        if (budget <= 0 || Double.isNaN(spent) || Double.isNaN(budget) || Double.isInfinite(spent) || Double.isInfinite(budget)) {
            return "#50fa7b";  // Default color for invalid cases
        }

        double ratio = spent / budget;

        // Clamp the ratio between 0 and 1 if necessary
        ratio = Math.max(0, Math.min(1, ratio));

        if (ratio < 0.5) {
            return "#50fa7b";  // Green for healthy budget (under 50%)
        } else if (ratio < 0.75) {
            return "#f1fa8c";  // Yellow for caution (50% to 75%)
        } else if (ratio < 0.9) {
            return "#ffb86c";  // Orange for warning (75% to 90%)
        } else {
            return "#ff5555";  // Red for danger (90% to 100%)
        }
    }

    // Helper for optional budget (category)
    public static Double getCategoryBudget(TextField budgetField) {
        Double categoryBudget = null; // Default value is null (no budget)
        if (!budgetField.getText().isEmpty()) {
            try {
                categoryBudget = Double.parseDouble(budgetField.getText());
            } catch (NumberFormatException ex) {
                categoryBudget = null;  // If invalid input, treat as no budget
            }
        }
        return categoryBudget;
    }

    // Validate if a TextField has a numeric value
    public static boolean isNumeric(TextField textField) {
        try {
            Double.parseDouble(textField.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Format a double value as a currency string
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
}
