package com.backend.expensetracker_backend.service.factory;

import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportServiceFactory {

    @Qualifier("freeReportService")
    private final ReportService freeReportService;

    @Qualifier("premiumReportService")
    private final ReportService premiumReportService;

    public ReportService getReportService(SubscriptionType subscriptionType) {
        return switch (subscriptionType) {
            case FREE -> freeReportService;
            case PREMIUM -> premiumReportService;
        };
    }
}