package com.tuan.debtwizard.features.debt.repository;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByUserIdAndDeletedFalse(Long userId);
    List<Debt> findByUserIdAndStatusAndDeletedFalse(Long userId, DebtStatus status);

    @Query("""
    SELECT d FROM Debt d
    WHERE d.user.id = :userId
    AND d.deleted = false
    AND (:status IS NULL OR d.status = :status)
    AND (:debtType IS NULL OR d.debtType = :debtType)
    AND (:dueDateBefore IS NULL OR d.nextDueDate <= :dueDateBefore)
    AND (:dueDateAfter IS NULL OR d.nextDueDate >= :dueDateAfter)
    """)
    Page<Debt> findWithFilters(
            @Param("userId") Long userId,
            @Param("status") DebtStatus status,
            @Param("debtType") DebtType debtType,
            @Param("dueDateBefore") LocalDate dueDateBefore,
            @Param("dueDateAfter") LocalDate dueDateAfter,
            Pageable pageable
    );

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
    AND d.status = com.tuan.debtwizard.features.debt.model.DebtStatus.OVERDUE
    AND d.deleted = false
    """)
    BigDecimal getTotalOverdue(@Param("userId") Long userId);

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

    Page<Debt> findByDeletedFalseAndStatusNot(
            DebtStatus debtStatus,
            Pageable pageable
    );
    @Query("""
    SELECT d FROM Debt d 
    WHERE d.user.id = :userId 
    AND d.status <> com.tuan.debtwizard.features.debt.model.DebtStatus.PAID_OFF
    AND d.deleted = false
    """)
    List<Debt> findUnpaidDebtsByUserId(@Param("userId") Long userId);
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
    @Query("""
    SELECT d
    FROM Debt d
    JOIN FETCH d.user
    WHERE d.id IN :ids
    """)
    List<Debt> findAllByIdWithUser(@Param("ids") List<Long> ids);
}