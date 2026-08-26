package com.eventsphere.eventsphere_backend.payment.controller;

import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.payment.entity.PaymentStatus;
import com.eventsphere.eventsphere_backend.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private MockMvc mockMvc;

    private PaymentService paymentService;

    private ObjectMapper objectMapper;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        paymentService = mock(PaymentService.class);

        objectMapper = new ObjectMapper();

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@test.com");

        PaymentController paymentController =
                new PaymentController(paymentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(paymentController)
                .build();
    }


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Test
    void shouldCreatePayment() throws Exception {

        CreatePaymentRequest request =
                new CreatePaymentRequest(10L);

        PaymentResponse response =
                PaymentResponse.builder()
                        .id(1L)
                        .paymentReference("PAY-ABC12345")
                        .bookingId(10L)
                        .amount(new BigDecimal("2000.00"))
                        .paymentStatus(PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(paymentService.createPayment(
                any(CreatePaymentRequest.class),
                eq("user@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/payments")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.paymentReference")
                                .value("PAY-ABC12345")
                )
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(
                        jsonPath("$.amount")
                                .value(2000.00)
                )
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("PENDING")
                );

        verify(paymentService).createPayment(
                any(CreatePaymentRequest.class),
                eq("user@test.com")
        );
    }


    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    @Test
    void shouldGetPaymentById() throws Exception {

        PaymentResponse response =
                PaymentResponse.builder()
                        .id(1L)
                        .paymentReference("PAY-ABC12345")
                        .bookingId(10L)
                        .amount(new BigDecimal("2000.00"))
                        .paymentStatus(PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(paymentService.getPaymentById(
                1L,
                "user@test.com"
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/payments/1")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.paymentReference")
                                .value("PAY-ABC12345")
                )
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(
                        jsonPath("$.amount")
                                .value(2000.00)
                )
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("PENDING")
                );

        verify(paymentService)
                .getPaymentById(
                        1L,
                        "user@test.com"
                );
    }


    // =========================================================
    // MARK PAYMENT SUCCESSFUL
    // =========================================================

    @Test
    void shouldMarkPaymentSuccessful() throws Exception {

        PaymentResponse response =
                PaymentResponse.builder()
                        .id(1L)
                        .paymentReference("PAY-ABC12345")
                        .bookingId(10L)
                        .amount(new BigDecimal("2000.00"))
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .paymentDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(paymentService.markPaymentSuccessful(
                1L,
                "user@test.com"
        )).thenReturn(response);

        mockMvc.perform(
                        patch("/api/payments/1/success")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.paymentReference")
                                .value("PAY-ABC12345")
                )
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("SUCCESS")
                );

        verify(paymentService)
                .markPaymentSuccessful(
                        1L,
                        "user@test.com"
                );
    }


    // =========================================================
    // MARK PAYMENT FAILED
    // =========================================================

    @Test
    void shouldMarkPaymentFailed() throws Exception {

        PaymentResponse response =
                PaymentResponse.builder()
                        .id(1L)
                        .paymentReference("PAY-ABC12345")
                        .bookingId(10L)
                        .amount(new BigDecimal("2000.00"))
                        .paymentStatus(PaymentStatus.FAILED)
                        .paymentDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(paymentService.markPaymentFailed(
                1L,
                "user@test.com"
        )).thenReturn(response);

        mockMvc.perform(
                        patch("/api/payments/1/failure")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.paymentReference")
                                .value("PAY-ABC12345")
                )
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("FAILED")
                );

        verify(paymentService)
                .markPaymentFailed(
                        1L,
                        "user@test.com"
                );
    }
}