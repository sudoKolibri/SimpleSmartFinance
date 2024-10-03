package myProject.controller;

import myProject.model.Account;
import myProject.model.Category;
import myProject.service.ReportService;
import myProject.util.LoggerUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Der ReportController verwaltet die Logik für die Erstellung von Finanzberichten.
 * Er dient als Verbindung zwischen der Benutzeroberfläche und dem ReportService.
 */
public class ReportController {

    private final ReportService reportService;

    /**
     * Konstruktor für den ReportController.
     * @param reportService Der zu verwendende ReportService.
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Ruft die Ausgaben pro Kategorie für einen bestimmten Zeitraum ab.
     * @param userId Die ID des Benutzers.
     * @param startDate Das Startdatum des Zeitraums.
     * @param endDate Das Enddatum des Zeitraums.
     * @return Eine Map mit den Ausgaben pro Kategorie.
     */
    public Map<String, Double> getCategoryExpenses(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            return reportService.getCategoryExpenses(userId, startDate, endDate);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Abrufen der Kategorieausgaben: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Ruft die monatlichen Einnahmen und Ausgaben für einen bestimmten Zeitraum ab.
     * @param userId Die ID des Benutzers.
     * @param startDate Das Startdatum des Zeitraums.
     * @param endDate Das Enddatum des Zeitraums.
     * @return Eine Map mit den monatlichen Einnahmen und Ausgaben.
     */
    public Map<String, Map<String, Double>> getMonthlyIncomeAndExpenses(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            return reportService.getMonthlyIncomeAndExpenses(userId, startDate, endDate);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Abrufen der monatlichen Einnahmen und Ausgaben: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Ruft den Gesamtkontostand des Benutzers ab.
     * @param userId Die ID des Benutzers.
     * @return Der Gesamtkontostand.
     */
    public double getTotalBalance(String userId) {
        try {
            return reportService.getTotalBalance(userId);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Abrufen des Gesamtkontostands: " + e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * Ruft alle Konten des Benutzers ab.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste aller Konten des Benutzers.
     */
    public List<Account> getUserAccounts(String userId) {
        try {
            return reportService.getUserAccounts(userId);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Abrufen der Benutzerkonten: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Ermittelt die Kategorie mit den höchsten Ausgaben für einen bestimmten Zeitraum.
     * @param userId Die ID des Benutzers.
     * @param startDate Das Startdatum des Zeitraums.
     * @param endDate Das Enddatum des Zeitraums.
     * @return Die Kategorie mit den höchsten Ausgaben.
     */
    public Category getMostSpentCategory(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            return reportService.getMostSpentCategory(userId, startDate, endDate);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Ermitteln der Kategorie mit den höchsten Ausgaben: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Ruft den Budgetfortschritt für alle Kategorien des Benutzers ab.
     * @param userId Die ID des Benutzers.
     * @return Eine Map mit dem Budgetfortschritt pro Kategorie.
     */
    public Map<Category, Double> getCategoryBudgetProgress(String userId) {
        try {
            return reportService.getCategoryBudgetProgress(userId);
        } catch (Exception e) {
            LoggerUtils.logError(ReportController.class.getName(), "Fehler beim Abrufen des Kategorie-Budgetfortschritts: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }
}