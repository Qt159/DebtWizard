package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component

public class PaymentReminderScheduler {
    private final DebtRepository debtRepository;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(PaymentReminderScheduler.class);
    public PaymentReminderScheduler(DebtRepository debtRepository, NotificationService notificationService) {
        this.debtRepository = debtRepository;
        this.notificationService = notificationService;
    }
    @Scheduled(cron = "0 0 0 * * ?")
    public void schedulePaymentReminder() {
        log.info("Starting payment reminder scheduler");
        LocalDate reminderDate =  LocalDate.now().plusDays(3);
        List<Debt> debts = debtRepository.findByNextDueDateAndStatus(reminderDate, DebtStatus.ACTIVE);
        for (Debt debt : debts) {
            notificationService.createPaymentReminder(debt);
        }
        log.info("Created payment reminders for {} debts", debts.size());
    }
}
