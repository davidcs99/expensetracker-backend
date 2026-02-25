package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.event.LimitAlertEvent;
import com.backend.expensetracker_backend.service.EventObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationObserver implements EventObserver {

    @Override
    public void onLimitAlert(LimitAlertEvent event) {
        log.warn("╔═══════════════════════════════════════════════════════════╗");
        log.warn("║  🔔 ALERTA DE LÍMITE - NOTIFICACIÓN                      ║");
        log.warn("╠═══════════════════════════════════════════════════════════╣");
        log.warn("║  Usuario: {}                   ║", padRight(event.getUserEmail(), 38));
        log.warn("║  Gastos actuales: {}/{}                                  ║",
                event.getCurrentExpenseCount(), event.getMonthlyLimit());
        log.warn("║  Porcentaje de uso: {}%                                ║",
                String.format("%.1f", event.getUsagePercentage()));
        log.warn("╠═══════════════════════════════════════════════════════════╣");
        log.warn("║  ACCIÓN: Enviando notificación por email...              ║");
        log.warn("╚═══════════════════════════════════════════════════════════╝");

        sendEmailNotification(event);
    }

    private void sendEmailNotification(LimitAlertEvent event) {
        log.info("📧 Email enviado a: {}", event.getUserEmail());
        log.info("   Asunto: Alerta - Te estás acercando al límite mensual");
        log.info("   Mensaje: Has usado {}% de tu límite de {} gastos mensuales.",
                String.format("%.0f", event.getUsagePercentage()),
                event.getMonthlyLimit());
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}