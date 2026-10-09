
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
    void shouldSendEmailOtpSuccessfully() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(otpService.generateOtp(
                "test@example.com",
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        )).thenReturn("123456");

        PendingRegistrationResponse response =
                registrationOtpService.sendRegistrationOtp(
                        pendingRegistration
                );

        assertEquals(
                "Email OTP sent successfully",
                response.getMessage()
        );
        assertFalse(response.isMobileOtpSent());
        assertTrue(response.isEmailOtpSent());

        verify(otpService).generateOtp(
                "test@example.com",
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        );

        verify(emailService).sendOtp(
                "test@example.com",
                "123456",
                "Registration"
        );

        verifyNoInteractions(smsService);
    }

    @Test
    void shouldResendEmailOtpSuccessfully() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(pendingRegistration));

        when(otpService.generateOtp(
                "test@example.com",
                OtpChannel.EMAIL,
                OtpPurpose.REGISTRATION
        )).thenReturn("654321");

        PendingRegistrationResponse response =
                registrationOtpService.resendRegistrationOtp(
                        " TEST@example.com "
                );

        assertEquals(
                "A new email OTP has been sent successfully",
                response.getMessage()
        );
        assertFalse(response.isMobileOtpSent());
        assertTrue(response.isEmailOtpSent());

        verify(emailService).sendOtp(
                "test@example.com",
                "654321",
                "Registration"
        );

        verifyNoInteractions(smsService);
    }

    @Test
    void shouldRejectResendWhenRegistrationDoesNotExist() {
        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.empty());

        OtpVerificationException exception = assertThrows(
                OtpVerificationException.class,
                () -> registrationOtpService.resendRegistrationOtp(
                        "test@example.com"
                )
        );

        assertEquals(
                "Registration request not found. Please register again.",
                exception.getMessage()
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    @Test
    void shouldRejectResendWhenRegistrationHasExpired() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        pendingRegistration.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(pendingRegistration));

        OtpVerificationException exception = assertThrows(
                OtpVerificationException.class,
                () -> registrationOtpService.resendRegistrationOtp(
                        "test@example.com"
                )
        );

        assertEquals(
                "Registration request has expired. Please register again.",
                exception.getMessage()
        );

        verify(pendingRegistrationRepository).delete(
                pendingRegistration
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    @Test
    void shouldVerifyEmailOtpWithoutMobileVerification() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        assertFalse(pendingRegistration.isMobileVerified());

        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(pendingRegistration));

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

        verify(pendingRegistrationRepository).save(
                pendingRegistration
        );

        verifyNoInteractions(smsService);
    }

    @Test
    void shouldRejectExpiredRegistration() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        pendingRegistration.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(pendingRegistration));

        OtpVerificationException exception = assertThrows(
                OtpVerificationException.class,
                () -> registrationOtpService.verifyEmailOtp(
                        "test@example.com",
                        "123456"
                )
        );

        assertEquals(
                "Registration request has expired. Please register again.",
                exception.getMessage()
        );

        verify(pendingRegistrationRepository).delete(
                pendingRegistration
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    @Test
    void shouldRejectWhenEmailRegistrationDoesNotExist() {
        when(pendingRegistrationRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.empty());

        OtpVerificationException exception = assertThrows(
                OtpVerificationException.class,
                () -> registrationOtpService.verifyEmailOtp(
                        "test@example.com",
                        "123456"
                )
        );

        assertEquals(
                "Registration request not found",
                exception.getMessage()
        );

        verifyNoInteractions(otpService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    @Test
    void shouldVerifyLegacyMobileOtpAndSendEmailOtp() {
        PendingRegistration pendingRegistration =
                createPendingRegistration();

        when(pendingRegistrationRepository.findByPhoneNumber(
                "9876543210"
        )).thenReturn(Optional.of(pendingRegistration));

        when(otpService.generateOtp(
                "test@example.com",
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

        verify(pendingRegistrationRepository).save(
                pendingRegistration
        );

        verify(emailService).sendOtp(
                "test@example.com",
                "654321",
                "Registration"
        );
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
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();
    }
}