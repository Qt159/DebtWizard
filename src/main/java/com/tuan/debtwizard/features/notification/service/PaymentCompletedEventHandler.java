package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.features.event.PaymentCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentCompletedEventHandler {
    private final NotificationService notificationService;
    public PaymentCompletedEventHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        System.out.println(">>> PAYMENT COMPLETED EVENT RECEIVED: "
                + event.paymentId());
        notificationService.createPaymentCompletedNotification(event);
    }
}
