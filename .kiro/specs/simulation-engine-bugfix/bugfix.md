# Bugfix Requirements Document

## Introduction

Tài liệu này mô tả 5 lỗi được phát hiện trong module Planning của DebtWizard, bao gồm các lỗi tính toán trong SimulationEngine, lỗi mutation snapshot trong PlanningService, lỗi công thức trong ImproveCashflowStrategy, mâu thuẫn công thức Quick Win Score giữa các tài liệu thiết kế, và lỗi bỏ qua overpayment trong PaymentService. Các lỗi này ảnh hưởng trực tiếp đến tính chính xác của kết quả mô phỏng trả nợ và so sánh kế hoạch.

---

## Bug 1: Thứ tự tính lãi/gốc trong SimulationEngine sai

### Current Behavior (Defect)

1.1 WHEN SimulationEngine bắt đầu xử lý một tháng mới THEN hệ thống áp dụng minimum payment trước (trừ vào balance), sau đó áp dụng extra payment (trừ vào balance), rồi mới tính lãi trên balance đã giảm sau khi trả

1.2 WHEN lãi được tính trên balance sau khi đã trừ payment THEN hệ thống underestimate tổng lãi phát sinh vì balance tính lãi nhỏ hơn balance đầu tháng thực tế

### Expected Behavior (Correct)

2.1 WHEN SimulationEngine bắt đầu xử lý một tháng mới THEN hệ thống SHALL tính lãi trên balance đầu tháng trước (trước khi trừ bất kỳ khoản thanh toán nào), sau đó cộng lãi vào balance, rồi mới áp dụng minimum payment và extra payment

2.2 WHEN lãi được cộng vào balance đầu tháng THEN hệ thống SHALL phân bổ payment theo thứ tự: lãi trước, gốc sau — nhất quán với cách PaymentService.createPayment() đang thực hiện

### Unchanged Behavior (Regression Prevention)

3.1 WHEN tất cả các khoản nợ đều có balance > 0 THEN hệ thống SHALL CONTINUE TO áp dụng minimum payment cho toàn bộ active debts mỗi tháng

3.2 WHEN một khoản nợ được tất toán THEN hệ thống SHALL CONTINUE TO giải phóng minimum payment của khoản đó và cộng vào extra payment budget

3.3 WHEN extra payment được phân bổ THEN hệ thống SHALL CONTINUE TO ưu tiên khoản nợ theo chiến lược được chọn (MINIMIZE_INTEREST hoặc IMPROVE_CASHFLOW)

---

## Bug 2: Snapshot bị mutate khi so sánh 2 kế hoạch

### Current Behavior (Defect)

1.1 WHEN PlanningService.comparePlans() gọi simulationEngine.simulate() lần đầu với danh sách snapshots THEN hệ thống mutate trực tiếp các DebtSnapshot objects (balance về 0, paidOff = true)

1.2 WHEN simulationEngine.simulate() được gọi lần thứ hai với cùng danh sách snapshots đã bị mutate THEN hệ thống trả về kết quả sai vì tất cả debts đã ở trạng thái paidOff = true và balance = 0, không còn gì để tính

### Expected Behavior (Correct)

2.1 WHEN PlanningService.comparePlans() chuẩn bị gọi simulate() cho mỗi chiến lược THEN hệ thống SHALL tạo một bản deep copy độc lập của danh sách snapshots cho mỗi lần simulate

2.2 WHEN simulate() lần thứ hai được gọi THEN hệ thống SHALL nhận được danh sách snapshots với trạng thái ban đầu đầy đủ (balance gốc, paidOff = false) như lần simulate đầu tiên

### Unchanged Behavior (Regression Prevention)

3.1 WHEN chỉ simulate một chiến lược duy nhất THEN hệ thống SHALL CONTINUE TO trả về kết quả chính xác như hiện tại

3.2 WHEN dữ liệu debt gốc trong database THEN hệ thống SHALL CONTINUE TO không bị ảnh hưởng bởi quá trình simulate

---

## Bug 3: ImproveCashflowStrategy sai công thức

### Current Behavior (Defect)

1.1 WHEN ImproveCashflowStrategy tính EstimatedPayoffMonths THEN hệ thống chỉ dùng minimumPayment làm denominator: `payoffMonths = balance / minimumPayment`, bỏ qua extraPaymentAllocation

1.2 WHEN EstimatedPayoffMonths bị tính sai THEN hệ thống tính Priority Score sai dẫn đến chọn sai khoản nợ ưu tiên, không nhất quán với định nghĩa trong SAD flow5

### Expected Behavior (Correct)

2.1 WHEN ImproveCashflowStrategy tính EstimatedPayoffMonths THEN hệ thống SHALL dùng công thức: `EstimatedPayoffMonths = RemainingBalance / (MinimumPayment + ExtraPaymentAllocation)`

2.2 WHEN EstimatedPayoffMonths được tính đúng THEN hệ thống SHALL tính Priority Score theo công thức: `PriorityScore = MonthlyPayment / EstimatedPayoffMonths` — nhất quán với SAD flow5

### Unchanged Behavior (Regression Prevention)

3.1 WHEN extraPaymentAllocation bằng 0 THEN hệ thống SHALL CONTINUE TO tính payoffMonths chỉ dựa trên minimumPayment (công thức vẫn đúng khi extra = 0)

3.2 WHEN MinimizeInterestStrategy được sử dụng THEN hệ thống SHALL CONTINUE TO hoạt động độc lập, không bị ảnh hưởng bởi thay đổi này

---

## Bug 4: Hai công thức Quick Win Score mâu thuẫn

### Current Behavior (Defect)

1.1 WHEN hệ thống cần tính Quick Win Score THEN tài liệu SAD flow5 định nghĩa: `Quick Win Score = Monthly Payment / Remaining Balance`

1.2 WHEN hệ thống cần tính Quick Win Score THEN tài liệu business_validation_algorithm.md định nghĩa: `Quick Win Score = Monthly Payment Released × Expected Duration Reduction / Additional Payment Required`

1.3 WHEN hai tài liệu định nghĩa khác nhau THEN hệ thống không có công thức chính thức thống nhất, dẫn đến kết quả Quick Win Detection không nhất quán và không đáng tin cậy

### Expected Behavior (Correct)

2.1 WHEN hệ thống tính Quick Win Score THEN hệ thống SHALL dùng duy nhất công thức từ business_validation_algorithm.md: `Quick Win Score = Monthly Payment Released × Expected Duration Reduction / Additional Payment Required` vì công thức này phản ánh đúng giá trị kinh tế của việc tất toán sớm (lợi ích / chi phí)

2.2 WHEN tài liệu SAD flow5 được cập nhật THEN hệ thống SHALL có một định nghĩa duy nhất nhất quán giữa SAD và business_validation_algorithm.md

### Unchanged Behavior (Regression Prevention)

3.1 WHEN Quick Win Detection chạy THEN hệ thống SHALL CONTINUE TO trả về danh sách cơ hội được sort theo score giảm dần và bestOpportunity là khoản nợ có score cao nhất

---

## Bug 5: Overpayment bị discard trong PaymentService

### Current Behavior (Defect)

1.1 WHEN số tiền thanh toán vượt quá dư nợ còn lại (remainingAfterInterest > principal) THEN hệ thống cap principalPaid tại giá trị principal và bỏ qua phần overpayment hoàn toàn: `principalPaid = remainingAfterInterest.min(principal)`

1.2 WHEN phần overpayment bị bỏ qua THEN hệ thống vi phạm BR04: phần dư phải được cộng vào extra payment tháng kế tiếp thay vì bị mất

### Expected Behavior (Correct)

2.1 WHEN số tiền thanh toán vượt quá dư nợ còn lại THEN hệ thống SHALL tính overpayment = remainingAfterInterest - principal và lưu lại để xử lý tiếp

2.2 WHEN overpayment tồn tại sau khi tất toán khoản nợ THEN hệ thống SHALL cộng phần overpayment vào extra payment budget của tháng kế tiếp theo BR04

### Unchanged Behavior (Regression Prevention)

3.1 WHEN số tiền thanh toán đúng bằng hoặc nhỏ hơn dư nợ THEN hệ thống SHALL CONTINUE TO xử lý payment bình thường, không có overpayment

3.2 WHEN khoản nợ được tất toán bình thường (không overpayment) THEN hệ thống SHALL CONTINUE TO cập nhật trạng thái PAID_OFF và lưu payment đúng như hiện tại

---

## Bug Condition Summary

### Bug 1 – Interest Ordering

```pascal
FUNCTION isBugCondition_Bug1(snapshot, month)
  INPUT: snapshot là DebtSnapshot, month là vòng lặp simulate
  OUTPUT: boolean
  RETURN snapshot.balance > 0 AND month >= 1
END FUNCTION

// Property: Fix Checking
FOR ALL snapshot WHERE isBugCondition_Bug1(snapshot, month) DO
  result ← simulate'(snapshot)
  ASSERT totalInterestPaid(result) >= totalInterestPaid(simulate_old(snapshot))
  ASSERT orderOf(applyInterest) BEFORE orderOf(applyPayments)
END FOR

// Property: Preservation Checking
FOR ALL snapshot WHERE NOT isBugCondition_Bug1(snapshot, month) DO
  ASSERT simulate'(snapshot) = simulate(snapshot)
END FOR
```

### Bug 2 – Snapshot Mutation

```pascal
FUNCTION isBugCondition_Bug2(request)
  INPUT: request là CompareRequest với 2 chiến lược khác nhau
  OUTPUT: boolean
  RETURN request.firstStrategy != request.secondStrategy
END FUNCTION

// Property: Fix Checking
FOR ALL request WHERE isBugCondition_Bug2(request) DO
  result ← comparePlans'(request)
  ASSERT result.secondPlan.totalInterestPaid > 0
  ASSERT result.secondPlan.payoffDurationMonths > 0
END FOR

// Property: Preservation Checking
FOR ALL request WHERE NOT isBugCondition_Bug2(request) DO
  ASSERT comparePlans'(request).firstPlan = comparePlans(request).firstPlan
END FOR
```

### Bug 3 – ImproveCashflow Formula

```pascal
FUNCTION isBugCondition_Bug3(debt, extraPayment)
  INPUT: debt là DebtSnapshot, extraPayment là BigDecimal
  OUTPUT: boolean
  RETURN extraPayment > 0
END FUNCTION

// Property: Fix Checking
FOR ALL debt WHERE isBugCondition_Bug3(debt, extraPayment) DO
  score ← selectTargetDebt'([debt], extraPayment).priorityScore
  ASSERT denominator = minimumPayment + extraPayment
END FOR
```

### Bug 4 – Quick Win Score Conflict

```pascal
// Property: Consistency Checking
FOR ALL input DO
  scoreFromSAD ← monthlyPayment / remainingBalance
  scoreFromAlgorithm ← monthlyPaymentReleased * durationReduction / additionalPayment
  ASSERT scoreUsed = scoreFromAlgorithm  // business_validation_algorithm.md wins
END FOR
```

### Bug 5 – Overpayment Discarded

```pascal
FUNCTION isBugCondition_Bug5(payment, debt)
  INPUT: payment là PaymentRequest, debt là Debt
  OUTPUT: boolean
  RETURN payment.amount > debt.accruedInterest + debt.remainingPrincipal
END FUNCTION

// Property: Fix Checking
FOR ALL payment WHERE isBugCondition_Bug5(payment, debt) DO
  result ← createPayment'(payment)
  overpayment ← payment.amount - debt.accruedInterest - debt.remainingPrincipal
  ASSERT overpayment IS carried forward to next month extra budget
END FOR

// Property: Preservation Checking
FOR ALL payment WHERE NOT isBugCondition_Bug5(payment, debt) DO
  ASSERT createPayment'(payment) = createPayment(payment)
END FOR
```
