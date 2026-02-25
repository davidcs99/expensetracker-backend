package com.backend.expensetracker_backend.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitAlertEvent {

    private Long userId;
    private String userEmail;
    private int currentExpenseCount;
    private int monthlyLimit;
    private double usagePercentage;
    private LocalDateTime timestamp;

    public LimitAlertEvent(Long userId, String userEmail, int currentExpenseCount,
                           int monthlyLimit, double usagePercentage) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.currentExpenseCount = currentExpenseCount;
        this.monthlyLimit = monthlyLimit;
        this.usagePercentage = usagePercentage;
        this.timestamp = LocalDateTime.now();
    }
}