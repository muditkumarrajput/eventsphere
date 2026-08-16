package com.eventsphere.eventsphere_backend.payment.controller;

import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create Payment
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            org.springframework.security.core.Authentication authentication) {

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // Get Payment by ID
    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        return paymentService.getPaymentById(
                id,
                authentication.getName()
        );
    }

    // Mark Payment as Successful
    @PatchMapping("/{id}/success")
    public PaymentResponse markPaymentSuccessful(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        return paymentService.markPaymentSuccessful(
                id,
                authentication.getName()
        );
    }

    // Mark Payment as Failed
    @PatchMapping("/{id}/failure")
    public PaymentResponse markPaymentFailed(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        return paymentService.markPaymentFailed(
                id,
                authentication.getName()
        );
    }
}