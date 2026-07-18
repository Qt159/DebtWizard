package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CompareRequest {
    @NotEmpty
    private List<Long> debtIds;
    @NotNull
    @PositiveOrZero
    private BigDecimal monthlyExtraPayment;
    @NotNull
    private RepaymentStrategy firstStrategy;
    @NotNull
    private RepaymentStrategy secondStrategy;
}