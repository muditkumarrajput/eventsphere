package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.auth.entity.OtpVerification;
import com.eventsphere.eventsphere_backend.auth.repository.OtpVerificationRepository;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpService otpService;

    @Test
    void shouldGenerateOtpSuccessfully() {

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.empty());

        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("hashed-otp");

        String otp = otpService.generateOtp(
                "test@example.com",
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        verify(passwordEncoder).encode(otp);
        verify(otpVerificationRepository).save(any(OtpVerification.class));
    }

    @Test
    void shouldRejectOtpResendDuringCooldown() {

        OtpVerification existingOtp = OtpVerification.builder()
                .target("test@example.com")
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash("hashed-otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.of(existingOtp));

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> otpService.generateOtp(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
                );

        assertEquals(
                "Please wait before requesting another OTP",
                exception.getMessage()
        );

        verify(otpVerificationRepository, never())
                .save(any(OtpVerification.class));
    }

    @Test
    void shouldVerifyOtpSuccessfully() {

        OtpVerification otpVerification = OtpVerification.builder()
                .target("test@example.com")
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash("hashed-otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.of(otpVerification));

        when(passwordEncoder.matches("123456", "hashed-otp"))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                otpService.verifyOtp(
                        "test@example.com",
                        OtpChannel.EMAIL,
                        OtpPurpose.REGISTRATION,
                        "123456"
                )
        );

        assertTrue(otpVerification.isVerified());
        assertEquals(1, otpVerification.getAttempts());

        verify(passwordEncoder)
                .matches("123456", "hashed-otp");

        verify(otpVerificationRepository)
                .save(otpVerification);
    }

    @Test
    void shouldRejectInvalidOtp() {

        OtpVerification otpVerification = OtpVerification.builder()
                .target("test@example.com")
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash("hashed-otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.of(otpVerification));

        when(passwordEncoder.matches("999999", "hashed-otp"))
                .thenReturn(false);

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> otpService.verifyOtp(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION,
                                "999999"
                        )
                );

        assertEquals(
                "Invalid or expired OTP",
                exception.getMessage()
        );

        assertEquals(1, otpVerification.getAttempts());
        assertFalse(otpVerification.isVerified());

        verify(passwordEncoder)
                .matches("999999", "hashed-otp");

        verify(otpVerificationRepository)
                .save(otpVerification);
    }

    @Test
    void shouldRejectExpiredOtp() {

        OtpVerification otpVerification = OtpVerification.builder()
                .target("test@example.com")
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash("hashed-otp")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .attempts(0)
                .verified(false)
                .createdAt(LocalDateTime.now().minusMinutes(6))
                .build();

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.of(otpVerification));

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> otpService.verifyOtp(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION,
                                "123456"
                        )
                );

        assertEquals(
                "Invalid or expired OTP",
                exception.getMessage()
        );

        assertTrue(otpVerification.isVerified());

        verify(otpVerificationRepository)
                .save(otpVerification);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectOtpAfterMaximumAttempts() {

        OtpVerification otpVerification = OtpVerification.builder()
                .target("test@example.com")
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash("hashed-otp")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(5)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.of(otpVerification));

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> otpService.verifyOtp(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION,
                                "123456"
                        )
                );

        assertEquals(
                "Maximum OTP attempts exceeded",
                exception.getMessage()
        );

        assertTrue(otpVerification.isVerified());
        assertEquals(5, otpVerification.getAttempts());

        verify(otpVerificationRepository)
                .save(otpVerification);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectAlreadyVerifiedOtp() {

        when(
                otpVerificationRepository
                        .findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION
                        )
        ).thenReturn(Optional.empty());

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> otpService.verifyOtp(
                                "test@example.com",
                                OtpChannel.EMAIL,
                                OtpPurpose.REGISTRATION,
                                "123456"
                        )
                );

        assertEquals(
                "Invalid or expired OTP",
                exception.getMessage()
        );

        verifyNoInteractions(passwordEncoder);
    }
}
