package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.ChangePasswordRequest;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registrationRequest() {
        return RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .confirmPassword("password123")
                .phoneNumber("9876543210")
                .build();
    }

    @Test
    void shouldRegisterUserWithoutOtp() {
        RegisterRequest request = registrationRequest();

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("9876543210", response.getPhoneNumber());
        assertEquals(Role.USER, response.getRole());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNormalizeEmailDuringRegistration() {
        RegisterRequest request = registrationRequest();
        request.setEmail("  JOHN@Test.com  ");

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        RegisterResponse response = authService.register(request);

        assertEquals("john@test.com", response.getEmail());
    }

    @Test
    void shouldRejectRegistrationWhenPasswordsDoNotMatch() {
        RegisterRequest request = registrationRequest();
        request.setConfirmPassword("different123");

        assertThrows(
                PasswordMismatchException.class,
                () -> authService.register(request)
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(true);

        assertThrows(
                UserEmailAlreadyExistsException.class,
                () -> authService.register(registrationRequest())
        );

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByPhoneNumber(anyString());
    }

    @Test
    void shouldRejectRegistrationWhenPhoneAlreadyExists() {
        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(true);

        assertThrows(
                UserPhoneAlreadyExistsException.class,
                () -> authService.register(registrationRequest())
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() {
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
        when(passwordEncoder.matches("password123", "hashed-password"))
                .thenReturn(true);
        when(jwtService.generateToken("john@test.com", Role.USER))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.getToken());
        verify(jwtService).generateToken("john@test.com", Role.USER);
    }

    @Test
    void shouldLoginSuccessfullyWithMobileNumber() {
        LoginRequest request = LoginRequest.builder()
                .identifier("9876543210")
                .password("password123")
                .build();

        User user = User.builder()
                .email("john@test.com")
                .password("hashed-password")
                .phoneNumber("9876543210")
                .role(Role.USER)
                .build();

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password"))
                .thenReturn(true);
        when(jwtService.generateToken("john@test.com", Role.USER))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.getToken());
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
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never()).generateToken(anyString(), any(Role.class));
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() {
        LoginRequest request = LoginRequest.builder()
                .identifier("missing@test.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password123");
        request.setConfirmPassword("new-password123");

        User user = User.builder()
                .email("john@test.com")
                .password("old-hash")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash"))
                .thenReturn(true);
        when(passwordEncoder.encode("new-password123"))
                .thenReturn("new-hash");

        authService.changePassword("john@test.com", request);

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordIsWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong-password");
        request.setNewPassword("new-password123");
        request.setConfirmPassword("new-password123");

        User user = User.builder()
                .email("john@test.com")
                .password("old-hash")
                .build();

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.changePassword("john@test.com", request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectPasswordChangeWhenNewPasswordsDoNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password123");
        request.setConfirmPassword("different-password");

        assertThrows(
                PasswordMismatchException.class,
                () -> authService.changePassword("john@test.com", request)
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }
}
