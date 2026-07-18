package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.dto.PagedResponse;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.DebtStateService;
import com.tuan.debtwizard.features.debt.service.interest.InterestAccrualService;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.mapper.PaymentMapper;
import com.tuan.debtwizard.features.payment.model.Payment;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;
    private final InterestAccrualService interestAccrualService;

    public PaymentService(PaymentRepository paymentRepository,
                          DebtRepository debtRepository,
                          PaymentMapper paymentMapper,
                          UserRepository userRepository,
                          DebtStateService debtStateService,
                          InterestAccrualService interestAccrualService) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.paymentMapper = paymentMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
        this.interestAccrualService = interestAccrualService;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(request.getDebtId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            throw new AppException(ErrorCode.DEBT_ALREADY_PAID_OFF);
        }

        validatePaymentDate(request.getPaymentDate(), debt);

        interestAccrualService.accrueInterest(debt, request.getPaymentDate());

        BigDecimal amount = request.getAmount();
        BigDecimal interest = debt.getAccruedInterest();
        BigDecimal principal = debt.getRemainingPrincipal();

        BigDecimal totalDebt = interest.add(principal);
        if (amount.compareTo(totalDebt) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_REMAINING);
        }
        BigDecimal interestPaid = amount.min(interest);
        BigDecimal remainingAfterInterest = amount.subtract(interestPaid);
        BigDecimal principalPaid = remainingAfterInterest.min(principal);

        debt.setAccruedInterest(interest.subtract(interestPaid));
        debt.setRemainingPrincipal(principal.subtract(principalPaid));
        debt.setLastPaymentDate(request.getPaymentDate());

        debtStateService.moveNextDueDate(debt, request.getAmount());
        debtStateService.refreshDebtStatus(debt);

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            debt.setPaidOffAt(LocalDateTime.now());
        }
        debtRepository.save(debt);

        Payment payment = paymentMapper.toEntity(request, debt);
        payment.setInterestPaid(interestPaid);
        payment.setPrincipalPaid(principalPaid);

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UserDetails userDetails, Long id) {
        User user = getUserByUsername(userDetails.getUsername());
        Payment payment = paymentRepository.findByIdAndDebtUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toResponse(payment);
    }

    private static final Set<String> ALLOWED_PAYMENT_SORT_FIELDS =
            Set.of("paymentDate", "amount", "createdAt");

    @Transactional(readOnly = true)
    public PagedResponse<PaymentListItem> getPayments(
            UserDetails userDetails,
            Long debtId,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        User user = getUserByUsername(userDetails.getUsername());
        debtRepository.findByIdAndUserIdAndDeletedFalse(debtId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

        String resolvedSortBy = ALLOWED_PAYMENT_SORT_FIELDS.contains(sortBy) ? sortBy : "paymentDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int resolvedSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, resolvedSize, Sort.by(direction, resolvedSortBy));

        Page<Payment> paymentPage = paymentRepository.findByDebtIdAndUserId(debtId, user.getId(), pageable);

        List<PaymentListItem> content = new ArrayList<>();
        for (Payment payment : paymentPage.getContent()) {
            content.add(paymentMapper.toListItem(payment));
        }

        return PagedResponse.of(
                content,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isLast());
    }

    @Transactional(readOnly = true)
    public List<PaymentListItem> getAllPayments(UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        List<Payment> payments = paymentRepository.findAllByUserId(user.getId());

        List<PaymentListItem> items = new ArrayList<>();
        for (Payment payment : payments) {
            PaymentListItem item = paymentMapper.toListItem(payment);
            items.add(item);
        }
        return items;
    }


    private void validatePaymentDate(LocalDate paymentDate, Debt debt) {
        if (paymentDate.isAfter(LocalDate.now())
                || paymentDate.isBefore(debt.getStartDate())
                || (debt.getLastPaymentDate() != null
                && paymentDate.isBefore(debt.getLastPaymentDate()))) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_DATE);
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
