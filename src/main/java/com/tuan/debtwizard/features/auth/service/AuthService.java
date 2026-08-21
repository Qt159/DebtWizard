package com.tuan.debtwizard.features.auth.service;

import com.tuan.debtwizard.features.auth.dto.*;
import com.tuan.debtwizard.features.auth.model.RefreshToken;
import com.tuan.debtwizard.features.financeprofile.service.FinanceProfileService;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.auth.repository.RefreshTokenRepository;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FinanceProfileService profileService;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

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
        User savedUser = userRepository.save(user);
        profileService.createDefault(savedUser);

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
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        refreshTokenRepository.save(rt);
        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
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
        newRt.setExpiryDate(
                Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(newRt);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }
}