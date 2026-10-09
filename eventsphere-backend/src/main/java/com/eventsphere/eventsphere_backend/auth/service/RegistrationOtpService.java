package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.PendingRegistrationResponse;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpResponse;
import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.auth.entity.PendingRegistration;
import com.eventsphere.eventsphere_backend.auth.repository.PendingRegistrationRepository;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrationOtpService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final OtpService otpService;
    private final SmsService smsService;
    private final EmailService emailService;

    public RegistrationOtpService(
            PendingRegistrationRepository pendingRegistrationRepository,
            OtpService otpService,
            SmsService smsService,
            EmailService emailService) {

        this.pendingRegistrationRepository =
                pendingRegistrationRepository;
        this.otpService = otpService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @Transactional
    public PendingRegistrationResponse sendRegistrationOtp(
            PendingRegistration pendingRegistration) {

        String emailOtp = otpService.generateOtp(
                pendingRegistration.getEmail(),
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        emailService.sendOtp(
                pendingRegistration.getEmail(),
                emailOtp,
                "Registration"
        );

        return PendingRegistrationResponse.builder()
                .message("Email OTP sent successfully")
                .mobileOtpSent(false)
                .emailOtpSent(true)
                .build();
    }

    public PendingRegistrationResponse resendRegistrationOtp(
            String email) {

        String normalizedEmail = email.trim().toLowerCase();

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new OtpVerificationException(
                                        "Registration request not found. Please register again."
                                )
                        );

        validatePendingRegistration(pendingRegistration);

        if (pendingRegistration.isEmailVerified()) {
            throw new OtpVerificationException(
                    "Email is already verified"
            );
        }

        String emailOtp = otpService.generateOtp(
                normalizedEmail,
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        emailService.sendOtp(
                normalizedEmail,
                emailOtp,
                "Registration"
        );

        return PendingRegistrationResponse.builder()
                .message("A new email OTP has been sent successfully")
                .mobileOtpSent(false)
                .emailOtpSent(true)
                .build();
    }

    @Transactional
    public VerifyOtpResponse verifyMobileOtp(
            String phoneNumber,
            String otp) {

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByPhoneNumber(phoneNumber)
                        .orElseThrow(() ->
                                new OtpVerificationException(
                                        "Registration request not found"
                                )
                        );

        validatePendingRegistration(pendingRegistration);

        otpService.verifyOtp(
                phoneNumber,
                OtpChannel.MOBILE,
                OtpPurpose.REGISTRATION,
                otp
        );

        pendingRegistration.setMobileVerified(true);

        pendingRegistrationRepository.save(
                pendingRegistration
        );

        String emailOtp = otpService.generateOtp(
                pendingRegistration.getEmail(),
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        emailService.sendOtp(
                pendingRegistration.getEmail(),
                emailOtp,
                "Registration"
        );

        return VerifyOtpResponse.builder()
                .message(
                        "Mobile OTP verified. Email OTP sent successfully"
                )
                .verified(true)
                .nextStepRequired(true)
                .build();
    }

    @Transactional
    public VerifyOtpResponse verifyEmailOtp(
            String email,
            String otp) {

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new OtpVerificationException(
                                        "Registration request not found"
                                )
                        );

        validatePendingRegistration(pendingRegistration);

        otpService.verifyOtp(
                email,
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION,
                otp
        );

        pendingRegistration.setEmailVerified(true);

        pendingRegistrationRepository.save(
                pendingRegistration
        );

        return VerifyOtpResponse.builder()
                .message("Email OTP verified successfully")
                .verified(true)
                .nextStepRequired(false)
                .build();
    }

    private void validatePendingRegistration(
            PendingRegistration pendingRegistration) {

        if (LocalDateTime.now().isAfter(
                pendingRegistration.getExpiresAt())) {

            pendingRegistrationRepository.delete(
                    pendingRegistration
            );

            throw new OtpVerificationException(
                    "Registration request has expired. Please register again."
            );
        }
    }
}