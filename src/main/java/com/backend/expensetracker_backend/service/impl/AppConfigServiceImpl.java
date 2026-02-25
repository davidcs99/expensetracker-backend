package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    private static final int FREE_MONTHLY_LIMIT = 50;
    private static final int PREMIUM_MONTHLY_LIMIT = Integer.MAX_VALUE;
    private static final double ALERT_THRESHOLD_PERCENTAGE = 80.0;

    private final long instanceCreationTime;

    public AppConfigServiceImpl() {
        this.instanceCreationTime = System.currentTimeMillis();
    }

    @Override
    public int getFreeAccountMonthlyLimit() {
        return FREE_MONTHLY_LIMIT;
    }

    @Override
    public int getPremiumAccountMonthlyLimit() {
        return PREMIUM_MONTHLY_LIMIT;
    }

    @Override
    public boolean canCreateMoreExpenses(int currentExpenseCount, boolean isPremium) {
        if (isPremium) {
            return true;
        }

        boolean canCreate = currentExpenseCount < FREE_MONTHLY_LIMIT;

        if (!canCreate) {
            log.warn("⚠️ Usuario FREE alcanzó el límite: {}/{}",
                    currentExpenseCount, FREE_MONTHLY_LIMIT);
        }

        return canCreate;
    }

    @Override
    public double getUsagePercentage(int currentExpenseCount, boolean isPremium) {
        if (isPremium) {
            return 0.0;
        }

        double percentage = (currentExpenseCount * 100.0) / FREE_MONTHLY_LIMIT;

        if (percentage >= ALERT_THRESHOLD_PERCENTAGE && percentage < 100) {
            log.info("📊 Usuario cerca del límite: {}%",
                    String.format("%.1f", percentage));
        }

        return percentage;
    }

    public long getInstanceCreationTime() {
        return instanceCreationTime;
    }
}