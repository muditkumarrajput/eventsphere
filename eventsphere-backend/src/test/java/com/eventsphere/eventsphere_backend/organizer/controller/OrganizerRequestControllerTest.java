package com.eventsphere.eventsphere_backend.organizer.controller;

import com.eventsphere.eventsphere_backend.common.exception.GlobalExceptionHandler;
import com.eventsphere.eventsphere_backend.organizer.dto.CreateOrganizerRequest;
import com.eventsphere.eventsphere_backend.organizer.dto.OrganizerRequestResponse;
import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequestStatus;
import com.eventsphere.eventsphere_backend.organizer.service.OrganizerRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

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

class OrganizerRequestControllerTest {

    private MockMvc mockMvc;

    private OrganizerRequestService organizerRequestService;

    private ObjectMapper objectMapper;

    private Authentication authentication;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        organizerRequestService =
                mock(OrganizerRequestService.class);

        objectMapper =
                new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn(
                        "user@example.com"
                );

        validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        OrganizerRequestController
                organizerRequestController =
                new OrganizerRequestController(
                        organizerRequestService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                organizerRequestController
                        )
                        .setValidator(validator)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();
    }


    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Test
    void shouldCreateOrganizerRequest()
            throws Exception {

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        request.setReason(
                "I want to organize events."
        );

        OrganizerRequestResponse response =
                OrganizerRequestResponse.builder()
                        .id(1L)
                        .userId(5L)
                        .userName("Test User")
                        .userEmail("user@example.com")
                        .reason(
                                "I want to organize events."
                        )
                        .status(
                                OrganizerRequestStatus.PENDING
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        when(
                organizerRequestService.createRequest(
                        any(String.class),
                        any(CreateOrganizerRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/organizer-requests"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.userEmail")
                                .value(
                                        "user@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                );

        verify(
                organizerRequestService
        ).createRequest(
                eq("user@example.com"),
                any(CreateOrganizerRequest.class)
        );
    }


    // =========================================================
    // CREATE REQUEST - VALIDATION
    // =========================================================

    @Test
    void shouldRejectBlankReason()
            throws Exception {

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        request.setReason("");

        mockMvc.perform(
                        post(
                                "/api/organizer-requests"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }


    // =========================================================
    // CREATE REQUEST - MISSING REASON
    // =========================================================

    @Test
    void shouldRejectMissingReason()
            throws Exception {

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        mockMvc.perform(
                        post(
                                "/api/organizer-requests"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }


    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    @Test
    void shouldGetMyRequests()
            throws Exception {

        OrganizerRequestResponse response =
                OrganizerRequestResponse.builder()
                        .id(1L)
                        .userId(5L)
                        .userName("Test User")
                        .userEmail("user@example.com")
                        .reason(
                                "I want to organize events."
                        )
                        .status(
                                OrganizerRequestStatus.PENDING
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        when(
                organizerRequestService
                        .getMyRequests(
                                "user@example.com"
                        )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/api/organizer-requests/me"
                        )
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.size()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].userId")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("PENDING")
                );

        verify(
                organizerRequestService
        ).getMyRequests(
                "user@example.com"
        );
    }


    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @Test
    void shouldGetAllRequests()
            throws Exception {

        OrganizerRequestResponse response =
                OrganizerRequestResponse.builder()
                        .id(1L)
                        .userId(5L)
                        .userName("Test User")
                        .userEmail("user@example.com")
                        .reason(
                                "I want to organize events."
                        )
                        .status(
                                OrganizerRequestStatus.PENDING
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        when(
                organizerRequestService.getAllRequests()
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/api/organizer-requests"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.size()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].userEmail")
                                .value(
                                        "user@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("PENDING")
                );

        verify(
                organizerRequestService
        ).getAllRequests();
    }


    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @Test
    void shouldApproveRequest()
            throws Exception {

        OrganizerRequestResponse response =
                OrganizerRequestResponse.builder()
                        .id(1L)
                        .userId(5L)
                        .userName("Test User")
                        .userEmail("user@example.com")
                        .reason(
                                "I want to organize events."
                        )
                        .status(
                                OrganizerRequestStatus.APPROVED
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .reviewedAt(
                                LocalDateTime.now()
                        )
                        .reviewedById(10L)
                        .reviewedByName(
                                "Admin User"
                        )
                        .build();

        when(
                organizerRequestService.approveRequest(
                        1L,
                        "user@example.com"
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/organizer-requests/1/approve"
                        )
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("APPROVED")
                )
                .andExpect(
                        jsonPath("$.reviewedById")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.reviewedByName")
                                .value("Admin User")
                );

        verify(
                organizerRequestService
        ).approveRequest(
                1L,
                "user@example.com"
        );
    }


    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @Test
    void shouldRejectRequest()
            throws Exception {

        OrganizerRequestResponse response =
                OrganizerRequestResponse.builder()
                        .id(1L)
                        .userId(5L)
                        .userName("Test User")
                        .userEmail("user@example.com")
                        .reason(
                                "I want to organize events."
                        )
                        .status(
                                OrganizerRequestStatus.REJECTED
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .reviewedAt(
                                LocalDateTime.now()
                        )
                        .reviewedById(10L)
                        .reviewedByName(
                                "Admin User"
                        )
                        .build();

        when(
                organizerRequestService.rejectRequest(
                        1L,
                        "user@example.com"
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/organizer-requests/1/reject"
                        )
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("REJECTED")
                )
                .andExpect(
                        jsonPath("$.reviewedById")
                                .value(10)
                );

        verify(
                organizerRequestService
        ).rejectRequest(
                1L,
                "user@example.com"
        );
    }


    // =========================================================
    // GET MY REQUESTS - EMPTY
    // =========================================================

    @Test
    void shouldReturnEmptyListWhenNoMyRequests()
            throws Exception {

        when(
                organizerRequestService
                        .getMyRequests(
                                "user@example.com"
                        )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/organizer-requests/me"
                        )
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.size()")
                                .value(0)
                );

        verify(
                organizerRequestService
        ).getMyRequests(
                "user@example.com"
        );
    }


    // =========================================================
    // GET ALL REQUESTS - EMPTY
    // =========================================================

    @Test
    void shouldReturnEmptyListWhenNoRequests()
            throws Exception {

        when(
                organizerRequestService.getAllRequests()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/organizer-requests"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.size()")
                                .value(0)
                );

        verify(
                organizerRequestService
        ).getAllRequests();
    }
}