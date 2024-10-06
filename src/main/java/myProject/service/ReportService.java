package myProject.service;

import myProject.model.Account;
import myProject.model.Category;
import myProject.model.Transaction;
import myProject.util.LoggerUtils;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Der ReportService ist verantwortlich für die Erstellung von Finanzberichten.
 * Er verwendet TransactionService, AccountService und CategoryService, um die erforderlichen Daten zu sammeln und zu verarbeiten.
 */
public class ReportService {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    /**
     * Konstruktor für den ReportService.
     * @param transactionService Service für Transaktionsoperationen.
     * @param accountService Service für Kontooperationen.
     * @param categoryService Service für Kategorieoperationen.
     */
    public ReportService(TransactionService transactionService, AccountService accountService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    /**
     * Berechnet die Ausgaben pro Kategorie für einen bestimmten Zeitraum.
     * @param userId ID des Benutzers.
     * @param startDate Startdatum des Zeitraums.
     * @param endDate Enddatum des Zeitraums.
     * @return Map mit Kategorienamen als Schlüssel und Ausgabensummen als Werte.
     */
    public Map<String, Double> getCategoryExpenses(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByUserAndPeriod(userId, startDate, endDate);
            return transactions.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                    .collect(Collectors.groupingBy(
                            t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                            Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                    ));
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Berechnen der Kategorieausgaben: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Berechnet die monatlichen Einnahmen und Ausgaben für einen bestimmten Zeitraum.
     * @param userId ID des Benutzers.
     * @param startDate Startdatum des Zeitraums.
     * @param endDate Enddatum des Zeitraums.
     * @return Map mit "income" und "expense" als Schlüssel, die jeweils auf Maps mit Monaten und Summen verweisen.
     */
    public Map<String, Map<String, Double>> getMonthlyIncomeAndExpenses(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByUserAndPeriod(userId, startDate, endDate);

            Map<String, Map<String, Double>> result = new HashMap<>();
            result.put("income", new TreeMap<>());  // Verwenden Sie TreeMap für sortierte Schlüssel
            result.put("expense", new TreeMap<>());

            transactions.forEach(t -> {
                // Exkludiere Transaktionen mit der Beschreibung "Initial Balance"
                if (t.getCategory() == null
                        || "No Category".equalsIgnoreCase(t.getCategory().getName())
                        || "Initial Balance".equalsIgnoreCase(t.getDescription())) {
                    return;
                }

                String month = String.format("%d-%02d", t.getDate().toLocalDate().getYear(), t.getDate().toLocalDate().getMonthValue());
                String type = t.getType().toLowerCase();
                double amount = "expense".equalsIgnoreCase(type) ? Math.abs(t.getAmount()) : t.getAmount();

                LoggerUtils.logInfo(ReportService.class.getName(), "Processing Transaction - Month: " + month + ", Type: " + type + ", Amount: " + amount);

                if (result.containsKey(type)) {
                    result.get(type).merge(month, amount, Double::sum);
                } else {
                    LoggerUtils.logError(ReportService.class.getName(), "Unknown transaction type: " + type, null);
                }
            });

            return result;
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Berechnen der monatlichen Einnahmen und Ausgaben: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }



    /**
     * Berechnet den Gesamtkontostand eines Benutzers.
     * @param userId ID des Benutzers.
     * @return Gesamtkontostand des Benutzers.
     */
    public double getTotalBalance(String userId) {
        try {
            return accountService.calculateOverallBalanceForUser(userId);
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Berechnen des Gesamtkontostands: " + e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * Ruft alle Konten eines Benutzers ab.
     * @param userId ID des Benutzers.
     * @return Liste aller Konten des Benutzers.
     */
    public List<Account> getUserAccounts(String userId) {
        try {
            return accountService.getAllAccountsForUser(userId);
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Abrufen der Benutzerkonten: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ermittelt die Kategorie mit den höchsten Ausgaben für einen bestimmten Zeitraum.
     * @param userId ID des Benutzers.
     * @param startDate Startdatum des Zeitraums.
     * @param endDate Enddatum des Zeitraums.
     * @return Kategorie mit den höchsten Ausgaben oder null, wenn keine gefunden wurde.
     */
    public Category getMostSpentCategory(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Double> categoryExpenses = getCategoryExpenses(userId, startDate, endDate);
            return categoryExpenses.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> categoryService.getCategoryByName(userId, entry.getKey()))
                    .orElse(null);
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Ermitteln der Kategorie mit den höchsten Ausgaben: " + e.getMessage(), e);
            return null;
        }
    }


    /**
     * Berechnet den Budgetfortschritt für alle Kategorien eines Benutzers.
     * @param userId ID des Benutzers.
     * @param startDate Startdatum des Zeitraums.
     * @param endDate Enddatum des Zeitraums.
     * @return Map mit Kategorien als Schlüssel und ihrem Budgetfortschritt als Werte.
     */
    public Map<Category, Double> getCategoryBudgetProgress(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Category> categories = categoryService.getAllCategoriesForUser(userId);
            Map<Category, Double> budgetProgress = new HashMap<>();

            for (Category category : categories) {
                if (category.getBudget() != null && category.getBudget() > 0) {
                    double spent = Math.abs(getCategoryExpenses(userId, startDate, endDate).getOrDefault(category.getName(), 0.0));
                    double progress = spent / category.getBudget();
                    budgetProgress.put(category, progress);
                }
            }

            return budgetProgress;
        } catch (Exception e) {
            LoggerUtils.logError(ReportService.class.getName(), "Fehler beim Berechnen des Kategorie-Budgetfortschritts: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
