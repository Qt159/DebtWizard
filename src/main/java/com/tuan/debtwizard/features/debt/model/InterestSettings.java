package com.tuan.debtwizard.features.debt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class InterestSettings {
    @Enumerated(EnumType.STRING)
    private InterestCalculationMethod interestCalculationMethod;

    @Enumerated(EnumType.STRING)
    private InterestFrequency interestFrequency;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal interestRate;

    @Column(precision = 8, scale = 2)
    private BigDecimal lateFee;
}
