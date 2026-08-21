1. Current Architecture

Payment service -> Notification service -> Notification Repository -> PostgreSql
Scheduler -> Notification Service -> PostgreSql

Hạn chế:
- Payment Service phụ thuộc trực tiếp vào Notification Service.
- Notification Service đang nhận trực tiếp Entity (Payment, Debt).
- Notification processing đang nằm trong cùng application/transaction flow với business operation.
- referenceKey hiện chỉ được kiểm tra bằng existsByReferenceKey() nhưng chưa có database-level unique constraint.
- Notification có khả năng bị duplicate trong trường hợp concurrent processing.
- Scheduler chạy bên trong application instance thông qua @Scheduled.
- Khi application chạy nhiều instances, mỗi instance có thể cùng thực hiện scheduler.
  Notification delivery chưa có retry / dead-letter handling.
  Chưa hỗ trợ email notification qua Amazon SES.

2. Problems and goals

Problems:
- Payment Service gọi trực tiếp Notification Service: Điều này khiến Payment phải biết Notification tồn tại và phụ thuộc vào khả năng xử lý của Notification.
- Nếu notification được xử lý trong cùng transaction:Notification là side effect và không nên quyết định transaction thành công/thất bại của Payment.
- Hiện tại notification nhận trực tiếp domain entity: Điều này tạo coupling giữa Notification Module và Debt Module. 
Thay vào đó, Notification nên nhận event chứa những dữ liệu cần thiết: PaymentCompletedEvent, PaymentReminderEvent
- Có khả năng bị duplicate notification
- Scheduler trên application instance: Nếu deploy: EC2 #1, EC2 #2, EC2 #3  thì cả 3 instance có thể cùng chạy scheduler.

Goals:
- Gửi email notification.
- Tạo in-app notification.
- Hỗ trợ payment completed notification.
- Hỗ trợ payment reminder notification.

3. Target Architecture

Payment Service -> PaymentCompleted Event -> SQS -> Lambda -> Notification DB
                                                        |-> SES
Payment chỉ cần publish event.
Payment không cần biết:
- Notification Service xử lý như thế nào.
- Notification lưu ở đâu.
- Email được gửi bằng gì.
- Notification có retry hay không.

Scheduler -> PaymentReminderEvent -> SQS -> Lambda

EventBridge Scheduler: Trigger process theo schedule.
Reminder Lambda: Tìm các khoản nợ sắp đến hạn. Tạo PaymentReminderEvent. Publish event vào SQS.
Notification Lambda: Consume event. Create in-app notification. Send email. Handle failure/retry.
4. Event design
Event:
   {
   "eventId": "uuid",
   "eventType": "PAYMENT_COMPLETED",
   "paymentId": 123,
   "userId": 10,
   "debtId": 40,
   "amount": 100000,
   "occurredAt": "2026-08-17T15:30:00Z"
   }

   | Field        | Purpose                     |
   | ------------ | --------------------------- |
   | `eventId`    | Unique identifier của event |
   | `eventType`  | Xác định loại event         |
   | `paymentId`  | Payment liên quan           |
   | `userId`     | User nhận notification      |
   | `debtId`     | Debt liên quan              |
   | `amount`     | Số tiền thanh toán          |
   | `occurredAt` | Thời điểm event xảy ra      |

5. Idempotency Design
- referenceKey phải được ở database level.
- Nếu cùng một event được xử lý nhiều lần:
SQS
│ 
├── Event A 
├── Event A (retry) 
└── Event A (duplicate)
  thì notification database vẫn chỉ có một notification.
6. Failure Handling
   SQS cho phép notification xử lý bất đồng bộ.
   SQS
   │
   ▼
   Lambda
   │
   ├── Success → Delete message
   │
   └── Failure
   │
   ▼
   Retry
   │
   ▼
   Retry
   │
   ▼
   DLQ

Nếu SES tạm thời lỗi hoặc Lambda gặp exception:

Payment
│
▼
PaymentCompletedEvent
│
▼
SQS
│
▼
Notification Lambda
│
X
│
▼
Retry

Payment transaction không bị rollback.

7. Data design
   Notification
--------------------------
id
userId
title
message
type
isRead
referenceKey UNIQUE
createdAt
deleted

Có thể cân nhắc chuyển Notification DB sang DynamoDB nếu muốn tách hẳn serverless notification workload.

8. Technology
   | Component           | Technology            |
   | ------------------- | --------------------- |
   | Main Backend        | Spring Boot           |
   | Main Database       | PostgreSQL / RDS      |
   | Event Queue         | Amazon SQS            |
   | Notification Worker | AWS Lambda            |
   | Reminder Scheduler  | EventBridge Scheduler |
   | Email               | Amazon SES            |
   | Notification DB     | PostgreSQL initially  |
   | Retry               | SQS                   |
   | Failed Messages     | SQS DLQ               |
   | Monitoring          | CloudWatch            |




