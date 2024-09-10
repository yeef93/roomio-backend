package com.finpro.roomio_backend.auth.entity.dto;

import lombok.Data;

@Data
public class VerificationRequestDto {
    private String token;
    private String password;
    private String confirmPassword;
}
