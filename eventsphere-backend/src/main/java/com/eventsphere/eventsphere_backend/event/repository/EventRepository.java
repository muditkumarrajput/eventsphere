package com.eventsphere.eventsphere_backend.event.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
}