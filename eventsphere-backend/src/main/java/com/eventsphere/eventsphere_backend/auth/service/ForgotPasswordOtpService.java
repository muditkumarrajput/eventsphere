package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.ForgotPasswordOtpResponse;
import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.common.exception.InvalidCredentialsException;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordOtpService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;

    public ForgotPasswordOtpService(
            UserRepository userRepository,
            OtpService otpService,
            EmailService emailService,
            SmsService smsService) {

        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public ForgotPasswordOtpResponse sendOtp(
            String identifier) {

        String normalizedIdentifier =
                identifier.trim();

        User user;

        if (normalizedIdentifier.matches(
                "^[6-9]\\d{9}$")) {

            user = userRepository
                    .findByPhoneNumber(
                            normalizedIdentifier
                    )
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );

            String otp = otpService.generateOtp(
                    normalizedIdentifier,
                    OtpChannel.MOBILE,
                    OtpPurpose.PASSWORD_RESET
            );

            smsService.sendOtp(
                    normalizedIdentifier,
                    otp,
                    "Password Reset"
            );

        } else {

            user = userRepository
                    .findByEmail(
                            normalizedIdentifier.toLowerCase()
                    )
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );

            String otp = otpService.generateOtp(
                    user.getEmail(),
                    OtpChannel.EMAIL,
                    OtpPurpose.PASSWORD_RESET
            );

            emailService.sendOtp(
                    user.getEmail(),
                    otp,
                    "Password Reset"
            );
        }

        return ForgotPasswordOtpResponse.builder()
                .message(
                        "Password reset OTP sent successfully"
                )
                .otpSent(true)
                .build();
    }

    public User verifyOtp(
            String identifier,
            String otp) {

        String normalizedIdentifier =
                identifier.trim();

        User user;
        String target;
        OtpChannel channel;

        if (normalizedIdentifier.matches(
                "^[6-9]\\d{9}$")) {

            user = userRepository
                    .findByPhoneNumber(
                            normalizedIdentifier
                    )
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );

            target = normalizedIdentifier;
            channel = OtpChannel.MOBILE;

        } else {

            user = userRepository
                    .findByEmail(
                            normalizedIdentifier.toLowerCase()
                    )
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );

            target = user.getEmail();
            channel = OtpChannel.EMAIL;
        }

        otpService.verifyOtp(
                target,
                channel,
                OtpPurpose.PASSWORD_RESET,
                otp
        );

        return user;
    }
}