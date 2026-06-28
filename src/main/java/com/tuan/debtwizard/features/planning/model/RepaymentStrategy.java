package com.tuan.debtwizard.features.planning.model;

public enum RepaymentStrategy {
    MINIMIZE_INTEREST("Pay less interest"),
    IMPROVE_CASHFLOW("Improve monthly cashflow");
    private final String displayName;
    RepaymentStrategy(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}