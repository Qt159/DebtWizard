package com.tuan.debtwizard.features.analysis.service;

import com.tuan.debtwizard.features.analysis.FinanceHealth;


public class FinanceClassifier {
    public static FinanceHealth byRatio(double ratio, double good, double warning) {
        if (ratio <= good) return FinanceHealth.GOOD;
        if (ratio <= warning) return FinanceHealth.WARNING;
        return FinanceHealth.CRITICAL;
        }
    }
