package com.tuan.debtwizard.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100)
    private String fullName;

}
