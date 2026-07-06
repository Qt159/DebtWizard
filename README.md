# DebtWizard

DebtWizard là nền tảng quản lý nợ cá nhân trên nền tảng web, giúp người dùng quản lý các khoản nợ, theo dõi lịch sử thanh toán và xây dựng kế hoạch trả nợ dựa trên phân tích tình hình tài chính.

## Features

- Quản lý khoản nợ: tạo, cập nhật và theo dõi nhiều khoản nợ.
- Theo dõi thanh toán: ghi nhận các khoản thanh toán và theo dõi tiến độ trả nợ.
- Tính lãi suất: tự động tính lãi cho từng khoản nợ.
- Dashboard: hiển thị danh sách khoản nợ cùng trạng thái, dư nợ gốc còn lại, lãi phát sinh và tổng số tiền phải trả.
- Phân tích tài chính: đánh giá tình hình tài chính dựa trên 4 chỉ số: tỷ lệ DTI (Debt-to-Income), tỷ lệ gánh nặng lãi vay, tỷ lệ nợ quá hạn và thời gian dự kiến trả hết nợ; mỗi chỉ số đều đi kèm mức đánh giá và khuyến nghị.
- Lập kế hoạch trả nợ: tạo kế hoạch và lịch trình trả nợ phù hợp.
- Đánh giá sức khỏe tài chính: phân loại tình trạng tài chính dựa trên các chỉ số về nợ.
- Gamification (Dự kiến phát triển).

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.4 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | JWT (Bearer Token) |
| Authorization | Spring Security |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Maven |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository

```bash
git clone https://github.com/Qt159/DebtWizard.git
cd DebtWizard
```

### 2. Create the database

```sql
CREATE DATABASE debtwizard;
```

### 3. Configure environment variables

Tạo file .env tại thư mục gốc của project:

```env
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_jwt_secret_key
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Tất cả các API yêu cầu xác thực đều sử dụng Bearer Token.
Trước tiên, đăng nhập thông qua endpoint:
/api/auth/login

Sau đó nhấn Authorize trên Swagger UI và nhập Bearer Token để sử dụng các API.

## Project Structure

```
src/main/java/com/tuan/debtwizard/
├── config/          # Security, OpenAPI, and app configuration
├── dto/             # Shared DTOs (ApiResponse, etc.)
├── exception/       # Global exception handling and error codes
└── features/
    ├── auth/        # Registration, login, JWT filter
    ├── user/        # User profile management
    ├── debt/        # Debt CRUD operations
    ├── payment/     # Payment recording and tracking
    ├── planning/    # Repayment planning and scheduling
    └── analysis/    # Financial health analysis and DTI
```

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `DB_PASSWORD` | PostgreSQL password | Yes |
| `JWT_SECRET` | Secret key for signing JWT tokens | Yes |

## Documentation

Additional docs are in the `docs/` folder:

- [System Architecture (SAD)](docs/SAD.md)
- [Entity Relationship Diagram (ERD)](docs/ERD.md)
- [Business Validation Algorithm](docs/business_validation_algorithm.md)

## Testing the API

Dự án cung cấp sẵn bộ sưu tập Postman:

- `DebtWizard.postman_collection.json`
- `DebtWizard.postman_environment.json`

Chỉ cần import cả hai tệp vào Postman để bắt đầu kiểm thử các API.
