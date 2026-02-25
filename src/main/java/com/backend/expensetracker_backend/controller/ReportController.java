package com.backend.expensetracker_backend.controller;

import com.backend.expensetracker_backend.dto.ReportDTO;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.service.ReportService;
import com.backend.expensetracker_backend.service.UserSyncService;
import com.backend.expensetracker_backend.service.factory.ReportServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceFactory reportServiceFactory;
    private final UserSyncService userSyncService;

    @GetMapping("/current-month")
    public ResponseEntity<ReportDTO> getCurrentMonthReport() {
        User currentUser = userSyncService.getCurrentUser();

        ReportService reportService = reportServiceFactory
                .getReportService(currentUser.getSubscription());

        ReportDTO report = reportService.generateCurrentMonthReport(currentUser.getId());

        return ResponseEntity.ok(report);
    }

    @GetMapping("/custom")
    public ResponseEntity<ReportDTO> getCustomReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User currentUser = userSyncService.getCurrentUser();

        ReportService reportService = reportServiceFactory
                .getReportService(currentUser.getSubscription());

        ReportDTO report = reportService.generateReport(currentUser.getId(), startDate, endDate);

        return ResponseEntity.ok(report);
    }
}