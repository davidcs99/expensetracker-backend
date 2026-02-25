package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.event.LimitAlertEvent;

public interface EventPublisher {

    void publishLimitAlert(LimitAlertEvent event);
}