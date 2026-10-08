package com.eventsphere.eventsphere_backend.auth.service;

import com.eventsphere.eventsphere_backend.auth.dto.*;
import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistrationOtpService registrationOtpService;
    private final LoginOtpService loginOtpService;
    private final ForgotPasswordOtpService forgotPasswordOtpService;

    public AuthService(
            UserRepository userRepository,
            PendingRegistrationRepository pendingRegistrationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RegistrationOtpService registrationOtpService,
            LoginOtpService loginOtpService,
            ForgotPasswordOtpService forgotPasswordOtpService) {

        this.userRepository = userRepository;
        this.pendingRegistrationRepository =
                pendingRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registrationOtpService =
                registrationOtpService;
        this.loginOtpService =
                loginOtpService;
        this.forgotPasswordOtpService =
                forgotPasswordOtpService;
    }

    @Transactional
    public PendingRegistrationResponse register(
            RegisterRequest request) {

        if (!request.getPassword().equals(
                request.getConfirmPassword())) {

            throw new PasswordMismatchException();
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String phoneNumber =
                request.getPhoneNumber()
                        .trim();

        if (userRepository.existsByEmail(email)
                || pendingRegistrationRepository
                .existsByEmail(email)) {

            throw new UserEmailAlreadyExistsException(
                    email
            );
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)
                || pendingRegistrationRepository
                .existsByPhoneNumber(phoneNumber)) {

            throw new UserPhoneAlreadyExistsException(
                    phoneNumber
            );
        }

        pendingRegistrationRepository
                .findByEmail(email)
                .ifPresent(
                        pendingRegistrationRepository::delete
                );

        pendingRegistrationRepository
                .findByPhoneNumber(phoneNumber)
                .ifPresent(
                        pendingRegistrationRepository::delete
                );

        PendingRegistration pendingRegistration =
                PendingRegistration.builder()
                        .name(request.getName().trim())
                        .email(email)
                        .phoneNumber(phoneNumber)
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .mobileVerified(false)
                        .emailVerified(false)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(10)
                        )
                        .createdAt(LocalDateTime.now())
                        .build();

        PendingRegistration savedRegistration =
                pendingRegistrationRepository.save(
                        pendingRegistration
                );

        return registrationOtpService
                .sendRegistrationOtp(
                        savedRegistration
                );
    }

    @Transactional
    public VerifyOtpResponse verifyMobileRegistrationOtp(
            String phoneNumber,
            String otp) {

        return registrationOtpService.verifyMobileOtp(
                phoneNumber.trim(),
                otp
        );
    }

    @Transactional
    public RegisterResponse verifyEmailRegistrationOtp(
            String email,
            String otp) {

        String normalizedEmail =
                email.trim().toLowerCase();

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        VerifyOtpResponse verificationResponse =
                registrationOtpService.verifyEmailOtp(
                        normalizedEmail,
                        otp
                );

        if (!verificationResponse.isVerified()) {
            throw new InvalidCredentialsException();
        }

        if (!pendingRegistration.isMobileVerified()
                || !pendingRegistration.isEmailVerified()) {

            throw new InvalidCredentialsException();
        }

        if (userRepository.existsByEmail(
                pendingRegistration.getEmail())) {

            throw new UserEmailAlreadyExistsException(
                    pendingRegistration.getEmail()
            );
        }

        if (userRepository.existsByPhoneNumber(
                pendingRegistration.getPhoneNumber())) {

            throw new UserPhoneAlreadyExistsException(
                    pendingRegistration.getPhoneNumber()
            );
        }

        User user = User.builder()
                .name(pendingRegistration.getName())
                .email(pendingRegistration.getEmail())
                .password(pendingRegistration.getPassword())
                .phoneNumber(
                        pendingRegistration.getPhoneNumber()
                )
                .role(Role.USER)
                .build();

        User savedUser =
                userRepository.save(user);

        pendingRegistrationRepository.delete(
                pendingRegistration
        );

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginOtpResponse login(
            LoginRequest request) {

        String identifier =
                request.getIdentifier()
                        .trim();

        User user;

        if (identifier.matches("^[6-9]\\d{9}$")) {

            user = userRepository
                    .findByPhoneNumber(identifier)
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );

        } else {

            user = userRepository
                    .findByEmail(
                            identifier.toLowerCase()
                    )
                    .orElseThrow(
                            InvalidCredentialsException::new
                    );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException();
        }

        return loginOtpService.sendLoginOtp(
                user.getEmail(),
                null
        );
    }

    @Transactional
    public AuthResponse verifyLoginOtp(
            String email,
            String otp) {

        String normalizedEmail =
                email.trim().toLowerCase();

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        loginOtpService.verifyLoginOtp(
                normalizedEmail,
                OtpChannel.EMAIL,
                otp
        );

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public ForgotPasswordOtpResponse forgotPassword(
            ForgotPasswordRequest request) {

        return forgotPasswordOtpService.sendOtp(
                request.getIdentifier()
        );
    }

    @Transactional
    public void resetPassword(
            ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword())) {

            throw new PasswordMismatchException();
        }

        User user = forgotPasswordOtpService.verifyOtp(
                request.getIdentifier(),
                request.getOtp()
        );

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword())) {

            throw new PasswordMismatchException();
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException();
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }
}