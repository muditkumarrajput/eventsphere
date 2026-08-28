package com.eventsphere.eventsphere_backend.user.controller;

import com.eventsphere.eventsphere_backend.user.dto.ChangeUserRoleRequest;
import com.eventsphere.eventsphere_backend.user.dto.UpdateUserRequest;
import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "APIs for managing users and user profiles"
)
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
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all registered users. Accessible only by administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can access this resource"
            )
    })
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================================================
    // GET CURRENT USER
    // AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current user",
            description = "Returns the profile of the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
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
    @Operation(
            summary = "Get user by ID",
            description = "Returns a specific user by ID. Accessible only by administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can access this resource"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
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
    @Operation(
            summary = "Update current user profile",
            description = "Updates the profile information of the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user information"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
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
    @Operation(
            summary = "Change user role",
            description = "Changes the role of a user. Accessible only by administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User role changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role information"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can change user roles"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
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
    @Operation(
            summary = "Delete user",
            description = "Deletes a user by ID. Accessible only by administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can delete users"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User cannot be deleted because of existing events or related data"
            )
    })
    public String deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}