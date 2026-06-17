package com.tuan.debtwizard.features.debt.repository;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByUserIdAndDeletedFalse(Long userId);
    List<Debt> findByUserIdAndStatusAndDeletedFalse(Long userId, DebtStatus status);

    Optional<Debt> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Query("""
    SELECT SUM(d.totalPrincipal)
    FROM Debt d
    WHERE d.user.id = :userId
    AND d.deleted = false
    """)
    BigDecimal getTotalDebt(@Param("userId") Long userId);

    @Query("""
    SELECT SUM(d.remainingPrincipal)
    FROM Debt d
    WHERE d.user.id = :userId
    AND d.status = 'OVERDUE'
    AND d.deleted = false
    """)
    BigDecimal getTotalOverdue(@Param("userId") Long userId);
    @Query("""
    SELECT COUNT(d)
    FROM Debt d
    WHERE d.user.id = :userId
    AND d.deleted = false
    """)
    int countDebt(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(d)
    FROM Debt d
    WHERE d.user.id = :userId
    AND d.status = :status
    AND d.deleted = false
    """)
    int countDebtByStatus(
            @Param("userId") Long userId,
            @Param("status") DebtStatus status
    );

    Page<Debt> findByDeletedFalseAndStatusNot(DebtStatus debtStatus, Pageable pageable);


    @Query("""
    SELECT SUM(d.accruedInterest)
    FROM Debt d
    WHERE d.user.id = :userId
    and d.deleted = false
""")
    BigDecimal getTotalAccruedInterest(@Param ("userId")Long userId);


    @Query("""
    SELECT SUM(d.remainingPrincipal)
    FROM Debt d
    WHERE d.user.id = :userId
    and d.deleted = false
""")
    BigDecimal getTotalRemainingDebt(@Param("userId")Long userId);

    @Query("""
    SELECT SUM(d.expectedMonthlyPayment)
    FROM Debt d 
    WHERE d.user.id = :userId
    and d.deleted = false
    and d.status = com.tuan.debtwizard.features.debt.model.DebtStatus.ACTIVE
""")
    BigDecimal getTotalActiveExpectedMonthlyPayment(@Param("userId") Long userId);
}