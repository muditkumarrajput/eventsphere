package com.eventsphere.eventsphere_backend.auth.dto;

import com.eventsphere.eventsphere_backend.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String name;

    private String email;

    private String password;

    private String phoneNumber;

    private Role role;

}