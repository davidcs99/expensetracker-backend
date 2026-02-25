package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.event.LimitAlertEvent;
import com.backend.expensetracker_backend.service.EventObserver;
import com.backend.expensetracker_backend.service.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EventPublisherImpl implements EventPublisher {

    private final List<EventObserver> observers = new ArrayList<>();

    public EventPublisherImpl(List<EventObserver> observers) {
        this.observers.addAll(observers);
        log.info("EventPublisher inicializado con {} observadores", this.observers.size());
    }

    @Override
    public void publishLimitAlert(LimitAlertEvent event) {
        log.info("📢 PUBLICANDO EVENTO: Usuario {} alcanzó {}% del límite ({}/{})",
                event.getUserEmail(),
                String.format("%.1f", event.getUsagePercentage()),
                event.getCurrentExpenseCount(),
                event.getMonthlyLimit());

        for (EventObserver observer : observers) {
            try {
                observer.onLimitAlert(event);
            } catch (Exception e) {
                log.error("Error al notificar observador {}: {}",
                        observer.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}