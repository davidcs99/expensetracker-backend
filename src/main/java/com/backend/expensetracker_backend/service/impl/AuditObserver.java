package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.event.LimitAlertEvent;
import com.backend.expensetracker_backend.service.EventObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditObserver implements EventObserver {

    @Override
    public void onLimitAlert(LimitAlertEvent event) {
        log.info("📋 AUDITORÍA: Registrando evento de alerta de límite");
        log.info("   Timestamp: {}", event.getTimestamp());
        log.info("   UserId: {}", event.getUserId());
        log.info("   Email: {}", event.getUserEmail());
        log.info("   Gastos: {}/{}", event.getCurrentExpenseCount(), event.getMonthlyLimit());
        log.info("   Porcentaje: {}%", String.format("%.2f", event.getUsagePercentage()));
        log.info("   Registro guardado en sistema de auditoría.");
    }
}