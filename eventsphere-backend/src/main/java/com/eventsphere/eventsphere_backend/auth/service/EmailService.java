package com.eventsphere.eventsphere_backend.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // =========================================================
    // SEND OTP
    // =========================================================

    public void sendOtp(
            String email,
            String otp,
            String purpose) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "EventSphere - Verification OTP"
        );

        message.setText(
                "Your EventSphere verification OTP is: "
                        + otp
                        + "\n\n"
                        + "This OTP is valid for 5 minutes."
                        + "\n\n"
                        + "Purpose: "
                        + purpose
                        + "\n\n"
                        + "If you did not request this OTP, "
                        + "please ignore this email."
        );

        mailSender.send(message);
    }
}
