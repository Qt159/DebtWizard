package com.tuan.debtwizard.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // System
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi không xác định"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống nội bộ"),
    INVALID_KEY(HttpStatus.BAD_REQUEST, "Mã lỗi không hợp lệ"),

    // Auth
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Chưa xác thực, vui lòng đăng nhập"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Không có quyền truy cập"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu không chính xác"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),

    // Debt
    DEBT_NOT_FOUND(HttpStatus.NOT_FOUND, "Khoản nợ không tồn tại"),
    DEBT_DELETED(HttpStatus.BAD_REQUEST, "Khoản nợ đã bị xóa"),
    DEBT_ALREADY_PAID_OFF(HttpStatus.BAD_REQUEST, "Khoản nợ đã tất toán"),
    DEBT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Nợ không active"),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Thanh toán không tồn tại"),
    PAYMENT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "Số tiền vượt quá dư nợ"),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    INVALID_PAYMENT_RULE(HttpStatus.BAD_REQUEST, "PaymentApplicationRule is invalid"),
    INVALID_PAYMENT_STRATEGY(HttpStatus.BAD_REQUEST, "PaymentStrategy is invalid"),
    INVALID_PAYMENT_DATE(HttpStatus.BAD_REQUEST, "PaymentDate is invalid"),

    // Planning
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy kế hoạch đã lưu"),
    EXTRA_PAYMENT_EXCEEDS_MAX(HttpStatus.BAD_REQUEST, "Số tiền trả thêm vượt quá ngưỡng tối đa cho phép"),

    // General validation
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "Vi phạm ràng buộc dữ liệu"),
    INVALID_OAUTH2_USER(HttpStatus.UNAUTHORIZED, "OAuth2 không hợp lệ"),
    NEW_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu cũ"),
    INTEREST_CALCULATION_METHOD_NULL(HttpStatus.BAD_REQUEST,"Phương thức tính lãi không thể bỏ trống" );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}