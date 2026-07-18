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
                version = "1.0.0",
                description = "Biến áp lực tài chính thành lộ trình tự do.\n\n" +
                        "DebtWizard API cung cấp hệ thống dịch vụ RESTful hoàn chỉnh giúp người dùng làm chủ tài chính cá nhân thông qua các tính năng cốt lõi:\n\n" +
                        "* Quản lý nợ thông minh: Theo dõi chi tiết các khoản nợ, chủ nợ, kỳ hạn và biến động lãi suất.\n" +
                        "* Thanh toán: Thiết lập lịch trình trả nợ định kỳ, tránh phát sinh chi phí phạt.\n" +
                        "* Chiến lược tối ưu: Phân tích dòng tiền và gợi ý kế hoạch trả nợ.\n\n" +
                        "---\n" +
                        "*Tất cả các API yêu cầu bảo mật cần đính kèm Bearer Token trong Header trước khi gọi.*",
                contact = @Contact(name = "DebtWizard Team") ),
        servers = { @Server(url = "http://13.212.48.231:8080", description = "Production Server"),
                    @Server(url = "http://localhost:8080", description = "Local Development Server") } )
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" )
public class OpenApiConfig { }