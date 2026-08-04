package com.eventsphere.eventsphere_backend.dashboard.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardRepository extends JpaRepository<Event, Long> {

    // ===========================
    // Dashboard Summary
    // ===========================

    long countByCreatedBy(User organizer);

    @Query("""
            SELECT COUNT(e)
            FROM Event e
            WHERE e.createdBy = :organizer
            AND e.eventDate > CURRENT_TIMESTAMP
            """)
    long countUpcomingEvents(User organizer);

    @Query("""
            SELECT COUNT(e)
            FROM Event e
            WHERE e.createdBy = :organizer
            AND e.eventDate < CURRENT_TIMESTAMP
            """)
    long countCompletedEvents(User organizer);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.event.createdBy = :organizer
            AND b.bookingStatus = 'CONFIRMED'
            """)
    long countTotalBookings(User organizer);

    @Query("""
            SELECT COALESCE(SUM(b.numberOfTickets),0)
            FROM Booking b
            WHERE b.event.createdBy = :organizer
            AND b.bookingStatus = 'CONFIRMED'
            """)
    Integer sumTicketsSold(User organizer);

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount),0)
            FROM Booking b
            WHERE b.event.createdBy = :organizer
            AND b.bookingStatus = 'CONFIRMED'
            """)
    BigDecimal sumRevenue(User organizer);

    // ===========================
    // Event Insights
    // ===========================

    @Query("""
        SELECT
            e.id,
            e.title,
            e.capacity,
            COALESCE(SUM(b.numberOfTickets),0),
            e.capacity - COALESCE(SUM(b.numberOfTickets),0),
            CASE
                WHEN e.capacity = 0 THEN 0.0
                ELSE (COALESCE(SUM(b.numberOfTickets),0) * 100.0 / e.capacity)
            END,
            COALESCE(SUM(b.totalAmount),0)
        FROM Event e
        LEFT JOIN e.bookings b
             ON b.bookingStatus = 'CONFIRMED'
        WHERE e.createdBy = :organizer
        GROUP BY
            e.id,
            e.title,
            e.capacity,
            e.eventDate
        ORDER BY e.eventDate ASC
        """)
    List<Object[]> getEventInsights(User organizer);
}