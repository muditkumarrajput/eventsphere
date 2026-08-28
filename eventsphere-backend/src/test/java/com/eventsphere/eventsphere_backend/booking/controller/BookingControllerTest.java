package com.eventsphere.eventsphere_backend.booking.controller;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class BookingControllerTest {

    private MockMvc mockMvc;

    private BookingService bookingService;

    private ObjectMapper objectMapper;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        bookingService = mock(BookingService.class);

        objectMapper = new ObjectMapper();

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@test.com");

        BookingController bookingController =
                new BookingController(bookingService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();
    }


    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Test
    void shouldCreateBooking() throws Exception {

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(2)
                        .build();

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .bookingReference("EVT-ABC12345")
                        .numberOfTickets(2)
                        .totalAmount(
                                new BigDecimal("2998.00")
                        )
                        .build();

        when(bookingService.createBooking(
                any(CreateBookingRequest.class),
                eq("user@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/bookings")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.bookingReference")
                                .value("EVT-ABC12345")
                )
                .andExpect(
                        jsonPath("$.numberOfTickets")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalAmount")
                                .value(2998)
                );

        verify(bookingService)
                .createBooking(
                        any(CreateBookingRequest.class),
                        eq("user@test.com")
                );
    }


    // =========================================================
    // GET ALL BOOKINGS
    // =========================================================

    @Test
    void shouldGetAllBookings() throws Exception {

        BookingResponse response1 =
                BookingResponse.builder()
                        .id(1L)
                        .bookingReference("EVT-ABC12345")
                        .build();

        BookingResponse response2 =
                BookingResponse.builder()
                        .id(2L)
                        .bookingReference("EVT-XYZ67890")
                        .build();

        when(bookingService.getAllBookings())
                .thenReturn(
                        List.of(
                                response1,
                                response2
                        )
                );

        mockMvc.perform(
                        get("/api/bookings")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.size()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(2)
                );

        verify(bookingService)
                .getAllBookings();
    }


    // =========================================================
    // GET MY BOOKINGS
    // =========================================================

    @Test
    void shouldGetMyBookings() throws Exception {

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .bookingReference("EVT-ABC12345")
                        .build();

        when(bookingService.getMyBookings(
                "user@test.com"
        )).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/bookings/my")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.size()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].bookingReference")
                                .value("EVT-ABC12345")
                );

        verify(bookingService)
                .getMyBookings(
                        "user@test.com"
                );
    }


    // =========================================================
    // GET BOOKING BY ID
    // =========================================================

    @Test
    void shouldGetBookingById() throws Exception {

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .bookingReference("EVT-ABC12345")
                        .numberOfTickets(2)
                        .totalAmount(
                                new BigDecimal("2998.00")
                        )
                        .build();

        when(bookingService.getBookingById(
                1L,
                "user@test.com"
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/bookings/1")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.bookingReference")
                                .value("EVT-ABC12345")
                )
                .andExpect(
                        jsonPath("$.numberOfTickets")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalAmount")
                                .value(2998)
                );

        verify(bookingService)
                .getBookingById(
                        1L,
                        "user@test.com"
                );
    }


    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    @Test
    void shouldCancelBooking() throws Exception {

        mockMvc.perform(
                        delete("/api/bookings/1")
                                .principal(authentication)
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(bookingService)
                .cancelBooking(
                        1L,
                        "user@test.com"
                );
    }
}
