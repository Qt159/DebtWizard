package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.interest.mapper.InterestConfigMapper;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DebtService {
    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;
    private final UserRepository userRepository;
    private final InterestConfigMapper interestConfigMapper;
    private final DebtStateService debtStateService;
    public DebtService(
            DebtRepository debtRepository,
            DebtMapper debtMapper,
            UserRepository userRepository,
            InterestConfigMapper interestConfigMapper,
            DebtStateService debtStateService) {
        this.debtRepository = debtRepository;
        this.debtMapper = debtMapper;
        this.userRepository = userRepository;
        this.interestConfigMapper = interestConfigMapper;
        this.debtStateService = debtStateService;
    }

    private User findUserOrThrow(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // CREATE
    @Transactional
    public DebtResponse createDebt(CreateDebtRequest createDebtRequest,
                                   UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtMapper.toEntity(createDebtRequest.getDebt());
        debt.setUser(currentUser);
        debt.setStatus(DebtStatus.ACTIVE);
        debt.setNextDueDate(debtStateService.calculateFirstDueDate(debt));
        debt.setExpectedMonthlyPayment(
                calculateExpectedMonthlyPayment(
                        debt.getTotalPrincipal(),
                        debt.getTermMonths()));

        InterestConfig interestConfig =
                interestConfigMapper.toEntity(
                        createDebtRequest.getInterestConfig(),
                        debt);

        debt.setInterestConfig(interestConfig);

        Debt savedDebt = debtRepository.save(debt);

        return debtMapper.toResponse(savedDebt);
    }

    // GET LIST
    @Transactional(readOnly = true)
    public List<DebtListItemResponse> getDebts(
            UserDetails userDetails,
            DebtStatus status) {
        User currentUser = findUserOrThrow(userDetails);
        List<Debt> debts;

        if (status == null) {
            debts = debtRepository.findByUserIdAndDeletedFalse(currentUser.getId());
        } else {
            debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(
                    currentUser.getId(),
                    status);
        }
        List<DebtListItemResponse> items = new ArrayList<>();
        for (Debt debt : debts) {
            items.add(debtMapper.toListItem(debt));
        }
        return items;
    }

    // GET DETAIL
    @Transactional(readOnly = true)
    public DebtResponse getDebtById(
            Long id,
            UserDetails userDetails) {

        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND));
        return debtMapper.toResponse(debt);
    }

    // UPDATE
    public DebtResponse updateDebt(
            Long id,
            DebtRequest req,
            UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND));

        if (req.getLenderName() != null) {
            debt.setLenderName(req.getLenderName());
        }
        Debt updatedDebt = debtRepository.save(debt);
        return debtMapper.toResponse(updatedDebt);
    }

    // DELETE
    public void deleteDebt(
            Long id,
            UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND));

        debt.setDeleted(true);
        debtRepository.save(debt);
    }

    public BigDecimal calculateExpectedMonthlyPayment(
            BigDecimal principal,
            Integer termMonths) {

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (termMonths == null || termMonths <= 0) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        return principal.divide(
                BigDecimal.valueOf(termMonths),
                2,
                RoundingMode.HALF_UP
        );
    }
}