package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.dto.PagedResponse;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationService;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.tuan.debtwizard.features.debt.dto.UpdateDebtRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "nextDueDate", "totalPrincipal", "remainingPrincipal");

    @Transactional(readOnly = true)
    public PagedResponse<DebtListItemResponse> getDebts(
            UserDetails userDetails,
            DebtStatus status,
            DebtType debtType,
            LocalDate dueDateBefore,
            LocalDate dueDateAfter,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        User currentUser = findUserOrThrow(userDetails);

        String resolvedSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int resolvedSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, resolvedSize, Sort.by(direction, resolvedSortBy));

        Page<Debt> debtPage = debtRepository.findWithFilters(
                currentUser.getId(), status, debtType, dueDateBefore, dueDateAfter, pageable);

        List<DebtListItemResponse> content = new ArrayList<>();
        for (Debt debt : debtPage.getContent()) {
            content.add(debtMapper.toListItem(debt));
        }

        return PagedResponse.of(
                content,
                debtPage.getNumber(),
                debtPage.getSize(),
                debtPage.getTotalElements(),
                debtPage.getTotalPages(),
                debtPage.isLast());
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
