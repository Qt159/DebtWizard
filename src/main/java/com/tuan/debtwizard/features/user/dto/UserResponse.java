package com.tuan.debtwizard.features.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserResponse {
    private  Long id;
    private  String username;
    private  String fullName;
    private  String email;
    private  BigDecimal monthlyIncome;
    private  BigDecimal monthlyExpense;
    public UserResponse(Long id, String username, String fullName, String email, BigDecimal monthlyIncome, BigDecimal monthlyExpense) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpense = monthlyExpense;
    }
}
