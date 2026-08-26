package com.eventsphere.eventsphere_backend.review.mapper;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapper();
    }

    @Test
    void shouldMapReviewToResponse() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 26, 10, 30);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 26, 11, 30);

        User user =
                User.builder()
                        .id(5L)
                        .name("Test User")
                        .email("user@test.com")
                        .build();

        Event event =
                Event.builder()
                        .id(3L)
                        .title("Java Workshop")
                        .build();

        Review review =
                Review.builder()
                        .id(10L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .user(user)
                        .event(event)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        ReviewResponse response =
                reviewMapper.toResponse(review);

        assertNotNull(response);

        assertEquals(10L, response.getId());
        assertEquals(5L, response.getUserId());
        assertEquals(3L, response.getEventId());
        assertEquals(5, response.getRating());
        assertEquals(
                "Excellent workshop!",
                response.getComment()
        );
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}