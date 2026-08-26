package com.eventsphere.eventsphere_backend.user.controller;

import com.eventsphere.eventsphere_backend.auth.security.JwtService;
import com.eventsphere.eventsphere_backend.auth.service.CustomUserDetailsService;
import com.eventsphere.eventsphere_backend.common.exception.GlobalExceptionHandler;
import com.eventsphere.eventsphere_backend.common.exception.UserEmailAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.UserHasEventsException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.user.dto.ChangeUserRoleRequest;
import com.eventsphere.eventsphere_backend.user.dto.UpdateUserRequest;
import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@Import({
        UserControllerTest.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class UserControllerTest {


    // =========================================================
    // MOCK MVC
    // =========================================================

    @Autowired
    private MockMvc mockMvc;


    // =========================================================
    // MOCK USER SERVICE
    // =========================================================

    @MockitoBean
    private UserService userService;


    // =========================================================
    // MOCK JWT SERVICE
    // =========================================================

    @MockitoBean
    private JwtService jwtService;


    // =========================================================
    // MOCK CUSTOM USER DETAILS SERVICE
    // =========================================================

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // =========================================================
    // TEST SECURITY CONFIGURATION
    // =========================================================

    @TestConfiguration
    static class TestSecurityConfig {


        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http) throws Exception {

            http
                    .csrf(csrf -> csrf.disable())

                    .authorizeHttpRequests(auth -> auth

                            // Current user
                            .requestMatchers("/api/users/me")
                            .authenticated()

                            // Admin user APIs
                            .requestMatchers("/api/users/**")
                            .hasRole("ADMIN")

                            // Everything else
                            .anyRequest()
                            .authenticated()
                    );

            return http.build();
        }


        @Bean
        InMemoryUserDetailsManager userDetailsService() {

            UserDetails user = User
                    .withUsername("user@test.com")
                    .password("password")
                    .roles("USER")
                    .build();


            UserDetails admin = User
                    .withUsername("admin")
                    .password("password")
                    .roles("ADMIN")
                    .build();


            UserDetails john = User
                    .withUsername("john@test.com")
                    .password("password")
                    .roles("USER")
                    .build();


            return new InMemoryUserDetailsManager(
                    user,
                    admin,
                    john
            );
        }
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Test
    void shouldGetAllUsers() throws Exception {

        UserResponse user1 = UserResponse.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .build();


        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .name("Alice")
                .email("alice@test.com")
                .build();


        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));


        mockMvc.perform(
                        get("/api/users")
                                .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].email").value("john@test.com"))

                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Alice"))
                .andExpect(jsonPath("$[1].email").value("alice@test.com"));
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    @Test
    void shouldGetCurrentUser() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(5L)
                .name("John")
                .email("john@test.com")
                .build();


        when(userService.getCurrentUser("john@test.com"))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/users/me")
                                .with(user("john@test.com").roles("USER"))
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Test
    void shouldGetUserById() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(5L)
                .name("John")
                .email("john@test.com")
                .build();


        when(userService.getUserById(5L))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/users/5")
                                .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }


    // =========================================================
    // UPDATE CURRENT USER
    // =========================================================

    @Test
    void shouldUpdateCurrentUser() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(5L)
                .name("Updated John")
                .email("updated@test.com")
                .build();


        when(userService.updateUser(
                eq("john@test.com"),
                any(UpdateUserRequest.class)
        ))
                .thenReturn(response);


        mockMvc.perform(
                        put("/api/users/me")
                                .with(user("john@test.com").roles("USER"))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "name": "Updated John",
                                            "email": "updated@test.com",
                                            "phoneNumber": "9999999999"
                                        }
                                        """)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated John"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }


    // =========================================================
    // CHANGE USER ROLE
    // =========================================================

    @Test
    void shouldChangeUserRole() throws Exception {

        UserResponse response = UserResponse.builder()
                .id(5L)
                .name("John")
                .email("john@test.com")
                .role(Role.ORGANIZER)
                .build();


        when(userService.changeUserRole(
                eq(5L),
                any(ChangeUserRoleRequest.class)
        ))
                .thenReturn(response);


        mockMvc.perform(
                        patch("/api/users/5/role")
                                .with(user("admin").roles("ADMIN"))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "role": "ORGANIZER"
                                        }
                                        """)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.role").value("ORGANIZER"));
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @Test
    void shouldDeleteUser() throws Exception {

        doNothing()
                .when(userService)
                .deleteUser(5L);


        mockMvc.perform(
                        delete("/api/users/5")
                                .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$")
                        .value("User deleted successfully"));
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void shouldRejectInvalidUpdateRequest() throws Exception {

        mockMvc.perform(
                        put("/api/users/me")
                                .with(user("john@test.com").roles("USER"))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "name": "",
                                            "email": "",
                                            "phoneNumber": ""
                                        }
                                        """)
                )

                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // SECURITY
    // =========================================================

    @Test
    void shouldForbidNormalUserFromGettingAllUsers() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .with(user("user@test.com").roles("USER"))
                )

                .andExpect(status().isForbidden());
    }


    @Test
    void shouldForbidNormalUserFromGettingUserById() throws Exception {

        mockMvc.perform(
                        get("/api/users/5")
                                .with(user("user@test.com").roles("USER"))
                )

                .andExpect(status().isForbidden());
    }


    @Test
    void shouldForbidNormalUserFromChangingRole() throws Exception {

        mockMvc.perform(
                        patch("/api/users/5/role")
                                .with(user("user@test.com").roles("USER"))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "role": "ORGANIZER"
                                        }
                                        """)
                )

                .andExpect(status().isForbidden());
    }


    @Test
    void shouldForbidNormalUserFromDeletingUser() throws Exception {

        mockMvc.perform(
                        delete("/api/users/5")
                                .with(user("user@test.com").roles("USER"))
                )

                .andExpect(status().isForbidden());
    }


    // =========================================================
    // USER NOT FOUND
    // =========================================================

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        when(userService.getUserById(999L))
                .thenThrow(
                        new UserNotFoundException(999L)
                );


        mockMvc.perform(
                        get("/api/users/999")
                                .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isNotFound());
    }


    // =========================================================
    // EMAIL ALREADY EXISTS
    // =========================================================

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists()
            throws Exception {

        when(userService.updateUser(
                eq("john@test.com"),
                any(UpdateUserRequest.class)
        ))
                .thenThrow(
                        new UserEmailAlreadyExistsException(
                                "another@test.com"
                        )
                );


        mockMvc.perform(
                        put("/api/users/me")
                                .with(user("john@test.com").roles("USER"))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "name": "John",
                                            "email": "another@test.com",
                                            "phoneNumber": "9999999999"
                                        }
                                        """)
                )

                .andExpect(status().isConflict());
    }


    // =========================================================
    // USER HAS EVENTS
    // =========================================================

    @Test
    void shouldReturnConflictWhenUserHasEvents()
            throws Exception {

        doThrow(
                new UserHasEventsException(5L)
        )
                .when(userService)
                .deleteUser(5L);


        mockMvc.perform(
                        delete("/api/users/5")
                                .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isConflict());
    }
}