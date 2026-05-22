package com.tuan.debtwizard.features.payment.dto;

import com.tuan.debtwizard.features.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Debt ID không được để trống")
    private Long debtId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message ="Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
    @NotNull(message = "Ngày thanh toán không được để trống")
    private LocalDate paymentDate;

    @Size(max = 255)
    private String note;
}