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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizerRequestService {

    private final OrganizerRequestRepository organizerRequestRepository;
    private final UserRepository userRepository;

    public OrganizerRequestService(
            OrganizerRequestRepository organizerRequestRepository,
            UserRepository userRepository) {

        this.organizerRequestRepository =
                organizerRequestRepository;

        this.userRepository =
                userRepository;
    }

    // =========================================================
    // CREATE ORGANIZER REQUEST
    // USER ONLY
    // =========================================================

    public OrganizerRequestResponse createRequest(
            String email,
            CreateOrganizerRequest request) {

        User user = getUserByEmail(email);

        /*
         * Only normal users should request ORGANIZER access.
         */
        if (user.getRole() != Role.USER) {

            throw new OrganizerRequestAlreadyExistsException(
                    "Only users can request organizer access."
            );
        }

        /*
         * A user can have only one pending organizer request.
         */
        organizerRequestRepository
                .findByUserAndStatus(
                        user,
                        OrganizerRequestStatus.PENDING
                )
                .ifPresent(existingRequest -> {
                    throw new OrganizerRequestAlreadyExistsException(
                            "You already have a pending organizer request."
                    );
                });

        OrganizerRequest organizerRequest =
                OrganizerRequest.builder()
                        .user(user)
                        .reason(request.getReason())
                        .status(OrganizerRequestStatus.PENDING)
                        .build();

        OrganizerRequest savedRequest =
                organizerRequestRepository.save(
                        organizerRequest
                );

        return toResponse(savedRequest);
    }

    // =========================================================
    // GET CURRENT USER REQUESTS
    // =========================================================

    public List<OrganizerRequestResponse> getMyRequests(
            String email) {

        User user = getUserByEmail(email);

        return organizerRequestRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET ALL ORGANIZER REQUESTS
    // ADMIN ONLY
    // =========================================================

    public List<OrganizerRequestResponse> getAllRequests() {

        return organizerRequestRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // APPROVE ORGANIZER REQUEST
    // ADMIN ONLY
    // =========================================================

    @Transactional
    public OrganizerRequestResponse approveRequest(
            Long requestId,
            String adminEmail) {

        OrganizerRequest organizerRequest =
                getRequestById(requestId);

        validatePendingRequest(organizerRequest);

        User admin = getUserByEmail(adminEmail);

        /*
         * Approving the request changes the user's role
         * from USER to ORGANIZER.
         */
        User user = organizerRequest.getUser();

        user.setRole(Role.ORGANIZER);

        userRepository.save(user);

        organizerRequest.setStatus(
                OrganizerRequestStatus.APPROVED
        );

        organizerRequest.setReviewedAt(
                LocalDateTime.now()
        );

        organizerRequest.setReviewedBy(admin);

        OrganizerRequest savedRequest =
                organizerRequestRepository.save(
                        organizerRequest
                );

        return toResponse(savedRequest);
    }

    // =========================================================
    // REJECT ORGANIZER REQUEST
    // ADMIN ONLY
    // =========================================================

    @Transactional
    public OrganizerRequestResponse rejectRequest(
            Long requestId,
            String adminEmail) {

        OrganizerRequest organizerRequest =
                getRequestById(requestId);

        validatePendingRequest(organizerRequest);

        User admin = getUserByEmail(adminEmail);

        /*
         * Rejection does not change the user's role.
         *
         * The user remains USER.
         */
        organizerRequest.setStatus(
                OrganizerRequestStatus.REJECTED
        );

        organizerRequest.setReviewedAt(
                LocalDateTime.now()
        );

        organizerRequest.setReviewedBy(admin);

        OrganizerRequest savedRequest =
                organizerRequestRepository.save(
                        organizerRequest
                );

        return toResponse(savedRequest);
    }

    // =========================================================
    // FIND REQUEST
    // =========================================================

    private OrganizerRequest getRequestById(
            Long requestId) {

        return organizerRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new OrganizerRequestNotFoundException(
                                "Organizer request not found with id: "
                                        + requestId
                        )
                );
    }

    // =========================================================
    // FIND USER
    // =========================================================

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email)
                );
    }

    // =========================================================
    // VALIDATE REQUEST STATUS
    // =========================================================

    private void validatePendingRequest(
            OrganizerRequest organizerRequest) {

        if (organizerRequest.getStatus()
                != OrganizerRequestStatus.PENDING) {

            throw new OrganizerRequestAlreadyProcessedException(
                    "Organizer request has already been processed."
            );
        }
    }

    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private OrganizerRequestResponse toResponse(
            OrganizerRequest organizerRequest) {

        User user = organizerRequest.getUser();
        User reviewedBy = organizerRequest.getReviewedBy();

        return OrganizerRequestResponse.builder()
                .id(organizerRequest.getId())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .reason(organizerRequest.getReason())
                .status(organizerRequest.getStatus())
                .createdAt(organizerRequest.getCreatedAt())
                .reviewedAt(organizerRequest.getReviewedAt())
                .reviewedById(
                        reviewedBy != null
                                ? reviewedBy.getId()
                                : null
                )
                .reviewedByName(
                        reviewedBy != null
                                ? reviewedBy.getName()
                                : null
                )
                .build();
    }
}