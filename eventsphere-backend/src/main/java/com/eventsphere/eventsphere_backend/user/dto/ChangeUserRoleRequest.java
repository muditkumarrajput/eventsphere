package com.eventsphere.eventsphere_backend.user.dto;

import com.eventsphere.eventsphere_backend.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}