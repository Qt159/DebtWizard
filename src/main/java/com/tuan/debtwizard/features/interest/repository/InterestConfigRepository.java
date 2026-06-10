package com.tuan.debtwizard.features.interest.repository;

import com.tuan.debtwizard.features.interest.model.InterestConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestConfigRepository extends JpaRepository<InterestConfig, Long> {
    Optional<InterestConfig> findByDebtId(Long debtId);
}