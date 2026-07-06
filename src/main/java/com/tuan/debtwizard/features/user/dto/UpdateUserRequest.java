package com.tuan.debtwizard.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.math.BigDecimal;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    private String fullName;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
}
