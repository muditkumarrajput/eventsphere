package com.eventsphere.eventsphere_backend.auth.dto;

import com.eventsphere.eventsphere_backend.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private Role role;

    private LocalDateTime createdAt;
}