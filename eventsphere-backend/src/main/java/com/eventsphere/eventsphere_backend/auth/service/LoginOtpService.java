package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.LoginOtpResponse;
import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import org.springframework.stereotype.Service;

@Service
public class LoginOtpService {

    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;

    public LoginOtpService(
            OtpService otpService,
            EmailService emailService,
            SmsService smsService) {

        this.otpService = otpService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public LoginOtpResponse sendLoginOtp(
            String email,
            String phoneNumber) {

        if (email != null && !email.isBlank()) {

            String otp = otpService.generateOtp(
                    email,
                    OtpChannel.EMAIL,
                    OtpPurpose.LOGIN
            );

            emailService.sendOtp(
                    email,
                    otp,
                    "Login"
            );

            return LoginOtpResponse.builder()
                    .message("Login OTP sent successfully")
                    .otpSent(true)
                    .build();
        }

        if (phoneNumber != null && !phoneNumber.isBlank()) {

            String otp = otpService.generateOtp(
                    phoneNumber,
                    OtpChannel.MOBILE,
                    OtpPurpose.LOGIN
            );

            smsService.sendOtp(
                    phoneNumber,
                    otp,
                    "Login"
            );

            return LoginOtpResponse.builder()
                    .message("Login OTP sent successfully")
                    .otpSent(true)
                    .build();
        }

        throw new OtpVerificationException(
                "Unable to send login OTP"
        );
    }

    public void verifyLoginOtp(
            String target,
            OtpChannel channel,
            String otp) {

        otpService.verifyOtp(
                target,
                channel,
                OtpPurpose.LOGIN,
                otp
        );
    }
}