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
                description = "REST API for DebtWizard — quản lý nợ cá nhân, theo dõi thanh toán, phân tích tài chính và lập kế hoạch trả nợ.",
                contact = @Contact(name = "DebtWizard Team") ),
        servers = { @Server(url = "http://13.212.48.231:8080", description = "Production Server"),
                    @Server(url = "http://localhost:8080", description = "Local Development Server") } )
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" )
public class OpenApiConfig { }