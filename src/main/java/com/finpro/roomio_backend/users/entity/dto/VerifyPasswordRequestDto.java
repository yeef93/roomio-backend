package com.finpro.roomio_backend.users.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class VerifyPasswordRequestDto {
    @NotEmpty(message = "Password must not be empty")
    private String password;
}
