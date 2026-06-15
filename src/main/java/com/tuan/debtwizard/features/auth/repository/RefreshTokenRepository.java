package com.tuan.debtwizard.features.auth.repository;

import com.tuan.debtwizard.features.auth.model.RefreshToken;
import com.tuan.debtwizard.features.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUser(User user);
    Optional<RefreshToken> findByToken(String token);
}
