package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.PendingRegistrationResponse;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpResponse;
import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.auth.entity.PendingRegistration;
import com.eventsphere.eventsphere_backend.auth.repository.PendingRegistrationRepository;
import com.eventsphere.eventsphere_backend.common.exception.OtpVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationOtpServiceTest {

    @Mock
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private SmsService smsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationOtpService registrationOtpService;

    @Test
    void shouldSendMobileOtpSuccessfully() {

        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(otpService.generateOtp(
                pendingRegistration.getPhoneNumber(),
                OtpChannel.MOBILE,
                OtpPurpose.REGISTRATION
        )).thenReturn("123456");

        PendingRegistrationResponse response =
                registrationOtpService.sendRegistrationOtp(
                        pendingRegistration
                );

        assertEquals(
                "Mobile OTP sent successfully",
                response.getMessage()
        );

        assertTrue(response.isMobileOtpSent());
        assertFalse(response.isEmailOtpSent());

        verify(otpService).generateOtp(
                pendingRegistration.getPhoneNumber(),
                OtpChannel.MOBILE,
                OtpPurpose.REGISTRATION
        );

        verify(smsService).sendOtp(
                pendingRegistration.getPhoneNumber(),
                "123456",
                "Registration"
        );

        verifyNoInteractions(emailService);
    }

    @Test
    void shouldVerifyMobileOtpAndSendEmailOtp() {

        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(
                pendingRegistrationRepository
                        .findByPhoneNumber("9876543210")
        ).thenReturn(
                Optional.of(pendingRegistration)
        );

        when(otpService.generateOtp(
                pendingRegistration.getEmail(),
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        )).thenReturn("654321");

        VerifyOtpResponse response =
                registrationOtpService.verifyMobileOtp(
                        "9876543210",
                        "123456"
                );

        assertEquals(
                "Mobile OTP verified. Email OTP sent successfully",
                response.getMessage()
        );

        assertTrue(response.isVerified());
        assertTrue(response.isNextStepRequired());
        assertTrue(pendingRegistration.isMobileVerified());

        verify(otpService).verifyOtp(
                "9876543210",
                OtpChannel.MOBILE,
                OtpPurpose.REGISTRATION,
                "123456"
        );

        verify(pendingRegistrationRepository)
                .save(pendingRegistration);

        verify(otpService).generateOtp(
                pendingRegistration.getEmail(),
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        verify(emailService).sendOtp(
                pendingRegistration.getEmail(),
                "654321",
                "Registration"
        );
    }

    @Test
    void shouldVerifyEmailOtpSuccessfully() {

        PendingRegistration pendingRegistration =
                createPendingRegistration();

        pendingRegistration.setMobileVerified(true);

        when(
                pendingRegistrationRepository
                        .findByEmail("test@example.com")
        ).thenReturn(
                Optional.of(pendingRegistration)
        );

        VerifyOtpResponse response =
                registrationOtpService.verifyEmailOtp(
                        "test@example.com",
                        "654321"
                );

        assertEquals(
                "Email OTP verified successfully",
                response.getMessage()
        );

        assertTrue(response.isVerified());
        assertFalse(response.isNextStepRequired());
        assertTrue(pendingRegistration.isEmailVerified());

        verify(otpService).verifyOtp(
                "test@example.com",
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION,
                "654321"
        );

        verify(pendingRegistrationRepository)
                .save(pendingRegistration);
    }

    @Test
    void shouldRejectEmailOtpBeforeMobileVerification() {

        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(
                pendingRegistrationRepository
                        .findByEmail("test@example.com")
        ).thenReturn(
                Optional.of(pendingRegistration)
        );

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> registrationOtpService.verifyEmailOtp(
                                "test@example.com",
                                "654321"
                        )
                );

        assertEquals(
                "Mobile OTP must be verified first",
                exception.getMessage()
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldRejectExpiredRegistration() {

        PendingRegistration pendingRegistration =
                createPendingRegistration();

        pendingRegistration.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(
                pendingRegistrationRepository
                        .findByPhoneNumber("9876543210")
        ).thenReturn(
                Optional.of(pendingRegistration)
        );

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> registrationOtpService.verifyMobileOtp(
                                "9876543210",
                                "123456"
                        )
                );

        assertEquals(
                "Registration request has expired",
                exception.getMessage()
        );

        verify(pendingRegistrationRepository)
                .delete(pendingRegistration);

        verifyNoInteractions(otpService);
        verifyNoInteractions(smsService);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldRejectWhenRegistrationDoesNotExist() {

        when(
                pendingRegistrationRepository
                        .findByPhoneNumber("9876543210")
        ).thenReturn(Optional.empty());

        OtpVerificationException exception =
                assertThrows(
                        OtpVerificationException.class,
                        () -> registrationOtpService.verifyMobileOtp(
                                "9876543210",
                                "123456"
                        )
                );

        assertEquals(
                "Registration request not found",
                exception.getMessage()
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(smsService);
        verifyNoInteractions(emailService);
    }

    private PendingRegistration createPendingRegistration() {

        return PendingRegistration.builder()
                .id(1L)
                .name("John Doe")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .password("hashed-password")
                .mobileVerified(false)
                .emailVerified(false)
                .expiresAt(
                        LocalDateTime.now().plusMinutes(10)
                )
                .createdAt(LocalDateTime.now())
                .build();
    }
}