package com.backend.expensetracker_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Integer totalExpenses;
    private Map<String, BigDecimal> expensesByCategory;

    private BigDecimal averageExpensePerDay;
    private String mostExpensiveCategory;
    private BigDecimal mostExpensiveCategoryAmount;
    private List<MonthlyExpense> monthlyBreakdown;
    private Map<String, Integer> expenseCountByCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyExpense {
        private String month;
        private BigDecimal total;
        private Integer count;
    }
}