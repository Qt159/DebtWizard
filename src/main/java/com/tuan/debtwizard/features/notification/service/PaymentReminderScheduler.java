package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.event.EventPublisher;
import com.tuan.debtwizard.features.event.PaymentReminderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentReminderScheduler {
    private final DebtRepository debtRepository;
    private final EventPublisher eventPublisher;
    private static final Logger log =
            LoggerFactory.getLogger(PaymentReminderScheduler.class);
    public PaymentReminderScheduler(DebtRepository debtRepository, EventPublisher eventPublisher) {
        this.debtRepository = debtRepository;
        this.eventPublisher = eventPublisher;
    }
    @Scheduled(cron = "0 0 0 * * ?")
    public void schedulePaymentReminder() {
        log.info("Starting payment reminder scheduler");
        LocalDate reminderDate =  LocalDate.now().plusDays(3);
        List<Debt> debts = debtRepository.findByNextDueDateAndStatus(reminderDate, DebtStatus.ACTIVE);
        for (Debt debt : debts) {
            eventPublisher.publish(
                    new PaymentReminderEvent(
                            UUID.randomUUID(), debt.getId(),
                            debt.getUser().getId(), debt.getLenderName(),
                            debt.getNextDueDate(),
                            debt.getExpectedMonthlyPayment()));
        }
        log.info("Created payment reminders for {} debts", debts.size());
    }
}
