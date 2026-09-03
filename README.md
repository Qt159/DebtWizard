# DebtWizard

**DebtWizard** là nền tảng quản lý nợ cá nhân và hỗ trợ ra quyết định tài chính (Financial Decision Support System).

Hệ thống giúp người dùng tập trung quản lý các khoản nợ, tự động tính lãi, ghi nhận thanh toán, đánh giá sức khỏe tài chính và mô phỏng các chiến lược trả nợ dựa trên thu nhập và ngân sách thực tế.

> **Mục tiêu của project:** xây dựng một backend có business logic thực tế, tập trung vào thiết kế hệ thống, xử lý dữ liệu tài chính, tối ưu truy vấn và khả năng mở rộng thay vì chỉ tập trung vào CRUD.

---

## Tổng quan

DebtWizard được xây dựng theo hướng **backend-first**, sử dụng Java và Spring Boot.

Các bài toán chính của hệ thống:

* Quản lý vòng đời khoản nợ.
* Tính và cộng dồn lãi suất theo thời gian.
* Xử lý giao dịch thanh toán và phân bổ tiền trả.
* Mô phỏng nhiều chiến lược trả nợ.
* Phân tích sức khỏe tài chính.
* Xây dựng kế hoạch trả nợ dựa trên ngân sách.
* Xử lý notification theo hướng event-driven.
* Triển khai backend trên AWS.

---

## Kiến trúc

DebtWizard sử dụng **Feature-based Architecture**, trong đó mỗi feature chịu trách nhiệm cho một nhóm business capability riêng.

```text
src/main/java/com/tuan/debtwizard/

├── config/
├── dto/
├── exception/
│
└── features/
    ├── auth/
    ├── user/
    ├── financeprofile/
    ├── debt/
    ├── payment/
    ├── planning/
    ├── analysis/
    ├── dashboard/
    ├── notification/
    └── event/
```

Luồng xử lý chính:

```text
Client
   │
   ▼
REST API
   │
   ▼
Spring Boot
   │
   ├── Auth
   ├── Debt
   ├── Payment
   ├── Planning
   ├── Analysis
   ├── Dashboard
   └── Notification
   │
   ▼
PostgreSQL / Amazon RDS
```

Các domain event được sử dụng để tách notification khỏi business flow chính.

---

## Tính năng chính

| Module              | Chức năng                                                         |
| ------------------- | ----------------------------------------------------------------- |
| **Auth**            | Đăng nhập, JWT Access Token, Refresh Token Rotation               |
| **User**            | Quản lý hồ sơ người dùng và thay đổi mật khẩu                     |
| **Finance Profile** | Quản lý thu nhập và chi phí thiết yếu hàng tháng                  |
| **Debt**            | Quản lý nhiều khoản nợ, soft-delete và cấu hình lãi suất          |
| **Interest**        | Tính lãi theo `FLAT` và `REDUCING_BALANCE`                        |
| **Payment**         | Ghi nhận thanh toán và phân bổ tiền trả theo Interest-First       |
| **Planning**        | Mô phỏng và xây dựng kế hoạch trả nợ                              |
| **Analysis**        | Phân tích DTI, gánh nặng lãi vay, nợ quá hạn và thời gian sạch nợ |
| **Dashboard**       | Tổng hợp các chỉ số tài chính quan trọng                          |
| **Notification**    | Thông báo thanh toán và nhắc nợ                                   |
| **Event**           | Domain events phục vụ kiến trúc event-driven                      |

---

## Điểm nổi bật về kỹ thuật

### 1. Planning & Simulation Engine

DebtWizard có một simulation engine chạy **in-memory** để mô phỏng quá trình trả nợ theo từng tháng.

Hệ thống hỗ trợ hai chiến lược:

* `MINIMIZE_INTEREST`: ưu tiên khoản nợ có chi phí lãi cao hơn.
* `IMPROVE_CASHFLOW`: ưu tiên cải thiện dòng tiền và giải phóng ngân sách trả nợ.

Simulation engine xử lý:

* Tính lãi hàng tháng.
* Phân bổ tiền trả.
* Extra payment.
* Giải phóng dòng tiền sau khi khoản nợ được tất toán.
* So sánh kết quả giữa các chiến lược.
* Lưu kế hoạch trả nợ chi tiết.

Việc sử dụng Strategy Pattern giúp các thuật toán trả nợ có thể được mở rộng mà không cần thay đổi simulation engine.

Xem chi tiết: [Planning & Simulation Engine](docs/PLANNING_SIMULATION.md)

---

### 2. Payment & Transaction Processing

Payment module xử lý các nghiệp vụ liên quan đến giao dịch thanh toán:

```text
Payment
   │
   ├── Allocate to accrued interest
   │
   ├── Remaining amount → Principal
   │
   └── Update debt status
```

Hệ thống áp dụng nguyên tắc **Interest-First Allocation**, đảm bảo tiền thanh toán được phân bổ cho phần lãi phát sinh trước khi giảm dư nợ gốc.

Sau giao dịch, trạng thái khoản nợ được cập nhật dựa trên tình trạng thực tế:

* `ACTIVE`
* `OVERDUE`
* `PAID_OFF`

---

### 3. Database & SQL Optimization

PostgreSQL được sử dụng làm database chính vì dữ liệu tài chính yêu cầu tính nhất quán và quan hệ rõ ràng giữa các entity.

Database được thiết kế với:

* Foreign key constraints.
* Index cho các truy vấn thường xuyên.
* Soft-delete.
* Embedded `InterestSettings`.
* Cascade policies phù hợp với vòng đời dữ liệu.

Các truy vấn quan trọng được phân tích bằng:

```sql
EXPLAIN ANALYZE
```

để đánh giá execution plan và tác động của index.

Xem chi tiết: [Database Design](docs/DATABASE_DESIGN.md)

---

### 4. Interest Calculation

Hệ thống hỗ trợ nhiều phương pháp tính lãi:

* `FLAT`
* `REDUCING_BALANCE`

Logic tính lãi được tách thông qua **Strategy Pattern**, giúp business logic dễ mở rộng khi bổ sung thêm phương pháp tính lãi.

Ngoài ra, `DebtScheduler` chịu trách nhiệm xử lý việc cộng dồn lãi phát sinh theo thời gian.

---

### 5. Authentication & Security

Hệ thống sử dụng:

* Spring Security.
* JWT Access Token.
* Refresh Token.
* Refresh Token Rotation.
* BCrypt password hashing.
* Authentication và authorization ở API layer.

Refresh token rotation giúp giảm rủi ro khi refresh token bị lộ hoặc bị sử dụng lại.

---

### 6. Event-Driven Notification

Các sự kiện liên quan đến payment được tách khỏi notification flow.

Ví dụ:

```text
PaymentCompleted
       │
       ▼
 Domain Event
       │
       ▼
 Notification Processing
```

Kiến trúc này giúp business logic của Payment không bị phụ thuộc trực tiếp vào cách notification được xử lý.

Hệ thống được thiết kế để có thể mở rộng sang asynchronous processing với AWS SQS/Lambda.

---

## Engineering Decisions

| Quyết định                     | Lý do                                                                 |
| ------------------------------ | --------------------------------------------------------------------- |
| **Feature-based Architecture** | Tách business capability và giảm coupling giữa các module             |
| **Strategy Pattern**           | Cho phép mở rộng các thuật toán tính lãi và trả nợ                    |
| **PostgreSQL**                 | Phù hợp với dữ liệu tài chính có quan hệ và yêu cầu consistency       |
| **In-memory Simulation**       | Tránh ghi dữ liệu trung gian không cần thiết trong quá trình mô phỏng |
| **Database Indexing**          | Tối ưu các truy vấn payment/debt thường xuyên                         |
| **Domain Events**              | Tách notification khỏi payment business flow                          |
| **Soft Delete**                | Giữ lại lịch sử dữ liệu tài chính thay vì xóa vật lý                  |

---

## Công nghệ sử dụng

| Thành phần                | Công nghệ                           |
| ------------------------- | ----------------------------------- |
| **Ngôn ngữ**              | Java 17                             |
| **Backend Framework**     | Spring Boot 3.2.4                   |
| **Security**              | Spring Security 6                   |
| **Authentication**        | JJWT 0.12.5, JWT                    |
| **Database**              | PostgreSQL 14+                      |
| **ORM / Data Access**     | Spring Data JPA / Hibernate         |
| **API Documentation**     | SpringDoc OpenAPI 2.5.0             |
| **Build Tool**            | Apache Maven                        |
| **Cloud**                 | AWS EC2, Amazon RDS, VPC            |
| **Database Optimization** | PostgreSQL Index, `EXPLAIN ANALYZE` |

---

## Cấu trúc mã nguồn

```text
src/main/java/com/tuan/debtwizard/

├── config/
│   └── Security, OpenAPI, Web configuration
│
├── dto/
│   └── Shared DTOs, ApiResponse<T>
│
├── exception/
│   └── Global exception handling & business error codes
│
└── features/
    ├── auth/
    │   └── Authentication, JWT, Refresh Token
    │
    ├── user/
    │   └── User profile & password management
    │
    ├── financeprofile/
    │   └── Income & essential expenses
    │
    ├── debt/
    │   └── Debt management & interest calculation
    │
    ├── payment/
    │   └── Payment processing & allocation
    │
    ├── planning/
    │   └── SimulationEngine & repayment strategies
    │
    ├── analysis/
    │   └── Financial health analysis
    │
    ├── dashboard/
    │   └── Financial overview aggregation
    │
    ├── notification/
    │   └── In-app notifications & reminders
    │
    └── event/
        └── Domain events & event publishing
```

---

## Database Design

Database sử dụng PostgreSQL với các entity chính phục vụ:

* Users.
* Debts.
* Payments.
* Interest configuration.
* Financial profiles.
* Planning và simulation results.
* Notification.

Sơ đồ ERD và database design chi tiết:

[Database Design](docs/DATABASE_DESIGN.md)

---

## API Documentation

API được document bằng **Swagger / OpenAPI**.

Sau khi chạy ứng dụng:

```text
http://localhost:8080/swagger-ui/index.html
```

Quy trình authentication:

```text
POST /api/auth/login
        │
        ▼
   accessToken
        │
        ▼
Swagger → Authorize
        │
        ▼
Bearer <access-token>
```

Project cũng cung cấp Postman collection và environment mẫu:

```text
postman/
├── DebtWizard.postman_collection.json
└── DebtWizard.postman_environment.json
```

---

## Getting Started

### Yêu cầu

* Git
* Java 17 JDK
* Apache Maven 3.8+
* PostgreSQL 14+

### 1. Clone project

```bash
git clone https://github.com/Qt159/DebtWizard.git
cd DebtWizard
```

### 2. Tạo database

```sql
CREATE DATABASE debtwizard;
```

### 3. Cấu hình environment variables

Tạo file `.env` tại thư mục gốc:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=debtwizard
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
```

### 4. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Backend chạy mặc định tại:

```text
http://localhost:8080
```

---

## Deployment

Backend được triển khai trên AWS với kiến trúc:

```text
Internet
   │
   ▼
AWS EC2
   │
   │ Private connection
   ▼
Amazon RDS PostgreSQL
```

Infrastructure sử dụng:

* Amazon EC2.
* Amazon RDS PostgreSQL.
* VPC.
* Subnets.
* Security Groups.
* Linux `systemd`.

Chi tiết deployment:

[Deployment Guide](docs/DEPLOYMENT.md)

---

## Project Documentation

| Tài liệu                                                        | Nội dung                                                                                   |
| --------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| [**SRS**](docs/SRS.md)                                          | Functional requirements, non-functional requirements, business rules và validation rules   |
| [**SAD**](docs/SAD.md)                                          | Architecture, feature-based design, event-driven architecture, security và design patterns |
| [**Database Design**](docs/DATABASE_DESIGN.md)                  | ERD, database schema, constraints, indexes và data lifecycle                               |
| [**Deployment Guide**](docs/DEPLOYMENT.md)                      | AWS infrastructure và deployment process                                                   |
| [**Planning & Simulation Engine**](docs/PLANNING_SIMULATION.md) | Simulation algorithm, repayment strategies và financial calculation                        |

---

## Roadmap

Một số hướng phát triển tiếp theo:

* [ ] Hoàn thiện database query optimization và benchmark.
* [ ] Bổ sung integration testing cho các transaction flow quan trọng.
* [ ] Cải thiện observability và structured logging.
* [ ] Caching cho các dữ liệu được truy cập thường xuyên.
* [ ] Hoàn thiện asynchronous notification processing.
* [ ] CI/CD pipeline.
* [ ] Containerization.

---