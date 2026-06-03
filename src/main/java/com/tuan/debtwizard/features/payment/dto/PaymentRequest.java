package com.tuan.debtwizard.features.payment.dto;

import com.tuan.debtwizard.features.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    @NotNull
    private Long debtId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    @PastOrPresent
    private LocalDate paymentDate;


    @Size(max = 255)
    private String note;
}