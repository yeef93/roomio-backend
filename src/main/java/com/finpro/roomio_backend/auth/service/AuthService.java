package com.finpro.roomio_backend.auth.service;

import com.finpro.roomio_backend.auth.entity.dto.LoginRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {

 String generateToken(Authentication authentication);

 ResponseEntity<?> login(LoginRequestDto loginRequestDTO);

 void logout();

}
