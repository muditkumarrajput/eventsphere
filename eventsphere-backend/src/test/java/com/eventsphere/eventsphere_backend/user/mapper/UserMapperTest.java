package com.eventsphere.eventsphere_backend.user.mapper;

import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toResponse_shouldMapAllFields() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 29, 12, 30);

        User user = User.builder()
                .id(1L)
                .name("Mudit Kumar")
                .email("mudit@test.com")
                .phoneNumber("9876543210")
                .role(Role.USER)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        UserResponse result = userMapper.toResponse(user);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("Mudit Kumar", result.getName());
        assertEquals("mudit@test.com", result.getEmail());
        assertEquals("9876543210", result.getPhoneNumber());
        assertEquals(Role.USER, result.getRole());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}