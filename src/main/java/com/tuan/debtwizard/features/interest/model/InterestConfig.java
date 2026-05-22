package com.tuan.debtwizard.features.interest.model;

import com.tuan.debtwizard.features.debt.model.Debt;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "interest_configs")
@Data
public class InterestConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private InterestCalculationMethod interestCalculationMethod;//Flat or Reducing balance

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private InterestRatePeriod interestRatePeriod;

    @Column(nullable = false, updatable = false, precision = 8, scale = 2)
    private BigDecimal interestRate;


    private Boolean interestEnabled = true;

    private Integer gracePeriodDays = 0;

    private BigDecimal monthlyServiceFee;
    private BigDecimal insuranceFee;
    private BigDecimal overdueInterestRate;
    private BigDecimal lateFee;
}