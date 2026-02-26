package com.backend.expensetracker_backend.controller;

import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.repository.ExpenseRepository;
import com.backend.expensetracker_backend.service.UserSyncService;
import com.backend.expensetracker_backend.service.impl.AppConfigServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private static final String UNLIMITED = "UNLIMITED";
    private final AppConfigServiceImpl appConfigService;
    private final UserSyncService userSyncService;
    private final ExpenseRepository expenseRepository;

    @GetMapping("/singleton-proof")
    public ResponseEntity<Map<String, Object>> getSingletonProof() {
        long instanceTime = appConfigService.getInstanceCreationTime();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("instanceId", instanceTime);
        response.put("message", "Si este número NO cambia entre llamadas, es un Singleton ✓");
        response.put("currentTime", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimitsAndUsage() {
        User currentUser = userSyncService.getCurrentUser();
        boolean isPremium = currentUser.getSubscription() == SubscriptionType.PREMIUM;

        Long currentMonthCount = expenseRepository.countCurrentMonthExpensesByUserId(currentUser.getId());

        long instanceTime = appConfigService.getInstanceCreationTime();
        LocalDateTime instanceDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(instanceTime),
                ZoneId.systemDefault()
        );

        int limit = isPremium ?
                appConfigService.getPremiumAccountMonthlyLimit() :
                appConfigService.getFreeAccountMonthlyLimit();

        double usagePercentage = appConfigService.getUsagePercentage(
                currentMonthCount.intValue(),
                isPremium
        );

        boolean canCreateMore = appConfigService.canCreateMoreExpenses(
                currentMonthCount.intValue(),
                isPremium
        );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("userId", currentUser.getId());
        response.put("userEmail", currentUser.getEmail());
        response.put("subscriptionType", currentUser.getSubscription().name());
        response.put("monthlyLimit", isPremium ? UNLIMITED : limit);
        response.put("currentUsage", currentMonthCount);
        response.put("remainingExpenses", isPremium ? UNLIMITED : (limit - currentMonthCount.intValue()));
        response.put("usagePercentage", String.format("%.2f%%", usagePercentage));
        response.put("canCreateMore", canCreateMore);
        response.put("singletonInstanceCreatedAt", instanceTime);
        response.put("singletonInstanceDateTime", instanceDateTime.toString());

        if (!isPremium && usagePercentage >= 80) {
            response.put("alert", String.format(
                    "⚠️ Has usado %.1f%% de tu límite mensual. Considera actualizar a PREMIUM.",
                    usagePercentage
            ));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("freeLimitMonthly", appConfigService.getFreeAccountMonthlyLimit());
        response.put("premiumLimitMonthly", UNLIMITED);
        response.put("singletonInstance", appConfigService.getInstanceCreationTime());

        return ResponseEntity.ok(response);
    }
}