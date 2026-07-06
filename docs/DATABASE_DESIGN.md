# **Sơ đồ quan hệ thực thể (ERD)**

![ERD](images/erd.png)

## Quan hệ giữa các bảng

- Một **User** có thể có nhiều **Debt** (1:N)
- Một **Debt** có nhiều **Payment** (1:N)
- Một **Debt** chỉ có một **InterestConfig** (1:1)

## Ghi chú
- Payment dùng để cập nhật số tiền gốc và lãi còn lại của khoản nợ.
- InterestConfig dùng để cấu hình cách tính lãi cho từng khoản nợ.