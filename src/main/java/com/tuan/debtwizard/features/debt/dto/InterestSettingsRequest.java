package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.debt.model.InterestFrequency;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestSettingsRequest {
    @NotNull
    private InterestCalculationMethod interestCalculationMethod;

    @NotNull
    private InterestFrequency interestFrequency;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal interestRate;
}
