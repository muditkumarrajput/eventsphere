package com.eventsphere.eventsphere_backend.event.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository
        extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    List<Event> findByCreatedBy(User createdBy);

    List<Event> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );

    List<Event> findByCategory(EventCategory category);

    List<Event> findByLocationContainingIgnoreCase(String location);

    List<Event> findByEventDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<Event> findByTicketPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<Event> findByEventDateAfterOrderByEventDateAsc(
            LocalDateTime dateTime
    );

    Page<Event> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e
            FROM Event e
            WHERE e.id = :id
            """)
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
