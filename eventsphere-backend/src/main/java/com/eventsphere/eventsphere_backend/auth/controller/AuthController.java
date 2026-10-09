package com.eventsphere.eventsphere_backend.auth.controller;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginOtpResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.PendingRegistrationResponse;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.auth.dto.ResendRegistrationOtpRequest;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpRequest;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpResponse;
import com.eventsphere.eventsphere_backend.auth.dto.ForgotPasswordOtpResponse;
import com.eventsphere.eventsphere_backend.auth.dto.ForgotPasswordRequest;
import com.eventsphere.eventsphere_backend.auth.dto.ResetPasswordRequest;
import com.eventsphere.eventsphere_backend.auth.dto.ChangePasswordRequest;
import com.eventsphere.eventsphere_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<PendingRegistrationResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<PendingRegistrationResponse> resendRegistrationOtp(
            @Valid @RequestBody ResendRegistrationOtpRequest request) {

        return ResponseEntity.ok(
                authService.resendRegistrationOtp(
                        request.getTarget()
                )
        );
    }

    @PostMapping("/register/verify-mobile")
    public ResponseEntity<VerifyOtpResponse> verifyMobileOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyMobileRegistrationOtp(
                        request.getTarget(),
                        request.getOtp()
                )
        );
    }

    @PostMapping("/register/verify-email")
    public ResponseEntity<RegisterResponse> verifyEmailOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyEmailRegistrationOtp(
                        request.getTarget(),
                        request.getOtp()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyLoginOtp(
                        request.getTarget(),
                        request.getOtp()
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordOtpResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        authService.changePassword(
                authentication.getName(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}