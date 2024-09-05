package com.finpro.roomio_backend.auth.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String message;
    private String token;
    private String role;

    public LoginResponseDto(String token) {
        this.token = token;
        this.message = message;
        this.role= role;
    }
}
