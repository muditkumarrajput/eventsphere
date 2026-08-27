package com.eventsphere.eventsphere_backend.booking.controller;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // =========================================================
    // CREATE BOOKING
    // AUTHENTICATED USERS
    // =========================================================

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public BookingResponse createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {

        return bookingService.createBooking(
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // GET ALL BOOKINGS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getAllBookings() {

        return bookingService.getAllBookings();
    }

    // =========================================================
    // GET MY BOOKINGS
    // AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<BookingResponse> getMyBookings(
            Authentication authentication) {

        return bookingService.getMyBookings(
                authentication.getName()
        );
    }

    // =========================================================
    // GET BOOKING BY ID
    // AUTHENTICATED USERS
    // OWNERSHIP CHECKED IN SERVICE
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BookingResponse getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        return bookingService.getBookingById(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // CANCEL BOOKING
    // AUTHENTICATED USERS
    // OWNERSHIP CHECKED IN SERVICE
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        bookingService.cancelBooking(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}