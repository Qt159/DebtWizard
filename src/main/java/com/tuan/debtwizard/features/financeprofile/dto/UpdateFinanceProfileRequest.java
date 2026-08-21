package com.tuan.debtwizard.features.financeprofile.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateFinanceProfileRequest {

    @NotNull(message = "Thu nhập hàng tháng không được để trống")
    @PositiveOrZero(message = "Thu nhập không được âm")
    @Digits(integer = 15, fraction = 2, message = "Thu nhập tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Chi tiêu hàng tháng không được để trống")
    @PositiveOrZero(message = "Chi tiêu không được âm")
    @Digits(integer = 15, fraction = 2, message = "Chi tiêu tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal monthlyEssentialExpenses;
}
