package com.tuan.debtwizard.features.payment.repository;

import com.tuan.debtwizard.features.payment.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByDebtIdAndDeletedFalse(Long debtId);

    @Query("""
    SELECT p FROM Payment p
    WHERE p.debt.id = :debtId
    AND p.debt.user.id = :userId
    AND p.deleted = false
    """)
    Page<Payment> findByDebtIdAndUserId(
            @Param("debtId") Long debtId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Payment p
    WHERE p.debt.user.id = :userId
    AND p.deleted = false
    ORDER BY p.paymentDate DESC
    """)
    List<Payment> findAllByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.debt.user.id = :userId
    AND p.deleted = false
    """)
    BigDecimal getTotalPaid(@Param("userId") Long userId);

    Optional<Payment> findByIdAndDebtUserIdAndDeletedFalse(Long paymentId, Long userId);
}