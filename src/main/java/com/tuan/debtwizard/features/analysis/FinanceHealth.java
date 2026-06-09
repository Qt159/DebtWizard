package com.tuan.debtwizard.features.analysis;

import lombok.Getter;

@Getter
public enum FinanceHealth {
    GOOD ("An toàn", "Tình hình tài chính rất tốt."),
    WARNING("Cảnh báo", "Nợ bắt đầu cao, hãy cẩn thận chi tiêu."),
    CRITICAL("Nguy hiểm", "BÁO ĐỘNG! Nợ quá lớn so với thu nhập.");

    private final String label;
    private final String defaultAdvice;

    FinanceHealth(String label, String defaultAdvice) {
        this.label = label;
        this.defaultAdvice = defaultAdvice;
    }
}