package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.LoginOtpResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.PendingRegistrationResponse;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpResponse;
import com.eventsphere.eventsphere_backend.auth.entity.PendingRegistration;
import com.eventsphere.eventsphere_backend.auth.repository.PendingRegistrationRepository;
import com.eventsphere.eventsphere_backend.auth.security.JwtService;
import com.eventsphere.eventsphere_backend.common.exception.InvalidCredentialsException;
import com.eventsphere.eventsphere_backend.common.exception.PasswordMismatchException;
import com.eventsphere.eventsphere_backend.common.exception.UserEmailAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.UserPhoneAlreadyExistsException;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
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
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RegistrationOtpService registrationOtpService;

    @Mock
    private LoginOtpService loginOtpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldStartRegistrationSuccessfully() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .confirmPassword("password123")
                .phoneNumber("9876543210")
                .build();

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);

        when(
                pendingRegistrationRepository
                        .existsByEmail("john@test.com")
        ).thenReturn(false);

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);

        when(
                pendingRegistrationRepository
                        .existsByPhoneNumber("9876543210")
        ).thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        PendingRegistration savedRegistration =
                PendingRegistration.builder()
                        .id(1L)
                        .name("John")
                        .email("john@test.com")
                        .phoneNumber("9876543210")
                        .password("hashed-password")
                        .mobileVerified(false)
                        .emailVerified(false)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(10)
                        )
                        .createdAt(LocalDateTime.now())
                        .build();

        when(
                pendingRegistrationRepository.save(
                        any(PendingRegistration.class)
                )
        ).thenReturn(savedRegistration);

        PendingRegistrationResponse expectedResponse =
                PendingRegistrationResponse.builder()
                        .message(
                                "Mobile OTP sent successfully"
                        )
                        .mobileOtpSent(true)
                        .emailOtpSent(false)
                        .build();

        when(
                registrationOtpService
                        .sendRegistrationOtp(
                                savedRegistration
                        )
        ).thenReturn(expectedResponse);

        PendingRegistrationResponse response =
                authService.register(request);

        assertNotNull(response);

        assertEquals(
                "Mobile OTP sent successfully",
                response.getMessage()
        );

        assertTrue(response.isMobileOtpSent());
        assertFalse(response.isEmailOtpSent());

        verify(passwordEncoder)
                .encode("password123");

        verify(pendingRegistrationRepository)
                .save(any(PendingRegistration.class));

        verify(registrationOtpService)
                .sendRegistrationOtp(
                        savedRegistration
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectRegistrationWhenPasswordsDoNotMatch() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .confirmPassword("different123")
                .phoneNumber("9876543210")
                .build();

        PasswordMismatchException exception =
                assertThrows(
                        PasswordMismatchException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Password and confirm password do not match",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                pendingRegistrationRepository,
                passwordEncoder,
                registrationOtpService
        );
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .confirmPassword("password123")
                .phoneNumber("9876543210")
                .build();

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(true);

        UserEmailAlreadyExistsException exception =
                assertThrows(
                        UserEmailAlreadyExistsException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "User with email 'john@test.com' already exists",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("john@test.com");

        verifyNoInteractions(
                pendingRegistrationRepository,
                passwordEncoder,
                registrationOtpService
        );
    }

    @Test
    void shouldRejectRegistrationWhenPhoneAlreadyExists() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .confirmPassword("password123")
                .phoneNumber("9876543210")
                .build();

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);

        when(
                pendingRegistrationRepository
                        .existsByEmail("john@test.com")
        ).thenReturn(false);

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(true);

        UserPhoneAlreadyExistsException exception =
                assertThrows(
                        UserPhoneAlreadyExistsException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "User with phone number '9876543210' already exists",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("john@test.com");

        verify(userRepository)
                .existsByPhoneNumber("9876543210");

        verify(pendingRegistrationRepository)
                .existsByEmail("john@test.com");

        verify(pendingRegistrationRepository, never())
                .save(any(PendingRegistration.class));

        verifyNoInteractions(
                passwordEncoder,
                registrationOtpService
        );
    }

    @Test
    void shouldVerifyMobileRegistrationOtp() {

        VerifyOtpResponse expectedResponse =
                VerifyOtpResponse.builder()
                        .message(
                                "Mobile OTP verified. Email OTP sent successfully"
                        )
                        .verified(true)
                        .nextStepRequired(true)
                        .build();

        when(
                registrationOtpService.verifyMobileOtp(
                        "9876543210",
                        "123456"
                )
        ).thenReturn(expectedResponse);

        VerifyOtpResponse response =
                authService.verifyMobileRegistrationOtp(
                        "9876543210",
                        "123456"
                );

        assertEquals(
                expectedResponse,
                response
        );

        verify(registrationOtpService)
                .verifyMobileOtp(
                        "9876543210",
                        "123456"
                );
    }

    @Test
    void shouldCompleteRegistrationAfterEmailOtp() {

        PendingRegistration pendingRegistration =
                PendingRegistration.builder()
                        .id(1L)
                        .name("John")
                        .email("john@test.com")
                        .phoneNumber("9876543210")
                        .password("hashed-password")
                        .mobileVerified(true)
                        .emailVerified(false)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(10)
                        )
                        .createdAt(LocalDateTime.now())
                        .build();

        when(
                pendingRegistrationRepository
                        .findByEmail("john@test.com")
        ).thenReturn(
                Optional.of(pendingRegistration)
        );

        VerifyOtpResponse verificationResponse =
                VerifyOtpResponse.builder()
                        .message(
                                "Email OTP verified successfully"
                        )
                        .verified(true)
                        .nextStepRequired(false)
                        .build();

        when(
                registrationOtpService.verifyEmailOtp(
                        "john@test.com",
                        "123456"
                )
        ).thenAnswer(invocation -> {
            pendingRegistration.setEmailVerified(true);
            return verificationResponse;
        });

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);

        User savedUser = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .phoneNumber("9876543210")
                .password("hashed-password")
                .role(Role.USER)
                .build();

        when(
                userRepository.save(any(User.class))
        ).thenReturn(savedUser);

        RegisterResponse response =
                authService.verifyEmailRegistrationOtp(
                        "john@test.com",
                        "123456"
                );

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "John",
                response.getName()
        );

        assertEquals(
                "john@test.com",
                response.getEmail()
        );

        assertEquals(
                "9876543210",
                response.getPhoneNumber()
        );

        assertEquals(
                Role.USER,
                response.getRole()
        );

        verify(userRepository)
                .save(any(User.class));

        verify(pendingRegistrationRepository)
                .delete(pendingRegistration);
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = LoginRequest.builder()
                .identifier("john@test.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("hashed-password")
                .phoneNumber("9876543210")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        )).thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());

        verify(loginOtpService, never())
                .sendLoginOtp(anyString(), any());

        verify(jwtService)
                .generateToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {

        LoginRequest request = LoginRequest.builder()
                .identifier("john@test.com")
                .password("wrong-password")
                .build();

        User user = User.builder()
                .email("john@test.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(loginOtpService, never())
                .sendLoginOtp(
                        anyString(),
                        anyString()
                );

        verify(jwtService, never())
                .generateToken(
                        anyString(),
                        any(Role.class)
                );
    }
}