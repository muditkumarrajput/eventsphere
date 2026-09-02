package com.eventsphere.eventsphere_backend.auth.security;

import com.eventsphere.eventsphere_backend.booking.service.BookingService;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import com.eventsphere.eventsphere_backend.integration.AbstractPostgresIntegrationTest;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityConfigTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private BookingService bookingService;

    // =========================================================
    // PUBLIC EVENT API
    // =========================================================

    @Test
    void shouldAllowPublicEventApiWithoutAuthentication()
            throws Exception {

        when(eventService.getAllEvents())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/events")
                )
                .andExpect(status().isOk());
    }

    // =========================================================
    // PROTECTED BOOKING API
    // =========================================================

    @Test
    void shouldReturn401WhenAccessingBookingApiWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "eventId": 3,
                                            "numberOfTickets": 2
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Authentication is required to access this resource"
                                )
                );
    }

    // =========================================================
    // INVALID JWT
    // =========================================================

    @Test
    void shouldReturn401ForInvalidJwt()
            throws Exception {

        when(jwtService.extractEmail("invalid-token"))
                .thenThrow(
                        new RuntimeException("Invalid JWT")
                );

        mockMvc.perform(
                        post("/api/bookings")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "eventId": 3,
                                            "numberOfTickets": 2
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                );
    }

    // =========================================================
    // USER CANNOT ACCESS ADMIN ENDPOINT
    // =========================================================

    @Test
    void shouldReturn403WhenUserAccessesAdminBookingApi()
            throws Exception {

        User user = new User();

        user.setId(5L);
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        when(jwtService.extractEmail("user-token"))
                .thenReturn("user@test.com");

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(
                "user-token",
                "user@test.com"
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/bookings")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You do not have permission to access this resource"
                                )
                );
    }

    // =========================================================
    // ADMIN CAN ACCESS ADMIN ENDPOINT
    // =========================================================

    @Test
    void shouldAllowAdminToAccessBookingApi()
            throws Exception {

        User admin = new User();

        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setRole(Role.ADMIN);

        when(jwtService.extractEmail("admin-token"))
                .thenReturn("admin@test.com");

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(admin));

        when(jwtService.isTokenValid(
                "admin-token",
                "admin@test.com"
        )).thenReturn(true);

        when(bookingService.getAllBookings())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/bookings")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isOk());
    }

    // =========================================================
    // ORGANIZER IS AUTHENTICATED
    // =========================================================

    @Test
    void shouldAllowOrganizerToAccessAuthenticatedEventEndpoint()
            throws Exception {

        User organizer = new User();

        organizer.setId(2L);
        organizer.setEmail("organizer@test.com");
        organizer.setPassword("password");
        organizer.setRole(Role.ORGANIZER);

        when(jwtService.extractEmail(
                "organizer-token"
        )).thenReturn("organizer@test.com");

        when(userRepository.findByEmail(
                "organizer@test.com"
        )).thenReturn(Optional.of(organizer));

        when(jwtService.isTokenValid(
                "organizer-token",
                "organizer@test.com"
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/events")
                                .header(
                                        "Authorization",
                                        "Bearer organizer-token"
                                )
                )
                .andExpect(status().isOk());
    }
}