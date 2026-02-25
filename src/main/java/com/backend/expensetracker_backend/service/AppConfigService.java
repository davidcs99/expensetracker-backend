package com.backend.expensetracker_backend.service;

public interface AppConfigService {

    int getFreeAccountMonthlyLimit();

    int getPremiumAccountMonthlyLimit();

    boolean canCreateMoreExpenses(int currentExpenseCount, boolean isPremium);

    double getUsagePercentage(int currentExpenseCount, boolean isPremium);
}