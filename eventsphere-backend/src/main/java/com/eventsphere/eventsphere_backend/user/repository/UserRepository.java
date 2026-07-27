package com.eventsphere.eventsphere_backend.user.repository;

import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by email (used for login and registration)
    Optional<User> findByEmail(String email);

    // Check if an email already exists
    boolean existsByEmail(String email);

    // Find all users with a specific role
    List<User> findByRole(Role role);

}