package com.tuan.debtwizard.features.user.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserResponse {
    private  Long id;
    private  String username;
    private  String fullName;
    private  String email;

    public UserResponse(Long id, String username, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }
}
