package com.eventsphere.eventsphere_backend.review.service;

import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.ReviewAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewNotAllowedException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewOwnershipException;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import com.eventsphere.eventsphere_backend.review.mapper.ReviewMapper;
import com.eventsphere.eventsphere_backend.review.repository.ReviewRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    // =========================================================
    // CREATE REVIEW - SUCCESS
    // =========================================================

    @Test
    void shouldCreateReviewWhenUserHasConfirmedBooking() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setEventId(3L);
        request.setRating(5);
        request.setComment("Excellent workshop!");

        Review savedReview = Review.builder()
                .id(1L)
                .user(user)
                .event(event)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .userId(5L)
                .eventId(3L)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.existsByUserAndEventAndBookingStatus(
                user,
                event,
                BookingStatus.CONFIRMED
        )).thenReturn(true);

        when(reviewRepository.existsByUserAndEvent(user, event))
                .thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenReturn(savedReview);

        when(reviewMapper.toResponse(savedReview))
                .thenReturn(response);

        // Act
        ReviewResponse result =
                reviewService.createReview(request, email);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent workshop!",
                result.getComment()
        );

        verify(reviewRepository)
                .save(any(Review.class));
    }

    // =========================================================
    // CREATE REVIEW - NO CONFIRMED BOOKING
    // =========================================================

    @Test
    void shouldNotCreateReviewWhenUserHasNoConfirmedBooking() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setEventId(3L);
        request.setRating(5);
        request.setComment("Excellent workshop!");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.existsByUserAndEventAndBookingStatus(
                user,
                event,
                BookingStatus.CONFIRMED
        )).thenReturn(false);

        // Act + Assert
        assertThrows(
                ReviewNotAllowedException.class,
                () -> reviewService.createReview(request, email)
        );

        // Review must not be saved
        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    // =========================================================
    // CREATE REVIEW - DUPLICATE REVIEW
    // =========================================================

    @Test
    void shouldNotCreateDuplicateReview() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setEventId(3L);
        request.setRating(5);
        request.setComment("Excellent workshop!");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        // User has a confirmed booking
        when(bookingRepository.existsByUserAndEventAndBookingStatus(
                user,
                event,
                BookingStatus.CONFIRMED
        )).thenReturn(true);

        // User has already reviewed the event
        when(reviewRepository.existsByUserAndEvent(user, event))
                .thenReturn(true);

        // Act + Assert
        assertThrows(
                ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(request, email)
        );

        // Review must not be saved
        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    // =========================================================
// UPDATE REVIEW - SUCCESS
// =========================================================

    @Test
    void shouldUpdateOwnReview() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        Review review = Review.builder()
                .id(1L)
                .user(user)
                .event(event)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setEventId(3L);
        request.setRating(4);
        request.setComment("Very good workshop. Learned a lot!");

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .userId(5L)
                .eventId(3L)
                .rating(4)
                .comment("Very good workshop. Learned a lot!")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        when(reviewRepository.save(review))
                .thenReturn(review);

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        // Act
        ReviewResponse result =
                reviewService.updateReview(
                        1L,
                        request,
                        email
                );

        // Assert
        assertEquals(4, result.getRating());
        assertEquals(
                "Very good workshop. Learned a lot!",
                result.getComment()
        );

        verify(reviewRepository).save(review);
    }


// =========================================================
// UPDATE REVIEW - NOT OWNER
// =========================================================

    @Test
    void shouldNotUpdateReviewOwnedByAnotherUser() {

        // Arrange
        String email = "anotheruser@test.com";

        User anotherUser = new User();
        anotherUser.setId(10L);

        User reviewOwner = new User();
        reviewOwner.setId(5L);

        Event event = new Event();
        event.setId(3L);

        Review review = Review.builder()
                .id(1L)
                .user(reviewOwner)
                .event(event)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setEventId(3L);
        request.setRating(1);
        request.setComment("Trying to change someone else's review");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        // Act + Assert
        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.updateReview(
                        1L,
                        request,
                        email
                )
        );

        // Review must not be saved
        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    // =========================================================
// DELETE REVIEW - SUCCESS
// =========================================================

    @Test
    void shouldDeleteOwnReview() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        Review review = Review.builder()
                .id(1L)
                .user(user)
                .event(event)
                .rating(4)
                .comment("Very good workshop!")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        // Act
        reviewService.deleteReview(1L, email);

        // Assert
        verify(reviewRepository).delete(review);
    }


// =========================================================
// DELETE REVIEW - NOT OWNER
// =========================================================

    @Test
    void shouldNotDeleteReviewOwnedByAnotherUser() {

        // Arrange
        String email = "anotheruser@test.com";

        User anotherUser = new User();
        anotherUser.setId(10L);

        User reviewOwner = new User();
        reviewOwner.setId(5L);

        Event event = new Event();
        event.setId(3L);

        Review review = Review.builder()
                .id(1L)
                .user(reviewOwner)
                .event(event)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        // Act + Assert
        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.deleteReview(1L, email)
        );

        // Review must not be deleted
        verify(reviewRepository, never())
                .delete(any(Review.class));
    }


    // =========================================================
// GET EVENT REVIEWS
// =========================================================

    @Test
    void shouldGetEventReviews() {

        // Arrange
        Event event = new Event();
        event.setId(3L);

        Review review = Review.builder()
                .id(1L)
                .event(event)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .eventId(3L)
                .rating(5)
                .comment("Excellent workshop!")
                .build();

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.findByEventOrderByCreatedAtDesc(event))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        // Act
        List<ReviewResponse> result =
                reviewService.getEventReviews(3L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
        assertEquals(
                "Excellent workshop!",
                result.get(0).getComment()
        );

        verify(reviewRepository)
                .findByEventOrderByCreatedAtDesc(event);
    }


// =========================================================
// GET MY REVIEWS
// =========================================================

    @Test
    void shouldGetMyReviews() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Review review = Review.builder()
                .id(1L)
                .user(user)
                .rating(4)
                .comment("Very good workshop!")
                .build();

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .userId(5L)
                .rating(4)
                .comment("Very good workshop!")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        // Act
        List<ReviewResponse> result =
                reviewService.getMyReviews(email);

        // Assert
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getRating());
        assertEquals(
                "Very good workshop!",
                result.get(0).getComment()
        );

        verify(reviewRepository)
                .findByUserOrderByCreatedAtDesc(user);
    }


// =========================================================
// GET AVERAGE RATING
// =========================================================

    @Test
    void shouldGetAverageRating() {

        // Arrange
        Event event = new Event();
        event.setId(3L);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(3L))
                .thenReturn(4.5);

        // Act
        Double result =
                reviewService.getAverageRating(3L);

        // Assert
        assertEquals(4.5, result);

        verify(reviewRepository)
                .getAverageRating(3L);
    }
}