package com.finpro.roomio_backend.auth.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegistrationRequestDto {
    @Email
    @NotEmpty
    private String email;
}
