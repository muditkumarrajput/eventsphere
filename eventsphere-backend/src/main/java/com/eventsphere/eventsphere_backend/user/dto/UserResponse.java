package com.eventsphere.eventsphere_backend.user.dto;

import com.eventsphere.eventsphere_backend.user.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}