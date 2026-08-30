package com.eventsphere.eventsphere_backend.user.repository;

import com.eventsphere.eventsphere_backend.integration.AbstractPostgresIntegrationTest;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByEmail() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());

        assertEquals(
                "Test User",
                result.get().getName()
        );
    }

    @Test
    void shouldCheckIfEmailExists() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("exists@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        userRepository.save(user);

        assertTrue(
                userRepository.existsByEmail(
                        "exists@example.com"
                )
        );

        assertFalse(
                userRepository.existsByEmail(
                        "notfound@example.com"
                )
        );
    }

    @Test
    void shouldFindUsersByRole() {

        User user1 = new User();

        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setRole(Role.USER);

        User user2 = new User();

        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setRole(Role.USER);

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> result =
                userRepository.findByRole(Role.USER);

        assertEquals(2, result.size());
    }
}