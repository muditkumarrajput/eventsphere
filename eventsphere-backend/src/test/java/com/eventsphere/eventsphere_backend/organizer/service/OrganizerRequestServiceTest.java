package com.eventsphere.eventsphere_backend.organizer.service;

import com.eventsphere.eventsphere_backend.common.exception.OrganizerRequestAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.OrganizerRequestAlreadyProcessedException;
import com.eventsphere.eventsphere_backend.common.exception.OrganizerRequestNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.organizer.dto.CreateOrganizerRequest;
import com.eventsphere.eventsphere_backend.organizer.dto.OrganizerRequestResponse;
import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequest;
import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequestStatus;
import com.eventsphere.eventsphere_backend.organizer.repository.OrganizerRequestRepository;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizerRequestServiceTest {

    @Mock
    private OrganizerRequestRepository organizerRequestRepository;

    @Mock
    private UserRepository userRepository;

    private OrganizerRequestService organizerRequestService;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {

        organizerRequestService =
                new OrganizerRequestService(
                        organizerRequestRepository,
                        userRepository
                );

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setRole(Role.USER);

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
    }

    @Test
    void createRequest_shouldCreatePendingRequest() {

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        request.setReason(
                "I want to organize events."
        );

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(organizerRequestRepository
                .findByUserAndStatus(
                        user,
                        OrganizerRequestStatus.PENDING
                ))
                .thenReturn(Optional.empty());

        when(organizerRequestRepository.save(any(
                OrganizerRequest.class
        ))).thenAnswer(invocation -> {

            OrganizerRequest saved =
                    invocation.getArgument(0);

            saved.setId(1L);

            return saved;
        });

        OrganizerRequestResponse response =
                organizerRequestService.createRequest(
                        "user@example.com",
                        request
                );

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(
                "user@example.com",
                response.getUserEmail()
        );
        assertEquals(
                "I want to organize events.",
                response.getReason()
        );
        assertEquals(
                OrganizerRequestStatus.PENDING,
                response.getStatus()
        );

        verify(organizerRequestRepository)
                .save(any(OrganizerRequest.class));
    }

    @Test
    void createRequest_shouldRejectDuplicatePendingRequest() {

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        request.setReason(
                "I want to organize events."
        );

        OrganizerRequest existingRequest =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("Existing request")
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(organizerRequestRepository
                .findByUserAndStatus(
                        user,
                        OrganizerRequestStatus.PENDING
                ))
                .thenReturn(Optional.of(existingRequest));

        assertThrows(
                OrganizerRequestAlreadyExistsException.class,
                () -> organizerRequestService.createRequest(
                        "user@example.com",
                        request
                )
        );

        verify(organizerRequestRepository, never())
                .save(any(OrganizerRequest.class));
    }

    @Test
    void createRequest_shouldRejectNonUserRole() {

        user.setRole(Role.ORGANIZER);

        CreateOrganizerRequest request =
                new CreateOrganizerRequest();

        request.setReason(
                "I want to organize events."
        );

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                OrganizerRequestAlreadyExistsException.class,
                () -> organizerRequestService.createRequest(
                        "user@example.com",
                        request
                )
        );

        verify(
                organizerRequestRepository,
                never()
        ).save(any(OrganizerRequest.class));
    }

    @Test
    void getMyRequests_shouldReturnUserRequests() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(organizerRequestRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(request));

        List<OrganizerRequestResponse> responses =
                organizerRequestService.getMyRequests(
                        "user@example.com"
                );

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(
                OrganizerRequestStatus.PENDING,
                responses.get(0).getStatus()
        );
    }

    @Test
    void getAllRequests_shouldReturnAllRequests() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        when(organizerRequestRepository.findAll())
                .thenReturn(List.of(request));

        List<OrganizerRequestResponse> responses =
                organizerRequestService.getAllRequests();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(
                "user@example.com",
                responses.get(0).getUserEmail()
        );
    }

    @Test
    void approveRequest_shouldChangeUserRoleToOrganizer() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        when(organizerRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));

        when(userRepository.save(user))
                .thenReturn(user);

        when(organizerRequestRepository.save(request))
                .thenReturn(request);

        OrganizerRequestResponse response =
                organizerRequestService.approveRequest(
                        1L,
                        "admin@example.com"
                );

        assertEquals(
                Role.ORGANIZER,
                user.getRole()
        );

        assertEquals(
                OrganizerRequestStatus.APPROVED,
                request.getStatus()
        );

        assertNotNull(request.getReviewedAt());
        assertEquals(admin, request.getReviewedBy());

        assertEquals(
                OrganizerRequestStatus.APPROVED,
                response.getStatus()
        );

        verify(userRepository).save(user);
        verify(organizerRequestRepository)
                .save(request);
    }

    @Test
    void rejectRequest_shouldKeepUserRoleUnchanged() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        when(organizerRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));

        when(organizerRequestRepository.save(request))
                .thenReturn(request);

        OrganizerRequestResponse response =
                organizerRequestService.rejectRequest(
                        1L,
                        "admin@example.com"
                );

        assertEquals(Role.USER, user.getRole());

        assertEquals(
                OrganizerRequestStatus.REJECTED,
                request.getStatus()
        );

        assertNotNull(request.getReviewedAt());
        assertEquals(admin, request.getReviewedBy());

        assertEquals(
                OrganizerRequestStatus.REJECTED,
                response.getStatus()
        );

        verify(userRepository, never()).save(user);

        verify(organizerRequestRepository)
                .save(request);
    }

    @Test
    void approveRequest_shouldRejectAlreadyProcessedRequest() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.APPROVED)
                        .build();

        when(organizerRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThrows(
                OrganizerRequestAlreadyProcessedException.class,
                () -> organizerRequestService.approveRequest(
                        1L,
                        "admin@example.com"
                )
        );

        verify(userRepository, never())
                .findByEmail("admin@example.com");

        verify(organizerRequestRepository, never())
                .save(any(OrganizerRequest.class));
    }

    @Test
    void rejectRequest_shouldRejectAlreadyProcessedRequest() {

        OrganizerRequest request =
                OrganizerRequest.builder()
                        .id(1L)
                        .user(user)
                        .reason("I want to organize events.")
                        .status(OrganizerRequestStatus.REJECTED)
                        .build();

        when(organizerRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThrows(
                OrganizerRequestAlreadyProcessedException.class,
                () -> organizerRequestService.rejectRequest(
                        1L,
                        "admin@example.com"
                )
        );

        verify(userRepository, never())
                .findByEmail("admin@example.com");

        verify(organizerRequestRepository, never())
                .save(any(OrganizerRequest.class));
    }

    @Test
    void approveRequest_shouldThrowWhenRequestDoesNotExist() {

        when(organizerRequestRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrganizerRequestNotFoundException.class,
                () -> organizerRequestService.approveRequest(
                        99L,
                        "admin@example.com"
                )
        );
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> organizerRequestService.getMyRequests(
                        "unknown@example.com"
                )
        );
    }
}
