package com.tuan.debtwizard.features.payment.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdatePaymentRequest {
    @Size(max = 255)
    private String note;

    @PastOrPresent
    private LocalDate paymentDate;
}
