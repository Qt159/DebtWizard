package com.tuan.debtwizard.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // System
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi không xác định"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống nội bộ"),

    // Auth
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Chưa xác thực, vui lòng đăng nhập"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Không có quyền truy cập"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng"),
    NEW_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu cũ"),

    // Debt
    DEBT_NOT_FOUND(HttpStatus.NOT_FOUND, "Khoản nợ không tồn tại"),
    DEBT_ALREADY_PAID_OFF(HttpStatus.BAD_REQUEST, "Khoản nợ đã tất toán"),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Thanh toán không tồn tại"),
    PAYMENT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "Số tiền thanh toán vượt quá dư nợ hiện tại"),
    INVALID_PAYMENT_DATE(HttpStatus.BAD_REQUEST, "Ngày thanh toán không hợp lệ"),

    // Planning
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy kế hoạch đã lưu"),
    EXTRA_PAYMENT_EXCEEDS_BUDGET(HttpStatus.BAD_REQUEST, "Số tiền trả thêm vượt quá ngân sách cho phép"),
    PAYMENT_EXCEEDS_DEBT_BALANCE(HttpStatus.BAD_REQUEST, "Số tiền thanh toán vượt quá số dư nợ"),
    SIMULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Quy trình mô phỏng thất bại"),

    // General validation
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "Vi phạm ràng buộc dữ liệu"),
    INVALID_INTEREST_SETTINGS(HttpStatus.BAD_REQUEST, "Thông tin lãi suất không hợp lệ"),
    STRATEGY_MISSING(HttpStatus.BAD_REQUEST, "Vui lòng chọn chiến lược trả nợ"),
    STRATEGY_DUPLICATE(HttpStatus.BAD_REQUEST, "Hai chiến lược phải khác nhau"),
    DUPLICATE_DEBT(HttpStatus.BAD_REQUEST, "Có khoản nợ trùng lặp");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
