package com.tuan.debtwizard.features.financeprofile.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.financeprofile.dto.FinanceProfileResponse;
import com.tuan.debtwizard.features.financeprofile.dto.UpdateFinanceProfileRequest;
import com.tuan.debtwizard.features.financeprofile.model.FinanceProfile;
import com.tuan.debtwizard.features.financeprofile.repository.FinanceProfileRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class FinanceProfileService {

    private final FinanceProfileRepository financeProfileRepository;
    private final UserRepository userRepository;

    public FinanceProfileService(FinanceProfileRepository financeProfileRepository,
                                 UserRepository userRepository) {
        this.financeProfileRepository = financeProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public FinanceProfileResponse getProfile(UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        FinanceProfile profile = financeProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FINANCE_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    @Transactional
    public FinanceProfileResponse updateProfile(UserDetails userDetails,
                                                UpdateFinanceProfileRequest request) {
        User user = getUser(userDetails.getUsername());
        FinanceProfile profile = financeProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FINANCE_PROFILE_NOT_FOUND));

        profile.setMonthlyIncome(request.getMonthlyIncome());
        profile.setMonthlyEssentialExpenses(request.getMonthlyEssentialExpenses());

        return toResponse(financeProfileRepository.save(profile));
    }

    
    @Transactional
    public FinanceProfile createDefault(User user) {
        FinanceProfile profile = new FinanceProfile(user, BigDecimal.ZERO, BigDecimal.ZERO);
        return financeProfileRepository.save(profile);
    }


    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private FinanceProfileResponse toResponse(FinanceProfile profile) {
        return new FinanceProfileResponse(
                profile.getId(),
                profile.getMonthlyIncome(),
                profile.getMonthlyEssentialExpenses(),
                profile.getUpdatedAt()
        );
    }
}
