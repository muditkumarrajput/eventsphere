package com.eventsphere.eventsphere_backend.booking.repository;

import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("""
            SELECT COALESCE(SUM(b.numberOfTickets), 0)
            FROM Booking b
            WHERE b.event.id = :eventId
            AND b.bookingStatus = 'CONFIRMED'
            """)
    Integer getBookedTickets(@Param("eventId") Long eventId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    // Check whether user has at least one confirmed booking for an event
    boolean existsByUserAndEventAndBookingStatus(
            User user,
            Event event,
            BookingStatus bookingStatus
    );

    // Check whether an event has any booking at all
    boolean existsByEvent(Event event);
}