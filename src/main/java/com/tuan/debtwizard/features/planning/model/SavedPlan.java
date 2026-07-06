package com.tuan.debtwizard.features.planning.model;

import com.tuan.debtwizard.features.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "saved_plans")
public class SavedPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStrategy strategy;

    @Column(nullable = false)
    private String planName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyExtraPayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestPaid;

    @Column(nullable = false)
    private int payoffDurationMonths;

    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @OneToMany(mappedBy = "savedPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanMonthlySchedule> monthlySchedules = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}
