package com.tuan.debtwizard.features.auth.service;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.auth.dto.UserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.tuan.debtwizard.features.auth.dto.LoginRequest;
import com.tuan.debtwizard.features.auth.dto.LoginResponse;
import com.tuan.debtwizard.features.auth.dto.RegisterRequest;
import com.tuan.debtwizard.features.auth.dto.RegisterResponse;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

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
        user.setMonthlyIncome(request.getMonthlyIncome() != null
                ? request.getMonthlyIncome()
                : BigDecimal.ZERO);
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName()
        );
    }

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUsername(),
                                    request.getPassword()
                            )
                    );
            saveAuthentication(authentication, httpRequest);
            User user = userRepository.findByUsername(
                    request.getUsername()
            ).orElseThrow(() ->
                    new AppException(ErrorCode.USER_NOT_FOUND)
            );

            return new LoginResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail());

        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    public void processGoogleLogin(
            OAuth2User oAuth2User,
            HttpServletRequest request
    ) {

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new AppException(ErrorCode.INVALID_OAUTH2_USER);
        }
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email);
                    newUser.setEmail(email);
                    newUser.setFullName(name);

                    newUser.setPassword(
                            passwordEncoder.encode("google_default_pass")
                    );
                    newUser.setMonthlyIncome(BigDecimal.ZERO);
                    return userRepository.save(newUser);
                });

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getUsername()
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        saveAuthentication(authentication, request);
    }

    private void saveAuthentication(
            Authentication authentication,
            HttpServletRequest request
    ) {

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        request.getSession(true)
                .setAttribute(
                        "SPRING_SECURITY_CONTEXT",
                        SecurityContextHolder.getContext()
                );
    }
    public UserResponse getCurrentUser(UserDetails userDetails){
        if (userDetails == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String userName = userDetails.getUsername();
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getMonthlyIncome());
    }
}