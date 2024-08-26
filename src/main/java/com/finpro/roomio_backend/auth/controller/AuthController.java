package com.finpro.roomio_backend.auth.controller;

import com.finpro.roomio_backend.auth.dto.CheckEmailDto;
import com.finpro.roomio_backend.auth.dto.RegistrationRequestDto;
import com.finpro.roomio_backend.auth.dto.VerificationRequestDto;
import com.finpro.roomio_backend.auth.entity.dto.LoginRequestDto;
import com.finpro.roomio_backend.auth.service.AuthService;
import com.finpro.roomio_backend.auth.service.RedisTokenService;
import com.finpro.roomio_backend.auth.service.RegistrationService;
import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final RedisTokenService redisTokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/check-email")
    public ResponseEntity<Response<Object>> checkEmail(@Validated @RequestBody CheckEmailDto check) {
        Optional<Users> userOptional = userService.getUserByEmail(check.getEmail());

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            Map<String, Object> data = new HashMap<>();
            data.put("exists", true);
            data.put("method", user.getMethod());
            data.put("type", user.getIsTenant() ? "tenant" : "user");
            data.put("verified", user.getIsVerified());
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

    @PostMapping("/resend-verification")
    public ResponseEntity<Response<Object>> resendVerificationEmail(@RequestBody @Validated CheckEmailDto checkEmailDto) {
        try {
            // Call the service to resend the verification email
            registrationService.resendVerificationEmail(checkEmailDto.getEmail());
            return Response.successfulResponse("Verification email resent successfully. Please check your email.");
        } catch (IllegalArgumentException ex) {
            // Handle case where email is not registered or some other argument issue
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        } catch (IllegalStateException ex) {
            // Handle case where user is already verified
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        } catch (Exception ex) {
            // Handle unexpected errors
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.");
        }
    }

    @GetMapping("/verify-page")
    public ResponseEntity<Response<Object>> getVerificationPage(@RequestParam String token) {
        String storedToken = redisTokenService.getToken(token);

        if (storedToken == null) {
            // Return a failed response if the token is invalid or expired
            Response<Object> response = Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Token is expired or invalid.").getBody();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Return a successful response if the token is valid
        Response<Object> response = Response.successfulResponse("Token is valid", null).getBody();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Response<String>> verify(@RequestBody VerificationRequestDto request) {
        String token = request.getToken();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Passwords do not match.");
        }

        // Retrieve token from Redis
        String storedToken = redisTokenService.getToken(token);
        if (storedToken == null) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Token is expired or invalid.");
        }

        try {
            // Encrypt password before saving
            String hashedPassword = passwordEncoder.encode(password);

            // Verify user and save the password
            registrationService.verifyUser(storedToken, hashedPassword);

            // Delete the token
            redisTokenService.deleteToken(token);

            return Response.successfulResponse(HttpStatus.OK.value(), "Verification successful, you can now log in.", "Verification successful");
        } catch (Exception e) {
            // Log the exception with a logger
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("Error during verification: {}", e.getMessage(), e);

            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Verification failed: " + e.getMessage());
        }
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