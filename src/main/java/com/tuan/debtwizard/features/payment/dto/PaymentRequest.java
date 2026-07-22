package com.tuan.debtwizard.features.payment.dto;

import com.tuan.debtwizard.features.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
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

    @NotNull(message = "Khoản nợ không được để trống")
    private Long debtId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    @Digits(integer = 15, fraction = 2, message = "Số tiền thanh toán tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Ngày thanh toán không được để trống")
    @PastOrPresent(message = "Ngày thanh toán không được là ngày trong tương lai")
    private LocalDate paymentDate;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}