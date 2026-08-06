package com.eventsphere.eventsphere_backend.review.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for an event
    List<Review> findByEventOrderByCreatedAtDesc(Event event);

    // Find all reviews written by a user
    List<Review> findByUserOrderByCreatedAtDesc(User user);

    // Find a user's review for a particular event
    Optional<Review> findByUserAndEvent(User user, Event event);

    // Check whether the user has already reviewed the event
    boolean existsByUserAndEvent(User user, Event event);

    // Calculate average rating for an event
    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.event.id = :eventId
            """)
    Double getAverageRating(Long eventId);

    // Count reviews for an event
    long countByEvent(Event event);
}