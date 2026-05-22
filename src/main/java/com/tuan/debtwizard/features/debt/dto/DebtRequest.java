package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DebtRequest {

    @NotBlank(message = "Tên người cho vay không được để trống")
    private String lenderName;

    @NotNull(message = "Số tiền vay không được để trống")
    @Positive(message = "Số tiền vay phải lớn hơn 0")
    private BigDecimal totalPrincipal;

    @NotNull(message = "Ngày bắt đầu vay không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Loại khoản nợ không được để trống")
    private DebtType debtType;

    @NotNull(message = "Số tiền thanh toán hàng tháng không được để trống")
    @Positive(message = "Số tiền thanh toán hàng tháng phải lớn hơn 0")
    private BigDecimal monthlyPayment;

    @NotNull(message = "Lãi suất năm không được để trống")
    @PositiveOrZero
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal interestRate;

    @NotNull(message = "Thời hạn vay không được để trống")
    @Positive(message = "Thời hạn vay phải lớn hơn 0")
    private Integer termMonths;

    @NotNull(message = "Ngày hạn thanh toán hàng tháng không được để trống")
    @Min(value = 1, message = "Ngày hạn thanh toán thấp nhất là ngày 1")
    @Max(value = 28, message = "Ngày hạn thanh toán cao nhất là ngày 28")
    private LocalDate dueDay;
}