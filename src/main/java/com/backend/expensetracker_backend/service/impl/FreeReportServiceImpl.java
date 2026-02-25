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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("freeReportService")
@RequiredArgsConstructor
@Slf4j
public class FreeReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateReport(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Generando reporte FREE para userId={}", userId);

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);

        ReportDTO report = new ReportDTO();
        report.setReportType("FREE");
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

        report.setAverageExpensePerDay(null);
        report.setMostExpensiveCategory(null);
        report.setMostExpensiveCategoryAmount(null);
        report.setMonthlyBreakdown(null);
        report.setExpenseCountByCategory(null);

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