package com.tuan.debtwizard.features.debt.model;

import com.tuan.debtwizard.features.auth.model.User;
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
    public Debt(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "debt")
    private List<Payment> payments;

    @Column(nullable = false)
    private String lenderName;
    @Column(nullable = false,precision = 15, scale = 2)// 2 chữ số tphan
    private BigDecimal totalPrincipal;//số tiền gốc ban đầu
    @Column(nullable = false,precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;//số tiền gốc còn lại
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate; // %/year (ví dụ: 10 = 10%)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;
    @Column(nullable = false)
    private Integer termMonths;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate dueDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtType debtType;
    @Column(nullable = false)
    private boolean deleted = false;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
