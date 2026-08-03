package com.tuan.debtwizard.features.planning.repository;

import com.tuan.debtwizard.features.planning.model.SavedPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedPlanRepository extends JpaRepository<SavedPlan, Long> {

    Optional<SavedPlan> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
