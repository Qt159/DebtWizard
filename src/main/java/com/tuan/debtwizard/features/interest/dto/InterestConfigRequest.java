package com.tuan.debtwizard.features.interest.dto;

import com.tuan.debtwizard.features.interest.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.interest.model.InterestRatePeriod;
import com.tuan.debtwizard.features.payment.model.PaymentApplicationRule;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestConfigRequest {
    @NotNull
    private InterestCalculationMethod interestCalculationMethod;

    @NotNull
    private InterestRatePeriod interestRatePeriod;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal interestRate;

    @NotNull
    private PaymentApplicationRule paymentApplicationRule;

    @PositiveOrZero
    @Max(365)
    private Integer gracePeriodDays = 0;

    @NotNull
    @PositiveOrZero
    private BigDecimal lateFee;
}