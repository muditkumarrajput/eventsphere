package com.eventsphere.eventsphere_backend.auth.controller;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}