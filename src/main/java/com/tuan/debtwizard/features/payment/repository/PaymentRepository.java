package com.tuan.debtwizard.features.payment.repository;

import com.tuan.debtwizard.features.payment.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
    SELECT p 
    FROM Payment p
    JOIN p.debt d
    WHERE d.id = :debtId
    AND d.user.id = :userId
    AND p.deleted = false
    """)
    Page<Payment> findByDebtIdAndUserId(
            @Param("debtId") Long debtId,
            @Param("userId") Long userId,
            Pageable pageable
    );
    @Query("""
    SELECT p
    FROM Payment p
    JOIN FETCH p.debt d
    WHERE d.id = :debtId
    AND d.user.id = :userId
    AND p.deleted = false
    AND p.paymentDate >= :dateFrom
    """)
    Page<Payment> findByDebtIdAndUserIdAndDateFrom(
            @Param("debtId") Long debtId,
            @Param("userId") Long userId,
            @Param("dateFrom") LocalDate dateFrom,
            Pageable pageable
    );
    @Query("""
    SELECT p
    FROM Payment p
    JOIN FETCH p.debt d
    WHERE d.id = :debtId
    AND d.user.id = :userId
    AND p.deleted = false
    AND p.paymentDate <= :dateTo
    """)
    Page<Payment> findByDebtIdAndUserIdAndDateTo(
            @Param("debtId") Long debtId,
            @Param("userId") Long userId,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );
    @Query("""
    SELECT p
    FROM Payment p
    JOIN FETCH p.debt d
    WHERE d.id = :debtId
    AND d.user.id = :userId
    AND p.deleted = false
    AND p.paymentDate >= :dateFrom
    AND p.paymentDate <= :dateTo
    """)
    Page<Payment> findByDebtIdAndUserIdAndDateRange(
            @Param("debtId") Long debtId,
            @Param("userId") Long userId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    @Query("""
    SELECT p 
    FROM Payment p
    JOIN FETCH p.debt
    WHERE p.debt.user.id = :userId
    AND p.deleted = false
    """)
    Page<Payment> findAllByUserId(@Param("userId") Long userId,
                                  Pageable pageable);

    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.debt.user.id = :userId
    AND p.deleted = false
    """)
    BigDecimal getTotalPaid(@Param("userId") Long userId);

    Optional<Payment> findByIdAndDebtUserIdAndDeletedFalse(Long paymentId, Long userId);
}