package com.eventsphere.eventsphere_backend.user.controller;

import com.eventsphere.eventsphere_backend.user.dto.ChangeUserRoleRequest;
import com.eventsphere.eventsphere_backend.user.dto.UpdateUserRequest;
import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================
    // GET ALL USERS - ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================================================
    // GET CURRENT USER
    // AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName()
        );
    }

    // =========================================================
    // GET USER BY ID - ADMIN ONLY
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // =========================================================
    // UPDATE CURRENT USER PROFILE
    // AUTHENTICATED USERS
    // =========================================================

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        return userService.updateUser(
                authentication.getName(),
                request
        );
    }

    // =========================================================
    // CHANGE USER ROLE - ADMIN ONLY
    // =========================================================

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request) {

        return userService.changeUserRole(
                id,
                request
        );
    }

    // =========================================================
    // DELETE USER - ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}