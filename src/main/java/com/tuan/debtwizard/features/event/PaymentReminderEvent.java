package com.tuan.debtwizard.features.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentReminderEvent(
        UUID eventId,
        Long debtId,
        Long userId,
        String lenderName,
        LocalDate nextDueDate,
        BigDecimal expectedMonthlyPayment) { }