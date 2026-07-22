package com.tuan.debtwizard.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.math.BigDecimal;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100)
    private String fullName;
    @PositiveOrZero
    private BigDecimal monthlyIncome;
    @PositiveOrZero
    private BigDecimal monthlyExpense;
}
