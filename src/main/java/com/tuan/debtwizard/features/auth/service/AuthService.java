package com.tuan.debtwizard.features.auth.service;

import com.tuan.debtwizard.features.auth.model.RefreshToken;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.auth.repository.RefreshTokenRepository;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.auth.dto.LoginRequest;
import com.tuan.debtwizard.features.auth.dto.LoginResponse;
import com.tuan.debtwizard.features.auth.dto.RegisterRequest;
import com.tuan.debtwizard.features.auth.dto.RegisterResponse;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMonthlyIncome(request.getMonthlyIncome() != null ? request.getMonthlyIncome() : BigDecimal.ZERO);
        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getFullName());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Xóa token cũ trước khi tạo token mới để tránh lỗi
        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plus(Duration.ofDays(7)));

        refreshTokenRepository.save(rt);
        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        User user = tokenEntity.getUser();

        // Kiểm tra hết hạn
        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByUser(user);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Xóa cũ, lưu mới
        refreshTokenRepository.deleteByUser(user);

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefreshToken);
        newRt.setUser(user);
        newRt.setExpiryDate(Instant.now().plus(Duration.ofDays(7)));
        refreshTokenRepository.save(newRt);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
}