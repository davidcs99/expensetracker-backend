package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.dto.ReportDTO;
import com.backend.expensetracker_backend.entity.Expense;
import com.backend.expensetracker_backend.repository.ExpenseRepository;
import com.backend.expensetracker_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service("premiumReportService")
@RequiredArgsConstructor
@Slf4j
public class PremiumReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateReport(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Generando reporte PREMIUM para userId={}", userId);

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);

        ReportDTO report = new ReportDTO();
        report.setReportType("PREMIUM");
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setTotalAmount(total);
        report.setTotalExpenses(expenses.size());

        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        report.setExpensesByCategory(byCategory);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal avgPerDay = total.divide(
                BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP
        );
        report.setAverageExpensePerDay(avgPerDay);

        Optional<Map.Entry<String, BigDecimal>> mostExpensive = byCategory.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        mostExpensive.ifPresent(entry -> {
            report.setMostExpensiveCategory(entry.getKey());
            report.setMostExpensiveCategoryAmount(entry.getValue());
        });

        Map<YearMonth, List<Expense>> byMonth = expenses.stream()
                .collect(Collectors.groupingBy(e -> YearMonth.from(e.getDate())));

        List<ReportDTO.MonthlyExpense> monthlyBreakdown = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    YearMonth month = entry.getKey();
                    List<Expense> monthExpenses = entry.getValue();

                    BigDecimal monthTotal = monthExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String monthName = month.getMonth()
                            .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
                            + " " + month.getYear();

                    return new ReportDTO.MonthlyExpense(monthName, monthTotal, monthExpenses.size());
                })
                .collect(Collectors.toList());

        report.setMonthlyBreakdown(monthlyBreakdown);

        Map<String, Integer> countByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.summingInt(e -> 1)
                ));

        report.setExpenseCountByCategory(countByCategory);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateCurrentMonthReport(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return generateReport(userId, startDate, endDate);
    }
}