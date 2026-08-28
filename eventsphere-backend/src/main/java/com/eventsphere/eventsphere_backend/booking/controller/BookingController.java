package com.eventsphere.eventsphere_backend.booking.controller;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(
        name = "Bookings",
        description = "APIs for creating, viewing, and cancelling event bookings"
)
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
    @Operation(
            summary = "Create a booking",
            description = "Creates a new booking for an authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event or user not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Event capacity exceeded"
            )
    })
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
    @Operation(
            summary = "Get all bookings",
            description = "Returns all bookings. Accessible only to administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bookings retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have administrator permissions"
            )
    })
    public List<BookingResponse> getAllBookings() {

        return bookingService.getAllBookings();
    }

    // =========================================================
    // GET MY BOOKINGS
    // AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my bookings",
            description = "Returns all bookings belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User bookings retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
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
    @Operation(
            summary = "Get booking by ID",
            description = "Returns a booking after verifying the authenticated user's access"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have access to this booking"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            )
    })
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
    @Operation(
            summary = "Cancel a booking",
            description = "Cancels a booking after verifying the authenticated user's ownership"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Booking cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have access to this booking"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Booking is already cancelled"
            )
    })
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