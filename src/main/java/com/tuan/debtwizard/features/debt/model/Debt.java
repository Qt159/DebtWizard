package com.tuan.debtwizard.features.debt.model;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.payment.model.Payment;
import com.tuan.debtwizard.features.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "debts")
public class Debt {

    public Debt() {}

    public Debt(
            String lenderName,
            BigDecimal totalPrincipal,
            Integer termMonths,
            LocalDate startDate,
            Integer dueDay,
            DebtType debtType,
            InterestSettings interestSettings
    ) {
        this.lenderName = lenderName;
        this.totalPrincipal = totalPrincipal;
        this.remainingPrincipal = totalPrincipal;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.dueDay = dueDay;
        this.debtType = debtType;
        this.interestSettings = interestSettings;
        this.status = DebtStatus.ACTIVE;
        this.accruedInterest = BigDecimal.ZERO;
        this.deleted = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "debt", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @Column(nullable = false)
    private String lenderName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrincipal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedMonthlyPayment = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer dueDay;

    @Column(nullable = false)
    private LocalDate nextDueDate;

    private LocalDate lastPaymentDate;

    @Column(nullable = false)
    private LocalDate lastInterestAccruedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtType debtType;

    private boolean deleted = false;

    private LocalDateTime paidOffAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Embedded
    private InterestSettings interestSettings;


    public void addInterest(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.accruedInterest = this.accruedInterest.add(amount);
    }


    public void reducePrincipal(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;}

        if (amount.compareTo(this.remainingPrincipal) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_DEBT_BALANCE);}
        this.remainingPrincipal = this.remainingPrincipal.subtract(amount);
    }


    public void markAsPaidOff() {
        this.status = DebtStatus.PAID_OFF;
        this.paidOffAt = LocalDateTime.now();
    }


    public boolean isPaidOff() {
        return getTotalOutstanding().compareTo(BigDecimal.ZERO) <= 0;
    }


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


    @Transient
    public BigDecimal getTotalOutstanding() {

        BigDecimal principal = remainingPrincipal;
        BigDecimal interest = accruedInterest;

        if (principal == null) {
            principal = BigDecimal.ZERO;}

        if (interest == null) {
            interest = BigDecimal.ZERO;}

        return principal.add(interest);
    }
}