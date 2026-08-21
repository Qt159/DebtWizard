package com.tuan.debtwizard.features.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

//immutable : ko nên để ai đó sửa event giữa đường
public record PaymentCompletedEvent(
        UUID eventId,
        Long paymentId, Long userId,
        Long debtId,
        BigDecimal amount,
        BigDecimal principalPaid,
        BigDecimal interestPaid,
        String lenderName,
        Instant occurredAt) {

}
