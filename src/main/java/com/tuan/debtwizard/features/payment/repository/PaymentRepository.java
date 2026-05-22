package com.tuan.debtwizard.features.payment.repository;

import com.tuan.debtwizard.features.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByDebtId(Long debtId);

    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.debt.user.id = :userId
""")
    BigDecimal getTotalPaid(@Param("userId") Long userId);
}
