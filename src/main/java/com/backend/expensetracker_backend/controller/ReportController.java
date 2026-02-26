package com.backend.expensetracker_backend.controller;

import com.backend.expensetracker_backend.entity.Expense;
import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.repository.ExpenseRepository;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserSyncService userSyncService;
    private final ExpenseRepository expenseRepository;

    @GetMapping("/current-month")
    public ResponseEntity<Map<String, Object>> getCurrentMonthReport() {
        User currentUser = userSyncService.getCurrentUser();
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(currentUser.getId(), startDate, endDate);

        Map<String, Object> report = new LinkedHashMap<>();

        // CÓDIGO DUPLICADO Y COMPLEJO - if/else gigante
        if (currentUser.getSubscription() == SubscriptionType.FREE) {
            report.put("reportType", "FREE");
            report.put("startDate", startDate);
            report.put("endDate", endDate);

            // Cálculo de total - código duplicado
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                total = total.add(expense.getAmount());
            }
            report.put("totalAmount", total);
            report.put("totalExpenses", expenses.size());

            // Por categoría - código duplicado
            Map<String, BigDecimal> byCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (byCategory.containsKey(categoryName)) {
                    BigDecimal current = byCategory.get(categoryName);
                    byCategory.put(categoryName, current.add(expense.getAmount()));
                } else {
                    byCategory.put(categoryName, expense.getAmount());
                }
            }
            report.put("expensesByCategory", byCategory);

            report.put("averageExpensePerDay", null);
            report.put("mostExpensiveCategory", null);
            report.put("mostExpensiveCategoryAmount", null);
            report.put("monthlyBreakdown", null);
            report.put("expenseCountByCategory", null);

        } else if (currentUser.getSubscription() == SubscriptionType.PREMIUM) {
            report.put("reportType", "PREMIUM");
            report.put("startDate", startDate);
            report.put("endDate", endDate);

            // DUPLICACIÓN MASIVA - mismo cálculo de total
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                total = total.add(expense.getAmount());
            }
            report.put("totalAmount", total);
            report.put("totalExpenses", expenses.size());

            // DUPLICACIÓN MASIVA - mismo código de categorías
            Map<String, BigDecimal> byCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (byCategory.containsKey(categoryName)) {
                    BigDecimal current = byCategory.get(categoryName);
                    byCategory.put(categoryName, current.add(expense.getAmount()));
                } else {
                    byCategory.put(categoryName, expense.getAmount());
                }
            }
            report.put("expensesByCategory", byCategory);

            // Promedio por día
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            BigDecimal avgPerDay = total.divide(
                    BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP
            );
            report.put("averageExpensePerDay", avgPerDay);

            // Categoría más costosa - código ineficiente
            String mostExpensiveCat = null;
            BigDecimal maxAmount = BigDecimal.ZERO;
            Iterator<Map.Entry<String, BigDecimal>> iterator = byCategory.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, BigDecimal> entry = iterator.next();
                if (entry.getValue().compareTo(maxAmount) > 0) {
                    maxAmount = entry.getValue();
                    mostExpensiveCat = entry.getKey();
                }
            }
            report.put("mostExpensiveCategory", mostExpensiveCat);
            report.put("mostExpensiveCategoryAmount", maxAmount);

            // Desglose mensual - código complejo y duplicado
            Map<YearMonth, List<Expense>> byMonth = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                YearMonth month = YearMonth.from(expense.getDate());
                if (byMonth.containsKey(month)) {
                    byMonth.get(month).add(expense);
                } else {
                    List<Expense> list = new ArrayList<>();
                    list.add(expense);
                    byMonth.put(month, list);
                }
            }

            List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
            Iterator<Map.Entry<YearMonth, List<Expense>>> monthIterator = byMonth.entrySet().iterator();
            while (monthIterator.hasNext()) {
                Map.Entry<YearMonth, List<Expense>> entry = monthIterator.next();
                Map<String, Object> monthData = new HashMap<>();
                YearMonth month = entry.getKey();
                List<Expense> monthExpenses = entry.getValue();

                BigDecimal monthTotal = BigDecimal.ZERO;
                for (int i = 0; i < monthExpenses.size(); i++) {
                    Expense exp = monthExpenses.get(i);
                    monthTotal = monthTotal.add(exp.getAmount());
                }

                String monthName = month.getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
                        + " " + month.getYear();

                monthData.put("month", monthName);
                monthData.put("total", monthTotal);
                monthData.put("count", monthExpenses.size());
                monthlyBreakdown.add(monthData);
            }
            report.put("monthlyBreakdown", monthlyBreakdown);

            // Conteo por categoría - más código duplicado
            Map<String, Integer> countByCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (countByCategory.containsKey(categoryName)) {
                    Integer count = countByCategory.get(categoryName);
                    countByCategory.put(categoryName, count + 1);
                } else {
                    countByCategory.put(categoryName, 1);
                }
            }
            report.put("expenseCountByCategory", countByCategory);
        }

        return ResponseEntity.ok(report);
    }

    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User currentUser = userSyncService.getCurrentUser();
        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(currentUser.getId(), startDate, endDate);

        Map<String, Object> report = new LinkedHashMap<>();

        // DUPLICACIÓN EXTREMA - TODO EL CÓDIGO REPETIDO DE NUEVO
        if (currentUser.getSubscription() == SubscriptionType.FREE) {
            report.put("reportType", "FREE");
            report.put("startDate", startDate);
            report.put("endDate", endDate);

            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                total = total.add(expense.getAmount());
            }
            report.put("totalAmount", total);
            report.put("totalExpenses", expenses.size());

            Map<String, BigDecimal> byCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (byCategory.containsKey(categoryName)) {
                    BigDecimal current = byCategory.get(categoryName);
                    byCategory.put(categoryName, current.add(expense.getAmount()));
                } else {
                    byCategory.put(categoryName, expense.getAmount());
                }
            }
            report.put("expensesByCategory", byCategory);

            report.put("averageExpensePerDay", null);
            report.put("mostExpensiveCategory", null);
            report.put("mostExpensiveCategoryAmount", null);
            report.put("monthlyBreakdown", null);
            report.put("expenseCountByCategory", null);

        } else if (currentUser.getSubscription() == SubscriptionType.PREMIUM) {
            report.put("reportType", "PREMIUM");
            report.put("startDate", startDate);
            report.put("endDate", endDate);

            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                total = total.add(expense.getAmount());
            }
            report.put("totalAmount", total);
            report.put("totalExpenses", expenses.size());

            Map<String, BigDecimal> byCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (byCategory.containsKey(categoryName)) {
                    BigDecimal current = byCategory.get(categoryName);
                    byCategory.put(categoryName, current.add(expense.getAmount()));
                } else {
                    byCategory.put(categoryName, expense.getAmount());
                }
            }
            report.put("expensesByCategory", byCategory);

            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            BigDecimal avgPerDay = total.divide(
                    BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP
            );
            report.put("averageExpensePerDay", avgPerDay);

            String mostExpensiveCat = null;
            BigDecimal maxAmount = BigDecimal.ZERO;
            Iterator<Map.Entry<String, BigDecimal>> iterator = byCategory.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, BigDecimal> entry = iterator.next();
                if (entry.getValue().compareTo(maxAmount) > 0) {
                    maxAmount = entry.getValue();
                    mostExpensiveCat = entry.getKey();
                }
            }
            report.put("mostExpensiveCategory", mostExpensiveCat);
            report.put("mostExpensiveCategoryAmount", maxAmount);

            Map<YearMonth, List<Expense>> byMonth = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                YearMonth month = YearMonth.from(expense.getDate());
                if (byMonth.containsKey(month)) {
                    byMonth.get(month).add(expense);
                } else {
                    List<Expense> list = new ArrayList<>();
                    list.add(expense);
                    byMonth.put(month, list);
                }
            }

            List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
            Iterator<Map.Entry<YearMonth, List<Expense>>> monthIterator = byMonth.entrySet().iterator();
            while (monthIterator.hasNext()) {
                Map.Entry<YearMonth, List<Expense>> entry = monthIterator.next();
                Map<String, Object> monthData = new HashMap<>();
                YearMonth month = entry.getKey();
                List<Expense> monthExpenses = entry.getValue();

                BigDecimal monthTotal = BigDecimal.ZERO;
                for (int i = 0; i < monthExpenses.size(); i++) {
                    Expense exp = monthExpenses.get(i);
                    monthTotal = monthTotal.add(exp.getAmount());
                }

                String monthName = month.getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
                        + " " + month.getYear();

                monthData.put("month", monthName);
                monthData.put("total", monthTotal);
                monthData.put("count", monthExpenses.size());
                monthlyBreakdown.add(monthData);
            }
            report.put("monthlyBreakdown", monthlyBreakdown);

            Map<String, Integer> countByCategory = new HashMap<>();
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String categoryName = expense.getCategory().getName();
                if (countByCategory.containsKey(categoryName)) {
                    Integer count = countByCategory.get(categoryName);
                    countByCategory.put(categoryName, count + 1);
                } else {
                    countByCategory.put(categoryName, 1);
                }
            }
            report.put("expenseCountByCategory", countByCategory);
        }

        return ResponseEntity.ok(report);
    }
}