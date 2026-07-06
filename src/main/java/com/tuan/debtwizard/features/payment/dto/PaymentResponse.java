package com.tuan.debtwizard.features.payment.dto;

import com.tuan.debtwizard.features.payment.model.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {
    private Long id;
    private Long debtId;
    private String lenderName;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PaymentMethod paymentMethod;
}
