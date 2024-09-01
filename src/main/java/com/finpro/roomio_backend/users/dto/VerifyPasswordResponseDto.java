package com.finpro.roomio_backend.users.dto;

import lombok.Data;

@Data
public class VerifyPasswordResponseDto {
    private boolean success;

    public VerifyPasswordResponseDto(boolean success){
        this.success = success;
    }
}
