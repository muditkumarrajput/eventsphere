package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.auth.entity.OtpVerification;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import com.eventsphere.eventsphere_backend.auth.repository.OtpVerificationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private static final int MAX_ATTEMPTS = 5;

    private static final long OTP_EXPIRATION_MINUTES = 5;

    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final OtpVerificationRepository otpVerificationRepository;

    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            OtpVerificationRepository otpVerificationRepository,
            PasswordEncoder passwordEncoder) {

        this.otpVerificationRepository =
                otpVerificationRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    // =========================================================
    // GENERATE OTP
    // =========================================================

    @Transactional
    public String generateOtp(
            String target,
            OtpChannel channel,
            OtpPurpose purpose) {

        LocalDateTime now = LocalDateTime.now();

        OtpVerification latestOtp =
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                target,
                                channel,
                                purpose
                        )
                        .orElse(null);

        // -----------------------------------------------------
        // RESEND COOLDOWN
        // -----------------------------------------------------

        if (latestOtp != null
                && latestOtp.getCreatedAt() != null) {

            long secondsSinceLastOtp =
                    Duration.between(
                            latestOtp.getCreatedAt(),
                            now
                    ).getSeconds();

            if (secondsSinceLastOtp
                    < RESEND_COOLDOWN_SECONDS) {

                throw new OtpVerificationException(
                        "Please wait before requesting another OTP"
                );
            }
        }

        // -----------------------------------------------------
        // INVALIDATE PREVIOUS OTP
        // -----------------------------------------------------

        if (latestOtp != null) {

            latestOtp.setVerified(true);

            otpVerificationRepository.save(
                    latestOtp
            );
        }

        // -----------------------------------------------------
        // GENERATE SECURE 6-DIGIT OTP
        // -----------------------------------------------------

        int otpNumber =
                secureRandom.nextInt(900000) + 100000;

        String otp =
                String.valueOf(otpNumber);

        // -----------------------------------------------------
        // HASH OTP
        // -----------------------------------------------------

        String otpHash =
                passwordEncoder.encode(otp);

        // -----------------------------------------------------
        // CREATE OTP RECORD
        // -----------------------------------------------------

        OtpVerification otpVerification =
                OtpVerification.builder()
                        .target(target)
                        .channel(channel)
                        .purpose(purpose)
                        .otpHash(otpHash)
                        .expiresAt(
                                now.plusMinutes(
                                        OTP_EXPIRATION_MINUTES
                                )
                        )
                        .attempts(0)
                        .verified(false)
                        .build();

        otpVerificationRepository.save(
                otpVerification
        );

        /*
         * IMPORTANT:
         *
         * The OTP is returned only internally so that the
         * email/SMS delivery service can send it.
         *
         * It must NEVER be returned directly from an API.
         */

        return otp;
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @Transactional
    public void verifyOtp(
            String target,
            OtpChannel channel,
            OtpPurpose purpose,
            String otp) {

        OtpVerification otpVerification =
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                target,
                                channel,
                                purpose
                        )
                        .orElseThrow(() ->
                                new OtpVerificationException(
                                        "Invalid or expired OTP"
                                )
                        );

        LocalDateTime now =
                LocalDateTime.now();

        // -----------------------------------------------------
        // CHECK EXPIRATION
        // -----------------------------------------------------

        if (now.isAfter(
                otpVerification.getExpiresAt())) {

            otpVerification.setVerified(true);

            otpVerificationRepository.save(
                    otpVerification
            );

            throw new OtpVerificationException(
                    "Invalid or expired OTP"
            );
        }

        // -----------------------------------------------------
        // CHECK MAX ATTEMPTS
        // -----------------------------------------------------

        if (otpVerification.getAttempts()
                >= MAX_ATTEMPTS) {

            otpVerification.setVerified(true);

            otpVerificationRepository.save(
                    otpVerification
            );

            throw new OtpVerificationException(
                    "Maximum OTP attempts exceeded"
            );
        }

        // -----------------------------------------------------
        // INCREMENT ATTEMPTS
        // -----------------------------------------------------

        otpVerification.setAttempts(
                otpVerification.getAttempts() + 1
        );

        // -----------------------------------------------------
        // VERIFY HASH
        // -----------------------------------------------------

        if (!passwordEncoder.matches(
                otp,
                otpVerification.getOtpHash())) {

            otpVerificationRepository.save(
                    otpVerification
            );

            throw new OtpVerificationException(
                    "Invalid or expired OTP"
            );
        }

        // -----------------------------------------------------
        // OTP VERIFIED
        // -----------------------------------------------------

        otpVerification.setVerified(true);

        otpVerificationRepository.save(
                otpVerification
        );
    }
}
