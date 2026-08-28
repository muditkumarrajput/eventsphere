package com.eventsphere.eventsphere_backend.payment.controller;

import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "Payments",
        description = "APIs for creating and managing event payments"
)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @PostMapping
    @Operation(
            summary = "Create payment",
            description = "Creates a payment for the authenticated user's booking"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment already exists or invalid payment state"
            )
    })
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieves a payment belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public PaymentResponse getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        return paymentService.getPaymentById(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // MARK PAYMENT AS SUCCESSFUL
    // =========================================================

    @PatchMapping("/{id}/success")
    @Operation(
            summary = "Mark payment as successful",
            description = "Marks the specified payment as successful for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment marked as successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid payment state transition"
            )
    })
    public PaymentResponse markPaymentSuccessful(
            @PathVariable Long id,
            Authentication authentication) {

        return paymentService.markPaymentSuccessful(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // MARK PAYMENT AS FAILED
    // =========================================================

    @PatchMapping("/{id}/failure")
    @Operation(
            summary = "Mark payment as failed",
            description = "Marks the specified payment as failed for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment marked as failed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid payment state transition"
            )
    })
    public PaymentResponse markPaymentFailed(
            @PathVariable Long id,
            Authentication authentication) {

        return paymentService.markPaymentFailed(
                id,
                authentication.getName()
        );
    }
}