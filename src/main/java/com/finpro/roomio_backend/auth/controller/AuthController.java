package com.finpro.roomio_backend.auth.controller;

import com.finpro.roomio_backend.auth.entity.dto.*;
import com.finpro.roomio_backend.auth.entity.dto.login.LoginRequestDto;
import com.finpro.roomio_backend.auth.service.AuthService;
import com.finpro.roomio_backend.auth.service.RedisTokenService;
import com.finpro.roomio_backend.auth.service.RegistrationService;
import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UserService;
import jakarta.validation.Valid;
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

import java.security.NoSuchAlgorithmException;
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
            data.put("role", user.getIsTenant() ? "TENANT" : "USER");
            data.put("verified", user.getIsVerified());
            return Response.successfulResponse("Email found", data);
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("exists", false);
            data.put("role", null);
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
            // Check if the user associated with the token is already verified
            Users user = registrationService.findUserByEmail(storedToken);
            if (user == null) {
                return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "User not found.");
            }

            if (user.getIsVerified()) {
                return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "User is already verified. Password cannot be updated.");
            }

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

    @PostMapping("/register/tenant")
    public ResponseEntity<Response<Object>> registerTenant(@Validated @RequestBody RegistrationRequestDto request) {
        try {
            registrationService.registerTenant(request.getEmail());
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
        return Response.successfulResponse("Logout request for user: " + SecurityContextHolder.getContext().getAuthentication().getName() + " successful");
    }

    // > verify token
    @PostMapping("/verify-token")
    public ResponseEntity<Response<Object>> verifyToken(@RequestBody @Valid VerifyTokenRequestDto verifyTokenDto) {
        try {
            String token = verifyTokenDto.getToken();

            // Log the token received
            System.out.println("Received token: " + token);

            if (redisTokenService.isTokenValid(token)) {
                return Response.successfulResponse("Token is valid.");
            } else {
                return Response.failedResponse("Invalid or expired token.");
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace(); // Consider using a logging framework in production

            // Return a generic error response
            return Response.failedResponse("An error occurred while verifying the token.");
        }
    }

    // > forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<Response<Object>> forgotPassword(@RequestBody @Valid CheckEmailDto checkEmailDto) {
        try {
            registrationService.forgotPassword(checkEmailDto.getEmail());
            return Response.successfulResponse("Reset Password link successful generate, please check your email for reset password.");
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

    @PostMapping("/reset-password")
    public ResponseEntity<Response<String>> resetPassword(@RequestBody VerificationRequestDto request) {
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
            // Check if the user associated with the token is already verified
            Users user = registrationService.findUserByEmail(storedToken);
            if (user == null) {
                return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "User not found.");
            }

            // Encrypt password before saving
            String hashedPassword = passwordEncoder.encode(password);

            // Verify user and save the password
            registrationService.verifyUser(storedToken, hashedPassword);

            // Delete the token
            redisTokenService.deleteToken(token);

            return Response.successfulResponse(HttpStatus.OK.value(), "Password successfuly reset, you can now log in.", "Reset Password successful");
        } catch (Exception e) {
            // Log the exception with a logger
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("Error during reset password: {}", e.getMessage(), e);

            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Reset Password failed: " + e.getMessage());
        }
    }


}