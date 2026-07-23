package com.tuan.debtwizard.features.planning.repository;

import com.tuan.debtwizard.features.planning.model.SavedPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedPlanRepository extends JpaRepository<SavedPlan, Long> {

    Optional<SavedPlan> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
    @Query("""
        SELECT DISTINCT sp
        FROM SavedPlan sp
        LEFT JOIN FETCH sp.monthlySchedules ms
        LEFT JOIN FETCH ms.debtPayments dp
        LEFT JOIN FETCH dp.debt
        WHERE sp.user.id = :userId
    """)
    Optional<SavedPlan> findDetailByUserId(@Param("userId") Long userId);
    void deleteByUserId(Long userId);
}
