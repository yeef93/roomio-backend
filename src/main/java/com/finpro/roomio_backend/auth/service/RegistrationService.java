package com.finpro.roomio_backend.auth.service;


import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UsersRepository userRepository;
    private final EmailService emailService;

    @Value("${app.verification-url}")
    private String verificationUrl;

    @Transactional
    public void registerUser(String email) {
        // Check if email already exists
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new IllegalArgumentException("Email already registered");
        });

        // Create a new user and set verification token
        Users user = new Users();
        user.setEmail(email);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setIsVerified(false);

        userRepository.save(user);

        // Send verification email
        String verificationLink = verificationUrl + "?token=" + user.getVerificationToken();
        emailService.sendVerificationEmail(email, verificationLink);
    }

    @Transactional
    public void verifyUser(String token, String password) {
        Users user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        user.setPassword(password);
        user.setIsVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

}