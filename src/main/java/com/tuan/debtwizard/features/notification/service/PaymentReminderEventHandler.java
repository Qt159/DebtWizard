package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.features.event.PaymentReminderEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderEventHandler {
    private final NotificationService notificationService;
    public PaymentReminderEventHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @EventListener
    public void handlePaymentReminder(PaymentReminderEvent event) {
        notificationService.createPaymentReminder(event);
    }
}
