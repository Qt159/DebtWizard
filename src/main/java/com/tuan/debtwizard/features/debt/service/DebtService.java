package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.UpdateDebtRequest;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationService;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        Debt debt = debtMapper.toEntity(request, request.getInterestSettings());
        debt.setUser(currentUser);
        debt.setStatus(DebtStatus.ACTIVE);
        debt.setNextDueDate(debtStateService.calculateFirstDueDate(debt));
        debt.setExpectedMonthlyPayment(interestCalculationService.calculateMonthlyPayment(debt));
        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Transactional(readOnly = true)
    public List<DebtListItemResponse> getDebts(
            UserDetails userDetails,
            String search,
            DebtStatus status,
            InterestCalculationMethod interestMethod,
            String sortBy,
            String sortDir) {

        User currentUser = findUserOrThrow(userDetails);

        List<Debt> debts = (search == null || search.isBlank())
                ? debtRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                : debtRepository.findByUserIdWithSearch(currentUser.getId(), search);

        // Filter enums in-memory to avoid JPQL enum null-check issues
        if (status != null) {
            debts = debts.stream()
                    .filter(d -> d.getStatus() == status)
                    .collect(java.util.stream.Collectors.toList());
        }
        if (interestMethod != null) {
            debts = debts.stream()
                    .filter(d -> d.getInterestSettings() != null
                            && d.getInterestSettings().getInterestCalculationMethod() == interestMethod)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Sort in-memory
        Comparator<Debt> comparator = "remainingPrincipal".equals(sortBy)
                ? Comparator.comparing(Debt::getRemainingPrincipal)
                : Comparator.comparing(Debt::getCreatedAt);

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        debts.sort(comparator);

        List<DebtListItemResponse> result = new ArrayList<>();
        for (Debt debt : debts) {
            result.add(debtMapper.toListItem(debt));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        return debtMapper.toResponse(debt);
    }

    public DebtResponse updateDebt(Long id, UpdateDebtRequest request, UserDetails userDetails) {
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
