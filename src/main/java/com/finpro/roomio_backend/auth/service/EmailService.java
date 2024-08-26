package com.finpro.roomio_backend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${spring.mail.displayName}")
    private String fromName;

    public void sendVerificationEmail(String to, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Verify your email");
            helper.setText("Click the link to verify your email: " + verificationLink);
            helper.setFrom(fromEmail, fromName); // Set both email and display name

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle the exception as needed
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}