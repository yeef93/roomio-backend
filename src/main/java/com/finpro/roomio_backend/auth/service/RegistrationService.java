package com.finpro.roomio_backend.auth.service;


import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UsersRepository userRepository;
    private final EmailService emailService;
    private final RedisTokenService redisTokenService;

    @Value("${app.verification-url}verify-page")
    private String verificationUrl;

    @Transactional
    public void registerUser(String email) {
        String token = UUID.randomUUID().toString();
        // Check if email already exists
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new IllegalArgumentException("Email already registered");
        });

        Users user = new Users();
        user.setEmail(email);
        user.setMethod("email");
//        user.setVerificationToken(UUID.randomUUID().toString());
        user.setIsVerified(false);

        redisTokenService.storeToken(token, email);

        try {
            // Attempt to save the new user
            userRepository.save(user);

            // Only send the verification email if the user is successfully saved
            String verificationLink = verificationUrl + "?token=" + token;
            emailService.sendVerificationEmail(email, verificationLink);

        } catch (Exception e) {
            // Handle the case where user saving fails
            // Log the exception or perform other actions if needed
            throw new RuntimeException("Failed to register user", e);
        }
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        // Find the user by email
        Optional<Users> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Email not registered");
        }

        Users user = userOptional.get();

        // Check if the user is already verified
        if (user.getIsVerified()== true) {
            throw new IllegalStateException("User is already verified");
        }

        // Generate a new verification token
        String newToken = UUID.randomUUID().toString();
        redisTokenService.storeToken(newToken, email);
        //user.setVerificationToken(newToken);

        // Send the verification email
        String verificationLink = verificationUrl + "?token=" + newToken;
        emailService.sendVerificationEmail(email, verificationLink);
    }


    @Transactional
    public void verifyUser(String email, String hashedPassword) {
        Logger logger = LoggerFactory.getLogger(getClass());

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(hashedPassword);
        user.setIsVerified(true);

        userRepository.save(user);
        logger.info("User with email {} has been verified and password updated.", email);
    }

}