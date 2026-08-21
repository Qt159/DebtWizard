-- =============================================================
-- DebtWizard — Seed Data
-- 5 users, 3 debts each, several months of payments
-- Focused on payment scenarios for optimisation testing
--
-- Công thức tham khảo:
--   FLAT:             M = P/n + P*(rate/100/12)
--   REDUCING_BALANCE: M = P*[r(1+r)^n]/[(1+r)^n-1]   r = rate/100/12
--
-- Passwords are BCrypt of "Password1!" (same for all users)
-- =============================================================

-- ---------------------------------------------------------------
-- CLEANUP (chạy lại an toàn)
-- ---------------------------------------------------------------
DELETE FROM payments;
DELETE FROM debts;
DELETE FROM refresh_token;
DELETE FROM users;

-- Reset sequences (PostgreSQL)
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE debts_id_seq RESTART WITH 1;
ALTER SEQUENCE payments_id_seq RESTART WITH 1;

-- ===============================================================
-- USERS
-- monthly_income / monthly_expense được chọn để tạo ra
-- các kịch bản DTI khác nhau (GOOD / WARNING / CRITICAL)
-- ===============================================================
INSERT INTO users (username, email, password, full_name, monthly_income, monthly_expense, created_at, updated_at)
VALUES
  -- User 1: DTI tốt (~22%) — nhân viên văn phòng
  ('alice_nguyen',  'alice@example.com',  '$2a$12$K2Ri8X9Y0Lm3Pq7Vn4Wt.eHgJdFsZxQoAiBcNmTyUvWrOpKlMhSe', 'Nguyễn Thị Alice',  20000000.00, 5000000.00, NOW(), NOW()),
  -- User 2: DTI cảnh báo (~38%) — kỹ sư mới đi làm
  ('bob_tran',      'bob@example.com',    '$2a$12$K2Ri8X9Y0Lm3Pq7Vn4Wt.eHgJdFsZxQoAiBcNmTyUvWrOpKlMhSe', 'Trần Văn Bob',      15000000.00, 4000000.00, NOW(), NOW()),
  -- User 3: DTI nguy hiểm (~55%) — freelancer thu nhập thấp
  ('charlie_le',    'charlie@example.com','$2a$12$K2Ri8X9Y0Lm3Pq7Vn4Wt.eHgJdFsZxQoAiBcNmTyUvWrOpKlMhSe', 'Lê Thị Charlie',    10000000.00, 3000000.00, NOW(), NOW()),
  -- User 4: Thu nhập cao, nợ lớn — kinh doanh
  ('david_pham',    'david@example.com',  '$2a$12$K2Ri8X9Y0Lm3Pq7Vn4Wt.eHgJdFsZxQoAiBcNmTyUvWrOpKlMhSe', 'Phạm Quốc David',   40000000.00, 8000000.00, NOW(), NOW()),
  -- User 5: Mix nợ quá hạn — để test OVERDUE scenario
  ('eva_hoang',     'eva@example.com',    '$2a$12$K2Ri8X9Y0Lm3Pq7Vn4Wt.eHgJdFsZxQoAiBcNmTyUvWrOpKlMhSe', 'Hoàng Thị Eva',     12000000.00, 3500000.00, NOW(), NOW());

-- ===============================================================
-- DEBTS — User 1: alice_nguyen (id=1)
-- 3 khoản: BANKING REDUCING_BALANCE, PERSONAL_LOAN FLAT, CREDIT FLAT
-- Start: 2025-01-15, đã thanh toán tháng 1–5 (5 lần)
-- ===============================================================

-- Debt 1-A: Vay ngân hàng mua xe
--   P=50,000,000  n=36  rate=9%/năm  REDUCING_BALANCE
--   r = 0.09/12 = 0.0075
--   M = 50000000 * 0.0075*(1.0075)^36 / ((1.0075)^36 - 1)
--     = 50000000 * 0.0075*1.30865 / 0.30865 ≈ 1,589,700
--   Sau 5 tháng trả đủ M: remaining_principal ≈ 43,750,000
--   accrued_interest = lãi tháng 6 đến 2025-08-15 (67 ngày)
--   = 43,750,000 * (0.09/365) * 67 ≈ 725,100
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  1, 'VietinBank - Vay mua xe', 50000000.00, 43750000.00,
  1589700.00, 36, '2025-01-15', 15,
  '2025-09-15', '2025-06-15', '2025-08-15',
  725100.00, 'ACTIVE', 'BANKING', false, NULL,
  '2025-01-15 09:00:00', NOW(),
  'REDUCING_BALANCE', 'ANNUALLY', 9.00
);

-- Debt 1-B: Vay cá nhân bạn bè
--   P=10,000,000  n=12  rate=12%/năm  FLAT
--   M = P/n + P*(rate/100/12) = 10000000/12 + 10000000*0.01 = 833,333 + 100,000 = 933,333
--   Sau 5 tháng: remaining = 10000000 - 5*(833333) = 10000000 - 4166665 = 5,833,335
--   accrued_interest = 10000000 * (0.12/365) * 67 ≈ 220,274
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  1, 'Vay bạn - Minh Tú', 10000000.00, 5833335.00,
  933333.00, 12, '2025-01-15', 15,
  '2025-09-15', '2025-06-15', '2025-08-15',
  220274.00, 'ACTIVE', 'PERSONAL_LOAN', false, NULL,
  '2025-01-15 09:00:00', NOW(),
  'FLAT', 'ANNUALLY', 12.00
);

-- Debt 1-C: Thẻ tín dụng
--   P=8,000,000  n=24  rate=18%/năm  FLAT
--   M = 8000000/24 + 8000000*0.015 = 333,333 + 120,000 = 453,333
--   Sau 5 tháng: remaining = 8000000 - 5*333333 = 8000000 - 1666665 = 6,333,335
--   accrued_interest = 8000000 * (0.18/365) * 67 ≈ 264,984
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  1, 'Sacombank - Thẻ tín dụng', 8000000.00, 6333335.00,
  453333.00, 24, '2025-01-15', 15,
  '2025-09-15', '2025-06-15', '2025-08-15',
  264984.00, 'ACTIVE', 'CREDIT', false, NULL,
  '2025-01-15 09:00:00', NOW(),
  'FLAT', 'ANNUALLY', 18.00
);

-- ===============================================================
-- DEBTS — User 2: bob_tran (id=2)
-- Nợ lớn hơn, DTI cảnh báo
-- Start: 2025-02-01, đã thanh toán tháng 2–6 (5 lần)
-- ===============================================================

-- Debt 2-A: Vay mua laptop/thiết bị
--   P=20,000,000  n=24  rate=10.5%/năm  REDUCING_BALANCE
--   r = 0.105/12 = 0.00875
--   M = 20000000 * 0.00875*(1.00875)^24 / ((1.00875)^24 - 1)
--     ≈ 20000000 * 0.00875*1.23272 / 0.23272 ≈ 926,200
--   Sau 5 tháng: remaining ≈ 16,250,000
--   accrued_interest = 16250000 * (0.105/365) * 46 ≈ 214,336
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  2, 'BIDV - Vay tiêu dùng', 20000000.00, 16250000.00,
  926200.00, 24, '2025-02-01', 1,
  '2025-09-01', '2025-07-01', '2025-08-15',
  214336.00, 'ACTIVE', 'BANKING', false, NULL,
  '2025-02-01 10:00:00', NOW(),
  'REDUCING_BALANCE', 'ANNUALLY', 10.50
);

-- Debt 2-B: Vay cá nhân đồng nghiệp
--   P=5,000,000  n=6  rate=8%/năm  FLAT
--   M = 5000000/6 + 5000000*0.00667 = 833,333 + 33,333 = 866,667
--   Sau 5 tháng: remaining = 5000000 - 5*833333 = 5000000 - 4166665 = 833,335
--   accrued_interest = 5000000 * (0.08/365) * 46 ≈ 50,411
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  2, 'Vay đồng nghiệp - Hùng', 5000000.00, 833335.00,
  866667.00, 6, '2025-02-01', 1,
  '2025-09-01', '2025-07-01', '2025-08-15',
  50411.00, 'ACTIVE', 'PERSONAL_LOAN', false, NULL,
  '2025-02-01 10:00:00', NOW(),
  'FLAT', 'ANNUALLY', 8.00
);

-- Debt 2-C: Thẻ tín dụng MB Bank
--   P=15,000,000  n=18  rate=24%/năm  FLAT
--   M = 15000000/18 + 15000000*0.02 = 833,333 + 300,000 = 1,133,333
--   Sau 5 tháng: remaining = 15000000 - 5*833333 = 15000000 - 4166665 = 10,833,335
--   accrued_interest = 15000000 * (0.24/365) * 46 ≈ 454,521
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  2, 'MB Bank - Thẻ tín dụng', 15000000.00, 10833335.00,
  1133333.00, 18, '2025-02-01', 1,
  '2025-09-01', '2025-07-01', '2025-08-15',
  454521.00, 'ACTIVE', 'CREDIT', false, NULL,
  '2025-02-01 10:00:00', NOW(),
  'FLAT', 'ANNUALLY', 24.00
);

-- ===============================================================
-- DEBTS — User 3: charlie_le (id=3)
-- DTI nguy hiểm, thu nhập thấp
-- Start: 2024-12-01, đã thanh toán tháng 12–4 (5 lần), 1 khoản OVERDUE
-- ===============================================================

-- Debt 3-A: Vay ngân hàng ACB — OVERDUE (bỏ lỡ tháng 7)
--   P=30,000,000  n=36  rate=11%/năm  REDUCING_BALANCE
--   r = 0.11/12 = 0.009167
--   M = 30000000 * 0.009167*(1.009167)^36 / ((1.009167)^36 - 1) ≈ 981,500
--   Sau 5 tháng trả, bỏ tháng 6 và 7: remaining ≈ 25,800,000
--   next_due_date đã qua → OVERDUE
--   accrued_interest = 25800000 * (0.11/365) * 107 ≈ 832,025
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  3, 'ACB - Vay tín chấp', 30000000.00, 25800000.00,
  981500.00, 36, '2024-12-01', 1,
  '2025-07-01', '2025-05-01', '2025-08-15',
  832025.00, 'OVERDUE', 'BANKING', false, NULL,
  '2024-12-01 08:00:00', NOW(),
  'REDUCING_BALANCE', 'ANNUALLY', 11.00
);

-- Debt 3-B: Vay online (lãi cao)
--   P=6,000,000  n=12  rate=30%/năm  FLAT
--   M = 6000000/12 + 6000000*0.025 = 500,000 + 150,000 = 650,000
--   Sau 5 tháng: remaining = 6000000 - 5*500000 = 3,500,000
--   accrued_interest = 6000000 * (0.30/365) * 107 ≈ 527,671
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  3, 'Vay online - Findo', 6000000.00, 3500000.00,
  650000.00, 12, '2024-12-01', 1,
  '2025-09-01', '2025-07-01', '2025-08-15',
  527671.00, 'ACTIVE', 'PERSONAL_LOAN', false, NULL,
  '2024-12-01 08:00:00', NOW(),
  'FLAT', 'ANNUALLY', 30.00
);

-- Debt 3-C: Thẻ tín dụng Techcombank — OVERDUE
--   P=12,000,000  n=24  rate=21%/năm  FLAT
--   M = 12000000/24 + 12000000*0.0175 = 500,000 + 210,000 = 710,000
--   Sau 5 tháng: remaining = 12000000 - 5*500000 = 9,500,000
--   next_due_date đã qua (bỏ tháng 7) → OVERDUE
--   accrued_interest = 12000000 * (0.21/365) * 107 ≈ 738,378
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  3, 'Techcombank - Thẻ tín dụng', 12000000.00, 9500000.00,
  710000.00, 24, '2024-12-01', 1,
  '2025-07-01', '2025-05-01', '2025-08-15',
  738378.00, 'OVERDUE', 'CREDIT', false, NULL,
  '2024-12-01 08:00:00', NOW(),
  'FLAT', 'ANNUALLY', 21.00
);

-- ===============================================================
-- DEBTS — User 4: david_pham (id=4)
-- Thu nhập cao, nợ lớn, trả đều đặn
-- Start: 2025-03-10, đã thanh toán tháng 3–7 (5 lần)
-- ===============================================================

-- Debt 4-A: Vay mua bất động sản (một phần)
--   P=200,000,000  n=60  rate=8.5%/năm  REDUCING_BALANCE
--   r = 0.085/12 = 0.007083
--   M = 200000000 * 0.007083*(1.007083)^60 / ((1.007083)^60 - 1)
--     ≈ 200000000 * 0.007083*1.52521 / 0.52521 ≈ 4,106,000
--   Sau 5 tháng: remaining ≈ 182,500,000
--   accrued_interest = 182500000 * (0.085/365) * 36 ≈ 1,530,740
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  4, 'Vietcombank - Vay bất động sản', 200000000.00, 182500000.00,
  4106000.00, 60, '2025-03-10', 10,
  '2025-09-10', '2025-08-10', '2025-08-15',
  1530740.00, 'ACTIVE', 'BANKING', false, NULL,
  '2025-03-10 11:00:00', NOW(),
  'REDUCING_BALANCE', 'ANNUALLY', 8.50
);

-- Debt 4-B: Vay vốn kinh doanh
--   P=50,000,000  n=24  rate=13%/năm  FLAT
--   M = 50000000/24 + 50000000*(0.13/12) = 2,083,333 + 541,667 = 2,625,000
--   Sau 5 tháng: remaining = 50000000 - 5*2083333 = 50000000 - 10416665 = 39,583,335
--   accrued_interest = 50000000 * (0.13/365) * 36 ≈ 642,740
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  4, 'TPBank - Vay kinh doanh', 50000000.00, 39583335.00,
  2625000.00, 24, '2025-03-10', 10,
  '2025-09-10', '2025-08-10', '2025-08-15',
  642740.00, 'ACTIVE', 'PERSONAL_LOAN', false, NULL,
  '2025-03-10 11:00:00', NOW(),
  'FLAT', 'ANNUALLY', 13.00
);

-- Debt 4-C: Thẻ tín dụng Visa Platinum
--   P=25,000,000  n=36  rate=20%/năm  FLAT
--   M = 25000000/36 + 25000000*(0.20/12) = 694,444 + 416,667 = 1,111,111
--   Sau 5 tháng: remaining = 25000000 - 5*694444 = 25000000 - 3472220 = 21,527,780
--   accrued_interest = 25000000 * (0.20/365) * 36 ≈ 493,151
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  4, 'VPBank - Visa Platinum', 25000000.00, 21527780.00,
  1111111.00, 36, '2025-03-10', 10,
  '2025-09-10', '2025-08-10', '2025-08-15',
  493151.00, 'ACTIVE', 'CREDIT', false, NULL,
  '2025-03-10 11:00:00', NOW(),
  'FLAT', 'ANNUALLY', 20.00
);

-- ===============================================================
-- DEBTS — User 5: eva_hoang (id=5)
-- Mix: 1 gần tất toán, 1 OVERDUE, 1 ACTIVE bình thường
-- Start: 2024-10-20, đã thanh toán nhiều tháng hơn
-- ===============================================================

-- Debt 5-A: Gần tất toán — còn 2 tháng
--   P=12,000,000  n=12  rate=9%/năm  REDUCING_BALANCE
--   r = 0.09/12 = 0.0075
--   M = 12000000 * 0.0075*(1.0075)^12 / ((1.0075)^12 - 1)
--     ≈ 12000000 * 0.0075*1.09381 / 0.09381 ≈ 1,048,600
--   Sau 10 tháng: remaining ≈ 2,100,000
--   accrued_interest = 2100000 * (0.09/365) * 26 ≈ 13,495
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  5, 'Agribank - Vay tiêu dùng', 12000000.00, 2100000.00,
  1048600.00, 12, '2024-10-20', 20,
  '2025-09-20', '2025-08-20', '2025-08-15',
  13495.00, 'ACTIVE', 'BANKING', false, NULL,
  '2024-10-20 07:00:00', NOW(),
  'REDUCING_BALANCE', 'ANNUALLY', 9.00
);

-- Debt 5-B: Vay cá nhân — OVERDUE (không trả tháng 7 và 8)
--   P=8,000,000  n=12  rate=15%/năm  FLAT
--   M = 8000000/12 + 8000000*(0.15/12) = 666,667 + 100,000 = 766,667
--   Sau 6 tháng trả, bỏ 2: remaining = 8000000 - 6*666667 = 8000000 - 4000002 = 3,999,998
--   accrued_interest = 8000000 * (0.15/365) * 77 ≈ 253,151
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  5, 'Vay anh họ - Minh', 8000000.00, 3999998.00,
  766667.00, 12, '2024-10-20', 20,
  '2025-06-20', '2025-05-20', '2025-08-15',
  253151.00, 'OVERDUE', 'PERSONAL_LOAN', false, NULL,
  '2024-10-20 07:00:00', NOW(),
  'FLAT', 'ANNUALLY', 15.00
);

-- Debt 5-C: Thẻ tín dụng Shinhan
--   P=5,000,000  n=18  rate=22%/năm  FLAT
--   M = 5000000/18 + 5000000*(0.22/12) = 277,778 + 91,667 = 369,444
--   Sau 8 tháng: remaining = 5000000 - 8*277778 = 5000000 - 2222224 = 2,777,776
--   accrued_interest = 5000000 * (0.22/365) * 26 ≈ 78,356
INSERT INTO debts (
  user_id, lender_name, total_principal, remaining_principal,
  expected_monthly_payment, term_months, start_date, due_day,
  next_due_date, last_payment_date, last_interest_accrued_date,
  accrued_interest, status, debt_type, deleted, paid_off_at,
  created_at, updated_at,
  interest_calculation_method, interest_frequency, interest_rate
) VALUES (
  5, 'Shinhan Bank - Thẻ tín dụng', 5000000.00, 2777776.00,
  369444.00, 18, '2024-10-20', 20,
  '2025-09-20', '2025-08-20', '2025-08-15',
  78356.00, 'ACTIVE', 'CREDIT', false, NULL,
  '2024-10-20 07:00:00', NOW(),
  'FLAT', 'ANNUALLY', 22.00
);

-- ===============================================================
-- PAYMENTS — User 1 / alice_nguyen
-- Debt IDs: 1 (BANKING), 2 (PERSONAL_LOAN), 3 (CREDIT)
-- 5 lần thanh toán: tháng 2 → tháng 6 (ngày 15)
-- Interest-first allocation: interestPaid = accrued lãi tháng đó
-- ===============================================================

-- Debt 1 (VietinBank): interest tháng = 50M*(0.09/12) → giảm dần
-- Tháng 1: lãi = 50000000*0.0075=375000, principal = 1589700-375000=1214700
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(1, '2025-02-15', 1589700.00, 1214700.00, 375000.00, false, 'BANK_TRANSFER', 'Trả tháng 1', NOW(), NOW()),
(1, '2025-03-15', 1589700.00, 1223808.00, 365892.00, false, 'BANK_TRANSFER', 'Trả tháng 2', NOW(), NOW()),
(1, '2025-04-15', 1589700.00, 1233000.00, 356700.00, false, 'BANK_TRANSFER', 'Trả tháng 3', NOW(), NOW()),
(1, '2025-05-15', 1589700.00, 1242275.00, 347425.00, false, 'BANK_TRANSFER', 'Trả tháng 4', NOW(), NOW()),
(1, '2025-06-15', 1589700.00, 1251635.00, 338065.00, false, 'BANK_TRANSFER', 'Trả tháng 5', NOW(), NOW());

-- Debt 2 (Vay bạn FLAT): interest tháng = 10M*0.01=100000 (cố định)
-- principal = M - interest = 933333 - 100000 = 833333
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(2, '2025-02-15', 933333.00, 833333.00, 100000.00, false, 'CASH',          'Trả tháng 1 - Minh Tú', NOW(), NOW()),
(2, '2025-03-15', 933333.00, 833333.00, 100000.00, false, 'E_WALLET',      'Trả tháng 2 - Minh Tú', NOW(), NOW()),
(2, '2025-04-15', 933333.00, 833333.00, 100000.00, false, 'CASH',          'Trả tháng 3 - Minh Tú', NOW(), NOW()),
(2, '2025-05-15', 933333.00, 833333.00, 100000.00, false, 'E_WALLET',      'Trả tháng 4 - Minh Tú', NOW(), NOW()),
(2, '2025-06-15', 933333.00, 833333.00, 100000.00, false, 'CASH',          'Trả tháng 5 - Minh Tú', NOW(), NOW());

-- Debt 3 (Sacombank FLAT): interest tháng = 8M*0.015=120000 (cố định)
-- principal = 453333 - 120000 = 333333
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(3, '2025-02-15', 453333.00, 333333.00, 120000.00, false, 'CREDIT_CARD',   'Thanh toán thẻ tháng 1', NOW(), NOW()),
(3, '2025-03-15', 453333.00, 333333.00, 120000.00, false, 'CREDIT_CARD',   'Thanh toán thẻ tháng 2', NOW(), NOW()),
(3, '2025-04-15', 453333.00, 333333.00, 120000.00, false, 'CREDIT_CARD',   'Thanh toán thẻ tháng 3', NOW(), NOW()),
(3, '2025-05-15', 453333.00, 333333.00, 120000.00, false, 'CREDIT_CARD',   'Thanh toán thẻ tháng 4', NOW(), NOW()),
(3, '2025-06-15', 453333.00, 333333.00, 120000.00, false, 'CREDIT_CARD',   'Thanh toán thẻ tháng 5', NOW(), NOW());

-- ===============================================================
-- PAYMENTS — User 2 / bob_tran
-- Debt IDs: 4 (BANKING), 5 (PERSONAL_LOAN), 6 (CREDIT)
-- 5 lần thanh toán: tháng 2 → tháng 7 (ngày 1)
-- ===============================================================

-- Debt 4 (BIDV REDUCING): interest tháng 1 = 20M*0.00875=175000, giảm dần
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(4, '2025-03-01', 926200.00, 751200.00, 175000.00, false, 'BANK_TRANSFER', 'Trả tháng 1', NOW(), NOW()),
(4, '2025-04-01', 926200.00, 757778.00, 168422.00, false, 'BANK_TRANSFER', 'Trả tháng 2', NOW(), NOW()),
(4, '2025-05-01', 926200.00, 764413.00, 161787.00, false, 'BANK_TRANSFER', 'Trả tháng 3', NOW(), NOW()),
(4, '2025-06-01', 926200.00, 771107.00, 155093.00, false, 'BANK_TRANSFER', 'Trả tháng 4', NOW(), NOW()),
(4, '2025-07-01', 926200.00, 777861.00, 148339.00, false, 'BANK_TRANSFER', 'Trả tháng 5', NOW(), NOW());

-- Debt 5 (Vay Hùng FLAT): interest = 5M*(0.08/12)=33333 (cố định), principal=833333
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(5, '2025-03-01', 866667.00, 833333.00, 33333.00, false, 'CASH',          'Trả tháng 1 - Hùng', NOW(), NOW()),
(5, '2025-04-01', 866667.00, 833333.00, 33333.00, false, 'CASH',          'Trả tháng 2 - Hùng', NOW(), NOW()),
(5, '2025-05-01', 866667.00, 833333.00, 33333.00, false, 'E_WALLET',      'Trả tháng 3 - Hùng', NOW(), NOW()),
(5, '2025-06-01', 866667.00, 833333.00, 33333.00, false, 'E_WALLET',      'Trả tháng 4 - Hùng', NOW(), NOW()),
(5, '2025-07-01', 866667.00, 833333.00, 33333.00, false, 'CASH',          'Trả tháng 5 - Hùng', NOW(), NOW());

-- Debt 6 (MB Bank FLAT): interest = 15M*0.02=300000 (cố định), principal=833333
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(6, '2025-03-01', 1133333.00, 833333.00, 300000.00, false, 'CREDIT_CARD',  'MB Bank tháng 1', NOW(), NOW()),
(6, '2025-04-01', 1133333.00, 833333.00, 300000.00, false, 'CREDIT_CARD',  'MB Bank tháng 2', NOW(), NOW()),
(6, '2025-05-01', 1133333.00, 833333.00, 300000.00, false, 'CREDIT_CARD',  'MB Bank tháng 3', NOW(), NOW()),
(6, '2025-06-01', 1133333.00, 833333.00, 300000.00, false, 'CREDIT_CARD',  'MB Bank tháng 4', NOW(), NOW()),
(6, '2025-07-01', 1133333.00, 833333.00, 300000.00, false, 'BANK_TRANSFER','MB Bank tháng 5', NOW(), NOW());

-- ===============================================================
-- PAYMENTS — User 3 / charlie_le
-- Debt IDs: 7 (BANKING OVERDUE), 8 (PERSONAL_LOAN), 9 (CREDIT OVERDUE)
-- Chỉ trả tháng 12–5 (5 lần), bỏ tháng 6 và 7 trên debt 7 và 9
-- ===============================================================

-- Debt 7 (ACB REDUCING): interest tháng 1 = 30M*(0.11/12)=275000, giảm dần
-- Chỉ 5 payments (tháng 1–5 của nợ = 12/2024 → 04/2025)
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(7, '2025-01-01', 981500.00, 706500.00, 275000.00, false, 'BANK_TRANSFER', 'ACB tháng 1', NOW(), NOW()),
(7, '2025-02-01', 981500.00, 712990.00, 268510.00, false, 'BANK_TRANSFER', 'ACB tháng 2', NOW(), NOW()),
(7, '2025-03-01', 981500.00, 719539.00, 261961.00, false, 'BANK_TRANSFER', 'ACB tháng 3', NOW(), NOW()),
(7, '2025-04-01', 981500.00, 726149.00, 255351.00, false, 'BANK_TRANSFER', 'ACB tháng 4', NOW(), NOW()),
(7, '2025-05-01', 981500.00, 732820.00, 248680.00, false, 'BANK_TRANSFER', 'ACB tháng 5', NOW(), NOW());

-- Debt 8 (Findo FLAT): interest = 6M*(0.30/12)=150000 (cố định), principal=500000
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(8, '2025-01-01', 650000.00, 500000.00, 150000.00, false, 'E_WALLET',      'Findo tháng 1', NOW(), NOW()),
(8, '2025-02-01', 650000.00, 500000.00, 150000.00, false, 'E_WALLET',      'Findo tháng 2', NOW(), NOW()),
(8, '2025-03-01', 650000.00, 500000.00, 150000.00, false, 'E_WALLET',      'Findo tháng 3', NOW(), NOW()),
(8, '2025-04-01', 650000.00, 500000.00, 150000.00, false, 'E_WALLET',      'Findo tháng 4', NOW(), NOW()),
(8, '2025-05-01', 650000.00, 500000.00, 150000.00, false, 'E_WALLET',      'Findo tháng 5', NOW(), NOW());

-- Debt 9 (Techcombank FLAT): interest = 12M*0.0175=210000 (cố định), principal=500000
-- 5 payments chỉ (tháng 1–5), skip tháng 6 và 7 → OVERDUE
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(9, '2025-01-01', 710000.00, 500000.00, 210000.00, false, 'CREDIT_CARD',   'Techcombank tháng 1', NOW(), NOW()),
(9, '2025-02-01', 710000.00, 500000.00, 210000.00, false, 'CREDIT_CARD',   'Techcombank tháng 2', NOW(), NOW()),
(9, '2025-03-01', 710000.00, 500000.00, 210000.00, false, 'CREDIT_CARD',   'Techcombank tháng 3', NOW(), NOW()),
(9, '2025-04-01', 710000.00, 500000.00, 210000.00, false, 'CREDIT_CARD',   'Techcombank tháng 4', NOW(), NOW()),
(9, '2025-05-01', 710000.00, 500000.00, 210000.00, false, 'CREDIT_CARD',   'Techcombank tháng 5', NOW(), NOW());

-- ===============================================================
-- PAYMENTS — User 4 / david_pham
-- Debt IDs: 10 (BANKING), 11 (PERSONAL_LOAN), 12 (CREDIT)
-- 5 lần thanh toán: tháng 3 → tháng 8 (ngày 10)
-- ===============================================================

-- Debt 10 (Vietcombank REDUCING): interest tháng 1 = 200M*(0.085/12)=1416667, giảm dần
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(10, '2025-04-10', 4106000.00, 2689333.00, 1416667.00, false, 'BANK_TRANSFER', 'Vietcombank tháng 1', NOW(), NOW()),
(10, '2025-05-10', 4106000.00, 2708390.00, 1397610.00, false, 'BANK_TRANSFER', 'Vietcombank tháng 2', NOW(), NOW()),
(10, '2025-06-10', 4106000.00, 2727583.00, 1378417.00, false, 'BANK_TRANSFER', 'Vietcombank tháng 3', NOW(), NOW()),
(10, '2025-07-10', 4106000.00, 2746921.00, 1359079.00, false, 'BANK_TRANSFER', 'Vietcombank tháng 4', NOW(), NOW()),
(10, '2025-08-10', 4106000.00, 2766406.00, 1339594.00, false, 'BANK_TRANSFER', 'Vietcombank tháng 5', NOW(), NOW());

-- Debt 11 (TPBank FLAT): interest = 50M*(0.13/12)=541667 (cố định), principal=2083333
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(11, '2025-04-10', 2625000.00, 2083333.00, 541667.00, false, 'BANK_TRANSFER', 'TPBank tháng 1', NOW(), NOW()),
(11, '2025-05-10', 2625000.00, 2083333.00, 541667.00, false, 'BANK_TRANSFER', 'TPBank tháng 2', NOW(), NOW()),
(11, '2025-06-10', 2625000.00, 2083333.00, 541667.00, false, 'BANK_TRANSFER', 'TPBank tháng 3', NOW(), NOW()),
(11, '2025-07-10', 2625000.00, 2083333.00, 541667.00, false, 'BANK_TRANSFER', 'TPBank tháng 4', NOW(), NOW()),
(11, '2025-08-10', 2625000.00, 2083333.00, 541667.00, false, 'BANK_TRANSFER', 'TPBank tháng 5', NOW(), NOW());

-- Debt 12 (VPBank FLAT): interest = 25M*(0.20/12)=416667 (cố định), principal=694444
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(12, '2025-04-10', 1111111.00, 694444.00, 416667.00, false, 'CREDIT_CARD',   'VPBank Visa tháng 1', NOW(), NOW()),
(12, '2025-05-10', 1111111.00, 694444.00, 416667.00, false, 'CREDIT_CARD',   'VPBank Visa tháng 2', NOW(), NOW()),
(12, '2025-06-10', 1111111.00, 694444.00, 416667.00, false, 'CREDIT_CARD',   'VPBank Visa tháng 3', NOW(), NOW()),
(12, '2025-07-10', 1111111.00, 694444.00, 416667.00, false, 'CREDIT_CARD',   'VPBank Visa tháng 4', NOW(), NOW()),
(12, '2025-08-10', 1111111.00, 694444.00, 416667.00, false, 'CREDIT_CARD',   'VPBank Visa tháng 5', NOW(), NOW());

-- ===============================================================
-- PAYMENTS — User 5 / eva_hoang
-- Debt IDs: 13 (BANKING gần tất toán), 14 (PERSONAL_LOAN OVERDUE), 15 (CREDIT)
-- Debt 13: 10 payments (nhiều tháng hơn)
-- Debt 14: 6 payments, bỏ 2 tháng gần nhất → OVERDUE
-- Debt 15: 8 payments
-- ===============================================================

-- Debt 13 (Agribank REDUCING gần tất toán): 10 payments 10/2024 → 07/2025
-- interest tháng 1 = 12M*0.0075=90000, giảm dần
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(13, '2024-11-20', 1048600.00, 958600.00, 90000.00, false, 'BANK_TRANSFER', 'Agribank tháng 1',  NOW(), NOW()),
(13, '2024-12-20', 1048600.00, 965789.00, 82811.00, false, 'BANK_TRANSFER', 'Agribank tháng 2',  NOW(), NOW()),
(13, '2025-01-20', 1048600.00, 973030.00, 75570.00, false, 'BANK_TRANSFER', 'Agribank tháng 3',  NOW(), NOW()),
(13, '2025-02-20', 1048600.00, 980325.00, 68275.00, false, 'BANK_TRANSFER', 'Agribank tháng 4',  NOW(), NOW()),
(13, '2025-03-20', 1048600.00, 987673.00, 60927.00, false, 'BANK_TRANSFER', 'Agribank tháng 5',  NOW(), NOW()),
(13, '2025-04-20', 1048600.00, 995076.00, 53524.00, false, 'BANK_TRANSFER', 'Agribank tháng 6',  NOW(), NOW()),
(13, '2025-05-20', 1048600.00, 1002534.00,46066.00, false, 'BANK_TRANSFER', 'Agribank tháng 7',  NOW(), NOW()),
(13, '2025-06-20', 1048600.00, 1010049.00,38551.00, false, 'BANK_TRANSFER', 'Agribank tháng 8',  NOW(), NOW()),
(13, '2025-07-20', 1048600.00, 1017620.00,30980.00, false, 'BANK_TRANSFER', 'Agribank tháng 9',  NOW(), NOW()),
(13, '2025-08-20', 1048600.00, 1025249.00,23351.00, false, 'BANK_TRANSFER', 'Agribank tháng 10', NOW(), NOW());

-- Debt 14 (Vay anh Minh FLAT OVERDUE): 6 payments (10/2024 → 03/2025)
-- interest = 8M*(0.15/12)=100000 (cố định), principal=666667
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(14, '2024-11-20', 766667.00, 666667.00, 100000.00, false, 'CASH',          'Anh Minh tháng 1', NOW(), NOW()),
(14, '2024-12-20', 766667.00, 666667.00, 100000.00, false, 'CASH',          'Anh Minh tháng 2', NOW(), NOW()),
(14, '2025-01-20', 766667.00, 666667.00, 100000.00, false, 'CASH',          'Anh Minh tháng 3', NOW(), NOW()),
(14, '2025-02-20', 766667.00, 666667.00, 100000.00, false, 'CASH',          'Anh Minh tháng 4', NOW(), NOW()),
(14, '2025-03-20', 766667.00, 666667.00, 100000.00, false, 'CASH',          'Anh Minh tháng 5', NOW(), NOW()),
(14, '2025-04-20', 766667.00, 666667.00, 100000.00, false, 'E_WALLET',      'Anh Minh tháng 6', NOW(), NOW());

-- Debt 15 (Shinhan FLAT): 8 payments (10/2024 → 05/2025)
-- interest = 5M*(0.22/12)=91667 (cố định), principal=277778
INSERT INTO payments (debt_id, payment_date, amount, principal_paid, interest_paid, deleted, payment_method, note, created_at, updated_at) VALUES
(15, '2024-11-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 1', NOW(), NOW()),
(15, '2024-12-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 2', NOW(), NOW()),
(15, '2025-01-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 3', NOW(), NOW()),
(15, '2025-02-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 4', NOW(), NOW()),
(15, '2025-03-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 5', NOW(), NOW()),
(15, '2025-04-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 6', NOW(), NOW()),
(15, '2025-05-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 7', NOW(), NOW()),
(15, '2025-06-20', 369444.00, 277778.00, 91667.00, false, 'CREDIT_CARD',   'Shinhan tháng 8', NOW(), NOW());

-- ===============================================================
-- SUMMARY
-- ===============================================================
-- User            | Income     | Debts (IDs)  | Scenario
-- ----------------|------------|--------------|------------------
-- alice_nguyen    | 20,000,000 | 1, 2, 3      | DTI tốt, đang trả đều
-- bob_tran        | 15,000,000 | 4, 5, 6      | DTI cảnh báo, lãi thẻ cao
-- charlie_le      | 10,000,000 | 7, 8, 9      | DTI nguy hiểm, 2 khoản OVERDUE
-- david_pham      | 40,000,000 | 10, 11, 12   | Nợ lớn, trả đều, nợ kinh doanh
-- eva_hoang       | 12,000,000 | 13, 14, 15   | Nợ gần tất toán + 1 OVERDUE
--
-- Payment scenarios có trong data:
-- • Interest-first allocation (interestPaid + principalPaid tách rõ)
-- • FLAT: interest cố định mỗi tháng, principal = M - interest
-- • REDUCING_BALANCE: interest giảm dần, principal tăng dần
-- • Trả đủ minimum (ACTIVE, next_due_date được cập nhật)
-- • Bỏ trả 2 tháng liên tiếp (OVERDUE, next_due_date không di chuyển)
-- • Gần tất toán: remaining_principal nhỏ (debt 13)
-- • Khoản nợ lãi cao nhất: Findo 30%/năm (debt 8) → target Avalanche
-- • Khoản nợ score cashflow cao: debt 5 (balance nhỏ, minimum lớn so với balance)
-- ===============================================================
