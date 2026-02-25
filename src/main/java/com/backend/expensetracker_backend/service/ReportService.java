package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.dto.ReportDTO;

import java.time.LocalDate;

public interface ReportService {

    ReportDTO generateReport(Long userId, LocalDate startDate, LocalDate endDate);

    ReportDTO generateCurrentMonthReport(Long userId);
}