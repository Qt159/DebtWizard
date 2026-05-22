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

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),

    // Debt
    DEBT_NOT_FOUND(HttpStatus.NOT_FOUND, "Khoản nợ không tồn tại"),
    DEBT_DELETED(HttpStatus.BAD_REQUEST, "Khoản nợ đã bị xóa"),
    DEBT_ALREADY_PAID_OFF(HttpStatus.BAD_REQUEST, "Khoản nợ đã tất toán"),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Thanh toán không tồn tại"),
    PAYMENT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "Số tiền vượt quá dư nợ"),

    // General validation
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "Vi phạm ràng buộc dữ liệu"),
    INVALID_OAUTH2_USER(HttpStatus.UNAUTHORIZED, "OAuth2 không hợp lệ"),


    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"Không có quyền truy cập" ),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ" );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}