# Deployment Guide — DebtWizard

## 1. Deployment Overview

DebtWizard Backend được triển khai trên AWS với kiến trúc:


Hệ thống chỉ triển khai Backend và Database.

- Backend: Spring Boot Application chạy trên Amazon EC2.
- Database: PostgreSQL chạy trên Amazon RDS.
- EC2 chịu trách nhiệm xử lý REST API và kết nối đến RDS thông qua JDBC.

---

# 2. AWS Infrastructure Setup

## 2.1 Create VPC

Tạo một Custom VPC để quản lý network của hệ thống.

Example:

```
VPC CIDR:
10.0.0.0/16
```

---

## 2.2 Create Subnets

Hệ thống sử dụng 3 Subnets:

### Public Subnet

Mục đích:

- Chạy EC2 Instance.
- Cho phép SSH truy cập từ Internet.

Architecture:

```text
Public Subnet
      |
      |
     EC2
```

---

### Private Subnets

Mục đích:

- Chứa Amazon RDS PostgreSQL.
- Không expose Database trực tiếp ra Internet.

Amazon RDS yêu cầu DB Subnet Group phải có tối thiểu 2 Subnets thuộc 2 Availability Zones khác nhau.

Architecture:

```text
Private Subnet A (AZ-a)

Private Subnet B (AZ-b)

          |
          |
    DB Subnet Group

          |
          |

Amazon RDS PostgreSQL
```

---

## 2.3 Internet Gateway

Tạo và attach Internet Gateway vào VPC.

Route Table cho Public Subnet:

```text
Destination        Target

0.0.0.0/0          Internet Gateway
```

Route này cho phép EC2 truy cập Internet để cài đặt package và pull source code.

---

# 3. Security Group Configuration

## 3.1 EC2 Security Group

Security Group kiểm soát traffic đến Backend Server.

Inbound Rules:

| Type       | Port | Source |
|------------|----|--------|
| SSH        | 22 | My IP |
| HTTP       | 80 |  |
| Custom TCP | 8080 | Required source |

Outbound:

```
Allow all traffic
```

---

## 3.2 RDS Security Group

Security Group bảo vệ Database.

Inbound Rules:

| Type | Port | Source |
|------|------|--------|
| PostgreSQL | 5432 | EC2 Security Group |

Chỉ EC2 Instance được phép kết nối đến RDS.

---

# 4. Launch EC2 Instance

Tạo EC2 Instance:

Configuration:

- OS: Ubuntu
- Instance Type: Free Tier compatible
- Subnet: Public Subnet
- Security Group: EC2 Security Group

Sau khi tạo EC2, sử dụng SSH Key Pair để truy cập server.

---

# 5. Create Amazon RDS PostgreSQL

Tạo PostgreSQL Database trên Amazon RDS.

Configuration:

- Engine: PostgreSQL
- VPC: Custom VPC
- Public Access: No
- DB Subnet Group:
    - Private Subnet A
    - Private Subnet B
- Security Group: RDS Security Group

Sau khi tạo thành công, lấy thông tin:

```
RDS Endpoint
Port: 5432
```

---

# 6. Connect to EC2

SSH vào EC2:

```bash
ssh -i <key.pem> ubuntu@<ec2-public-ip>
```

---

# 7. Install Required Dependencies

Update package:

```bash
sudo apt update
```

## Install Git

```bash
sudo apt install git
```

Verify:

```bash
git --version
```

---

## Install Java

```bash
sudo apt install openjdk-17-jre-headless
```

Verify:

```bash
java -version
```

---

## Install Maven

```bash
sudo apt install maven
```

Verify:

```bash
mvn -version
```

---

## Install PostgreSQL Client

Cài PostgreSQL Client để kết nối đến Amazon RDS:

```bash
sudo apt install postgresql-client
```

Không cần cài PostgreSQL Server trên EC2 vì Database đã được triển khai trên Amazon RDS.

---

# 8. Initialize Database

Kết nối đến RDS:

```bash
psql -h <rds-endpoint> -U postgres -d postgres
```

Tạo database:

```sql
CREATE DATABASE debtwizard;
```

Kiểm tra database:

```sql
\l
```

---

# 9. Clone Backend Source Code

Clone repository:

```bash
git clone <repository-url>
```

Di chuyển vào project:

```bash
cd DebtWizard
```

---

# 10. Configure Application

Cấu hình Database Connection trong Spring Boot.

Example:

```properties
spring.datasource.url=jdbc:postgresql://<rds-endpoint>:5432/debtwizard

spring.datasource.username=<username>

spring.datasource.password=<password>
```

Trong môi trường Production, các thông tin nhạy cảm nên được quản lý bằng Environment Variables.

---

# 11. Build Application

Build project bằng Maven:

```bash
mvn clean package
```

Sau khi build thành công:

```
target/*.jar
```

được tạo ra.

---

# 12. Run Application

Chạy Spring Boot Application:

```bash
java -jar target/DebtWizard.jar
```

Backend chạy tại:

```
http://<ec2-public-ip>:8080
```

---

# 13. Verify Deployment

Kiểm tra Application:

```bash
curl http://localhost:8080
```

Kiểm tra:

- Spring Boot Application khởi động thành công.
- EC2 kết nối được với RDS.
- REST API hoạt động bình thường.

---

# 14. Future Improvements

- Dockerize Application.
- Docker Compose cho local development.
- CI/CD với GitHub Actions.
- Nginx Reverse Proxy.
- HTTPS với SSL Certificate.
- AWS Secrets Manager.
- CloudWatch Monitoring.