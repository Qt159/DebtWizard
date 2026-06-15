package com.tuan.debtwizard.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Data
public class UpdateUserRequest {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;
    private BigDecimal monthlyIncome;
}
