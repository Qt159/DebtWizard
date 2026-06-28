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

    @NotBlank
    @Size(max = 100)
    private String lenderName;
    @NotNull
    @Positive
    private BigDecimal totalPrincipal;
    @NotNull
    private LocalDate startDate;
    @NotNull
    @Positive
    private Integer termMonths;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer dueDay;

    @NotNull
    private DebtType debtType;
}