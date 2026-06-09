package com.tuan.debtwizard.features.debt.model;

import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.debt.service.DebtStateService;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import com.tuan.debtwizard.features.payment.model.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "debts")
public class Debt {

    public Debt() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "debt", fetch = FetchType.LAZY)
    private List<Payment> payments;

    @Column(nullable = false)
    private String lenderName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrincipal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedMonthlyPayment;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer dueDay;

    @Column(nullable = false)
    private LocalDate nextDueDate;

    private LocalDate lastPaymentDate;

    // Ngày cuối đã tính lãi
    @Column(nullable = false)
    private LocalDate lastInterestAccruedDate;

    @OneToOne(mappedBy = "debt", cascade = CascadeType.ALL)
    private InterestConfig interestConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    // Lãi đã phát sinh
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtType debtType;

    @Column(nullable = false)
    private boolean deleted = false;


    private LocalDateTime paidOffAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
        if (remainingPrincipal == null) {
            remainingPrincipal = totalPrincipal;
        }

        if (accruedInterest == null) {
            accruedInterest = BigDecimal.ZERO;
        }
        if (lastInterestAccruedDate == null) {
            lastInterestAccruedDate = startDate;
        }

    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    @Transient//ko lưu vô db
    public BigDecimal getTotalOutstanding() {
        BigDecimal principal =
                remainingPrincipal == null ? BigDecimal.ZERO : remainingPrincipal;
        BigDecimal interest =
                accruedInterest == null ? BigDecimal.ZERO : accruedInterest;
        return principal.add(interest);

    }
}