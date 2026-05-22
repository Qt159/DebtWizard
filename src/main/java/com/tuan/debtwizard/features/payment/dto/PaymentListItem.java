package com.tuan.debtwizard.features.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tuan.debtwizard.features.payment.model.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentListItem {
    private Long id;
    private Long debtId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String note;
}