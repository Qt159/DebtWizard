package com.tuan.debtwizard.features.analysis;

import lombok.Getter;

@Getter
public enum FinanceHealth {
    GOOD ("An toàn", "Chỉ số tài chính ở mức tốt, ít rủi ro."),
    WARNING("Cảnh báo", "Chỉ số đang tiến gần ngưỡng rủi ro, cần theo dõi và điều chỉnh."),
    CRITICAL("Nguy hiểm", "Chỉ số vượt ngưỡng an toàn, nguy cơ mất khả năng thanh toán."),
    INCOMPLETE("Chưa đủ dữ liệu", "Vui lòng cập nhật thông tin tài chính.");
    private final String label;
    private final String defaultAdvice;

    FinanceHealth(String label, String defaultAdvice) {
        this.label = label;
        this.defaultAdvice = defaultAdvice;
    }
}