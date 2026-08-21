package com.tuan.debtwizard.features.financeprofile.repository;

import com.tuan.debtwizard.features.financeprofile.model.FinanceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceProfileRepository extends JpaRepository<FinanceProfile, Long> {

    Optional<FinanceProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
