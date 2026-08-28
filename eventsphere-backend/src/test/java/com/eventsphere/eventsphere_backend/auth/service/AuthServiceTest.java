package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.auth.security.JwtService;
import com.eventsphere.eventsphere_backend.common.exception.InvalidCredentialsException;
import com.eventsphere.eventsphere_backend.common.exception.UserEmailAlreadyExistsException;
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


    // =========================================================
    // REGISTER
    // =========================================================

    @Test
    void shouldRegisterUser() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .phoneNumber("9876543210")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        User savedUser = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .role(Role.USER)
                .createdAt(createdAt)
                .build();

        when(userRepository.existsByEmail("john@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse result =
                authService.register(request);

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "John",
                result.getName()
        );

        assertEquals(
                "john@test.com",
                result.getEmail()
        );

        assertEquals(
                "9876543210",
                result.getPhoneNumber()
        );

        assertEquals(
                Role.USER,
                result.getRole()
        );

        assertEquals(
                createdAt,
                result.getCreatedAt()
        );

        verify(userRepository)
                .existsByEmail("john@test.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {

        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
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

        verify(
                passwordEncoder,
                never()
        ).encode(anyString());

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @Test
    void shouldLoginUser() {

        String email = "john@test.com";
        String password = "password123";
        String encodedPassword = "encoded-password";
        String token = "jwt-token";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                password,
                encodedPassword
        )).thenReturn(true);

        when(jwtService.generateToken(email))
                .thenReturn(token);

        AuthResponse result =
                authService.login(request);

        assertNotNull(result);

        assertEquals(
                token,
                result.getToken()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(passwordEncoder)
                .matches(
                        password,
                        encodedPassword
                );

        verify(jwtService)
                .generateToken(email);
    }


    @Test
    void shouldRejectLoginWhenEmailDoesNotExist() {

        String email = "unknown@test.com";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }


    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {

        String email = "john@test.com";
        String password = "wrong-password";
        String encodedPassword = "encoded-password";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                password,
                encodedPassword
        )).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(passwordEncoder)
                .matches(
                        password,
                        encodedPassword
                );

        verifyNoInteractions(jwtService);
    }
}