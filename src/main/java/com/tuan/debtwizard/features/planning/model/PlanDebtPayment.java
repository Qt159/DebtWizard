package com.tuan.debtwizard.features.planning.model;

import com.tuan.debtwizard.features.debt.model.Debt;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "plan_debt_payments")
public class PlanDebtPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private PlanMonthlySchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    // Denormalized — giữ tên tại thời điểm save vì lenderName có thể bị update
    @Column(nullable = false)
    private String debtName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal extraPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private boolean paidOff;
}
