package com.finpro.roomio_backend.auth.controller;

import com.finpro.roomio_backend.auth.dto.CheckEmailDto;
import com.finpro.roomio_backend.auth.dto.RegistrationRequestDto;
import com.finpro.roomio_backend.auth.entity.dto.LoginRequestDto;
import com.finpro.roomio_backend.auth.service.AuthService;
import com.finpro.roomio_backend.auth.service.RegistrationService;
import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Log
public class AuthController {

    private final RegistrationService registrationService;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/check-email")
    public ResponseEntity<Response<Object>> checkEmail(@Validated @RequestBody CheckEmailDto check) {
        Optional<Users> userOptional = userService.getUserByEmail(check.getEmail());

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            Map<String, Object> data = new HashMap<>();
            data.put("exists", true);
            data.put("method", user.getMethod());
            data.put("type", user.getIsTenant() ? "tenant" : "user");
            return Response.successfulResponse("Email found", data);
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("exists", false);
            data.put("type", null);
            return Response.successfulResponse("Email not found", data);
        }
    }

    @PostMapping("/register/user")
    public ResponseEntity<Response<Object>> register(@Validated @RequestBody RegistrationRequestDto request) {
        try {
            registrationService.registerUser(request.getEmail());
            return Response.successfulResponse("Registration successful, please check your email for verification.");
        } catch (IllegalArgumentException ex) {
            // Log the exception message
            System.out.println("IllegalArgumentException: " + ex.getMessage());
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        } catch (Exception ex) {
            // Log the exception message and stack trace
            ex.printStackTrace();
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.");
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token, @RequestParam String password) {
        registrationService.verifyUser(token, password);
        return ResponseEntity.ok("Verification successful, you can now log in.");
    }

    // > DEV: check who is currently logged in this session
    @GetMapping("")
    public String getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return "Logged in user: " + username + " with role: " + role;
    }

    // > login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto) {
        log.info("Login request for email: " + requestDto.getEmail());
        return authService.login(requestDto);
    }

    // > logout
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok().body(
                "Logout request for user: " + SecurityContextHolder.getContext().getAuthentication().getName() + " successful");
    }
}