# DebtWizard

DebtWizard là nền tảng quản lý nợ cá nhân, hỗ trợ người dùng theo dõi lịch sử thanh toán, tính lãi tự động và xây dựng kế hoạch trả nợ tối ưu dựa trên phân tích tình hình tài chính thực tế.

## Features

- **Quản lý khoản nợ**: tạo, cập nhật và theo dõi nhiều khoản nợ (BANKING, PERSONAL_LOAN, CREDIT).
- **Theo dõi thanh toán**: ghi nhận các khoản thanh toán, cập nhật tiến độ trả nợ và xử lý phân bổ thanh toán ưu tiên phần lãi trước phần gốc.
- **Tính lãi tự động**: hỗ trợ 2 phương pháp FLAT và REDUCING_BALANCE, tự động accrual hàng ngày thông qua Scheduler.
- **Dashboard**: tổng hợp thông tin tài chính gồm danh sách khoản nợ, dư nợ gốc, lãi phát sinh và tổng số tiền phải trả.
- **Phân tích tài chính**: đánh giá sức khỏe tài chính qua 4 chỉ số định lượng — DTI, tỷ lệ gánh nặng lãi vay, tỷ lệ nợ quá hạn và thời gian dự kiến trả hết nợ.
- **Lập kế hoạch trả nợ**: mô phỏng và so sánh 2 chiến lược (Minimize Interest / Improve Cashflow), lưu kế hoạch đã chọn cùng lịch thanh toán chi tiết.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.4 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | JWT (JJWT 0.12.5) |
| Authorization | Spring Security |
| API Docs | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Build Tool | Maven |
| Cloud | AWS EC2, Amazon RDS, VPC |

## Prerequisites

- Git
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Deployment

Backend được triển khai trên AWS.  
Chi tiết kiến trúc và các bước triển khai:

- [Deployment Guide](docs/DEPLOYMENT.md)

## Getting Started

### 1. Clone Repository

```bash
git clone https://github.com/Qt159/DebtWizard.git
cd DebtWizard
```

### 2. Create Database
```bash
CREATE DATABASE debtwizard;
```

### 3. Configure Environment Variables
Tạo file .env tại thư mục gốc:
```bash
# Database Configuration (Required)
DB_PASSWORD=your_postgres_password

# JWT Configuration (Required)
JWT_SECRET=your_jwt_secret_key_at_least_32_chars

# Database Configuration (Optional - có default)
DB_HOST=localhost                # Default: localhost
DB_PORT=5432                     # Default: 5432
DB_NAME=debtwizard              # Default: debtwizard
DB_USERNAME=postgres            # Default: postgres

# JWT Configuration (Optional - có default)
JWT_ACCESS_EXPIRATION=900000     # Default: 900000 (15 phút)
JWT_REFRESH_EXPIRATION=604800000 # Default: 604800000 (7 ngày)
```

**Lưu ý:**
- `DB_PASSWORD` và `JWT_SECRET` **bắt buộc** phải cấu hình
- Các biến còn lại có giá trị mặc định, chỉ thay đổi khi cần
- Trong production: `JWT_SECRET` phải là chuỗi ngẫu nhiên mạnh (>32 ký tự)
- Database credentials phải khớp với cấu hình PostgreSQL của bạn
### 4. Run Application
```bash
mvn spring-boot:run
```
Application chạy tại:
http://localhost:8080

## API Documentation
Swagger UI:
http://localhost:8080/swagger-ui/index.html
Authentication flow:
Gọi POST /api/auth/login để lấy access token.
Nhấn Authorize trên Swagger UI.
Nhập: Bearer <access-token>

## Project Structure
src/main/java/com/tuan/debtwizard/
├── config/          # Security, OpenAPI, application configuration
├── dto/             # Shared DTOs
├── exception/       # Global exception handling
└── features/
├── auth/        # Authentication, JWT, refresh token
├── user/        # User management
├── debt/        # Debt management and interest calculation
├── payment/     # Payment tracking
├── planning/    # Repayment simulation and planning
├── analysis/    # Financial analysis
└── dashboard/   # Financial overview

## Environment Variables
| Variable      | Description                | Required |
| ------------- | -------------------------- | -------- |
| `DB_PASSWORD` | PostgreSQL password        | Yes      |
| `JWT_SECRET`  | Secret key for signing JWT | Yes      |

## Testing the API
Dự án cung cấp Postman Collection trong package postman:
- DebtWizard.postman_collection.json
- DebtWizard.postman_environment.json

Import hai file trên vào Postman để kiểm thử API.
## Documentation
Các tài liệu thiết kế và kỹ thuật:
    - [System Architecture Document (SAD)](docs/SAD.md)
    - [Database Design](docs/DatabaseDesign.md)
    - [Business Rules](docs/BusinessRules.md)
    - [Validation Rules](docs/ValidationRules.md)
    - [Algorithm](docs/Algorithm.md)
    - [Deployment Guide](docs/DEPLOYMENT.md)
    

