package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationService;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.tuan.debtwizard.features.debt.dto.UpdateDebtRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;
    private final InterestCalculationService interestCalculationService;

    public DebtService(DebtRepository debtRepository,
                       DebtMapper debtMapper,
                       UserRepository userRepository,
                       DebtStateService debtStateService,
                       InterestCalculationService interestCalculationService) {
        this.debtRepository = debtRepository;
        this.debtMapper = debtMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
        this.interestCalculationService = interestCalculationService;
    }

    private User findUserOrThrow(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public DebtResponse createDebt(CreateDebtRequest request, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtMapper.toEntity( request, request.getInterestSettings());
        debt.setUser(currentUser);
        debt.setStatus(DebtStatus.ACTIVE);
        debt.setNextDueDate(debtStateService.calculateFirstDueDate(debt));
        debt.setExpectedMonthlyPayment(interestCalculationService.calculateMonthlyPayment(debt));
        Debt savedDebt = debtRepository.save(debt);
        return debtMapper.toResponse(savedDebt);
    }

    @Transactional(readOnly = true)
    public List<DebtListItemResponse> getDebts(UserDetails userDetails, DebtStatus status) {
        User currentUser = findUserOrThrow(userDetails);
        List<Debt> debts;
        if (status == null) {
            debts = debtRepository.findByUserIdAndDeletedFalse(currentUser.getId());
        } else {
            debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(currentUser.getId(), status);
        }
        List<DebtListItemResponse> items = new ArrayList<>();
        for (Debt debt : debts) {
            items.add(debtMapper.toListItem(debt));
        }
        return items;
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        return debtMapper.toResponse(debt);
    }

    public DebtResponse updateDebt(Long id,  UpdateDebtRequest request, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        if (request.getLenderName() != null) {
            debt.setLenderName(request.getLenderName());
        }
        return debtMapper.toResponse(debtRepository.save(debt));
    }

    public void deleteDebt(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        debt.setDeleted(true);
        debtRepository.save(debt);
    }
}
