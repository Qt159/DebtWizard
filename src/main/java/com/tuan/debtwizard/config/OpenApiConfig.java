package com.tuan.debtwizard.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "DebtWizard API",
                version = "1.1.0",
                description = """
                DebtWizard là hệ thống RESTful hỗ trợ quản lý nợ cá nhân và lập kế hoạch trả nợ.

                ## Chức năng chính
                    - Quản lý khoản nợ và cấu hình lãi suất.
                    - Ghi nhận thanh toán và tự động phân bổ tiền vào lãi/gốc.
                    - Mô phỏng và so sánh nhiều chiến lược trả nợ.
                    - Phân tích sức khỏe tài chính (DTI, Interest Ratio, Overdue Ratio, Estimated Payoff).
                    - Theo dõi tổng quan tài chính qua Dashboard.
                    - Scheduler tự động tính lãi hằng ngày.

                ## Authentication
                    Ngoại trừ các API đăng ký, đăng nhập và refresh token,
                    tất cả endpoint đều yêu cầu Bearer JWT Token.

                    Authorization: Bearer <access_token>

                ## Business Notes
                    - Chỉ khoản nợ ACTIVE được đưa vào mô phỏng.
                    - Extra Payment không được vượt quá ngân sách khả dụng.
                    - Scheduler chạy hằng ngày để cập nhật lãi và trạng thái khoản nợ.
                    """,
                contact = @Contact(name = "Tuan Pham") ),
        servers = { @Server(url = "http://13.212.48.231:8080", description = "Production Server"),
                    @Server(url = "http://localhost:8080", description = "Local Development Server") } )
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" )
public class OpenApiConfig { }