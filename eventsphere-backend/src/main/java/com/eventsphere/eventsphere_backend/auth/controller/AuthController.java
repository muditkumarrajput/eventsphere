package com.eventsphere.eventsphere_backend.auth.controller;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.service.AuthService;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}