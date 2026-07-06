package com.tuan.debtwizard.features.planning.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "plan_monthly_schedules")
public class PlanMonthlySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_plan_id", nullable = false)
    private SavedPlan savedPlan;

    @Column(nullable = false)
    private int monthIndex;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal extraPaymentUsed;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cashflowReleased;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanDebtPayment> debtPayments = new ArrayList<>();
}
