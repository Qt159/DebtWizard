package com.tuan.debtwizard.features.interest.model;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.model.PaymentApplicationRule;
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
    @JoinColumn(name = "debt_id")
    private Debt debt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private InterestCalculationMethod interestCalculationMethod;//Flat or Reducing balance

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private InterestRatePeriod interestRatePeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentApplicationRule paymentApplicationRule;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal interestRate;

    private Integer gracePeriodDays = 0;// Khoảng thời gian được miễn lãi hoặc miễn phạt sau ngày đến hạn.

    @Column(precision = 8, scale = 2)
    private BigDecimal monthlyServiceFee;
    @Column(precision = 8, scale = 2)
    private BigDecimal insuranceFee;
    @Column(precision = 8, scale = 2)
    private BigDecimal overdueInterestRate;
    @Column(precision = 8, scale = 2)
    private BigDecimal lateFee;


}