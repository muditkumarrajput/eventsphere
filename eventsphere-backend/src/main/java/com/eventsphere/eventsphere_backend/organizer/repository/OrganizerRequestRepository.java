package com.eventsphere.eventsphere_backend.organizer.repository;

import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequest;
import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequestStatus;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizerRequestRepository
        extends JpaRepository<OrganizerRequest, Long> {

    Optional<OrganizerRequest> findByUserAndStatus(
            User user,
            OrganizerRequestStatus status
    );

    List<OrganizerRequest> findByStatusOrderByCreatedAtAsc(
            OrganizerRequestStatus status
    );

    List<OrganizerRequest> findByUserOrderByCreatedAtDesc(
            User user
    );
}
