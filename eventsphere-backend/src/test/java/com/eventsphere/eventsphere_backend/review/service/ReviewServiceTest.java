package com.eventsphere.eventsphere_backend.review.service;

import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewNotAllowedException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.ReviewOwnershipException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import com.eventsphere.eventsphere_backend.review.mapper.ReviewMapper;
import com.eventsphere.eventsphere_backend.review.repository.ReviewRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    private EventRepository eventRepository;
    private BookingRepository bookingRepository;
    private ReviewMapper reviewMapper;

    private ReviewService reviewService;


    @BeforeEach
    void setUp() {

        reviewRepository = mock(ReviewRepository.class);
        userRepository = mock(UserRepository.class);
        eventRepository = mock(EventRepository.class);
        bookingRepository = mock(BookingRepository.class);
        reviewMapper = mock(ReviewMapper.class);

        reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                eventRepository,
                bookingRepository,
                reviewMapper
        );
    }


    // =========================================================
    // CREATE REVIEW
    // =========================================================

    @Test
    void shouldCreateReviewSuccessfully() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);
        request.setRating(5);
        request.setComment("Excellent workshop!");

        User user = mock(User.class);
        Event event = mock(Event.class);
        Review savedReview = mock(Review.class);

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(10L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .build();

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(event.getId())
                .thenReturn(3L);

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

        ReviewResponse result =
                reviewService.createReview(
                        request,
                        "user@test.com"
                );

        assertEquals(5L, result.getId());
        assertEquals(3L, result.getEventId());
        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent workshop!",
                result.getComment()
        );

        verify(userRepository)
                .findByEmail("user@test.com");

        verify(eventRepository)
                .findById(3L);

        verify(bookingRepository)
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                );

        verify(reviewRepository)
                .existsByUserAndEvent(user, event);

        verify(reviewRepository)
                .save(any(Review.class));

        verify(reviewMapper)
                .toResponse(savedReview);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileCreatingReview() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.createReview(
                        request,
                        "unknown@test.com"
                )
        );

        verify(userRepository)
                .findByEmail("unknown@test.com");

        verify(eventRepository, never())
                .findById(any(Long.class));
    }


    @Test
    void shouldThrowExceptionWhenEventNotFoundWhileCreatingReview() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);

        User user = mock(User.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.createReview(
                        request,
                        "user@test.com"
                )
        );

        verify(eventRepository)
                .findById(3L);

        verify(bookingRepository, never())
                .existsByUserAndEventAndBookingStatus(
                        any(User.class),
                        any(Event.class),
                        any(BookingStatus.class)
                );
    }


    @Test
    void shouldThrowExceptionWhenUserHasNoConfirmedBooking() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);

        User user = mock(User.class);
        Event event = mock(Event.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(event.getId())
                .thenReturn(3L);

        when(bookingRepository.existsByUserAndEventAndBookingStatus(
                user,
                event,
                BookingStatus.CONFIRMED
        )).thenReturn(false);

        assertThrows(
                ReviewNotAllowedException.class,
                () -> reviewService.createReview(
                        request,
                        "user@test.com"
                )
        );

        verify(bookingRepository)
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                );

        verify(reviewRepository, never())
                .existsByUserAndEvent(user, event);

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    @Test
    void shouldThrowExceptionWhenReviewAlreadyExists() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);

        User user = mock(User.class);
        Event event = mock(Event.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(event.getId())
                .thenReturn(3L);

        when(bookingRepository.existsByUserAndEventAndBookingStatus(
                user,
                event,
                BookingStatus.CONFIRMED
        )).thenReturn(true);

        when(reviewRepository.existsByUserAndEvent(user, event))
                .thenReturn(true);

        assertThrows(
                ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(
                        request,
                        "user@test.com"
                )
        );

        verify(reviewRepository)
                .existsByUserAndEvent(user, event);

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    // =========================================================
    // GET EVENT REVIEWS
    // =========================================================

    @Test
    void shouldGetEventReviewsSuccessfully() {

        Event event = mock(Event.class);
        Review review = mock(Review.class);

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
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

        List<ReviewResponse> result =
                reviewService.getEventReviews(3L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(5, result.get(0).getRating());
        assertEquals(
                "Excellent workshop!",
                result.get(0).getComment()
        );

        verify(eventRepository)
                .findById(3L);

        verify(reviewRepository)
                .findByEventOrderByCreatedAtDesc(event);

        verify(reviewMapper)
                .toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenEventNotFoundWhileGettingReviews() {

        when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getEventReviews(3L)
        );

        verify(reviewRepository, never())
                .findByEventOrderByCreatedAtDesc(any(Event.class));
    }


    // =========================================================
    // GET MY REVIEWS
    // =========================================================

    @Test
    void shouldGetMyReviewsSuccessfully() {

        User user = mock(User.class);
        Review review = mock(Review.class);

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(10L)
                        .eventId(3L)
                        .rating(4)
                        .comment("Very good workshop!")
                        .build();

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        List<ReviewResponse> result =
                reviewService.getMyReviews("user@test.com");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(4, result.get(0).getRating());

        verify(userRepository)
                .findByEmail("user@test.com");

        verify(reviewRepository)
                .findByUserOrderByCreatedAtDesc(user);

        verify(reviewMapper)
                .toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingMyReviews() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.getMyReviews(
                        "unknown@test.com"
                )
        );

        verify(reviewRepository, never())
                .findByUserOrderByCreatedAtDesc(
                        any(User.class)
                );
    }


    // =========================================================
    // GET AVERAGE RATING
    // =========================================================

    @Test
    void shouldGetAverageRatingSuccessfully() {

        Event event = mock(Event.class);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(3L))
                .thenReturn(4.5);

        Double result =
                reviewService.getAverageRating(3L);

        assertEquals(4.5, result);

        verify(eventRepository)
                .findById(3L);

        verify(reviewRepository)
                .getAverageRating(3L);
    }


    @Test
    void shouldReturnZeroWhenEventHasNoReviews() {

        Event event = mock(Event.class);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(3L))
                .thenReturn(null);

        Double result =
                reviewService.getAverageRating(3L);

        assertEquals(0.0, result);

        verify(reviewRepository)
                .getAverageRating(3L);
    }


    @Test
    void shouldThrowExceptionWhenEventNotFoundWhileGettingAverageRating() {

        when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getAverageRating(3L)
        );

        verify(reviewRepository, never())
                .getAverageRating(3L);
    }


    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    @Test
    void shouldUpdateReviewSuccessfully() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setRating(4);
        request.setComment("Very good workshop!");

        User user = mock(User.class);
        Review review = mock(Review.class);
        Review updatedReview = mock(Review.class);

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(10L)
                        .eventId(3L)
                        .rating(4)
                        .comment("Very good workshop!")
                        .build();

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.of(review));

        when(user.getId())
                .thenReturn(10L);

        when(review.getUser())
                .thenReturn(user);

        when(reviewRepository.save(review))
                .thenReturn(updatedReview);

        when(reviewMapper.toResponse(updatedReview))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.updateReview(
                        5L,
                        request,
                        "user@test.com"
                );

        assertEquals(5L, result.getId());
        assertEquals(4, result.getRating());
        assertEquals(
                "Very good workshop!",
                result.getComment()
        );

        verify(review)
                .setRating(4);

        verify(review)
                .setComment("Very good workshop!");

        verify(reviewRepository)
                .save(review);

        verify(reviewMapper)
                .toResponse(updatedReview);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileUpdatingReview() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.updateReview(
                        5L,
                        request,
                        "unknown@test.com"
                )
        );

        verify(reviewRepository, never())
                .findById(5L);
    }


    @Test
    void shouldThrowExceptionWhenReviewNotFoundWhileUpdatingReview() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        User user = mock(User.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        5L,
                        request,
                        "user@test.com"
                )
        );

        verify(reviewRepository)
                .findById(5L);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnReview() {

        CreateReviewRequest request =
                new CreateReviewRequest();

        User currentUser = mock(User.class);
        User reviewOwner = mock(User.class);
        Review review = mock(Review.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(currentUser));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.of(review));

        when(currentUser.getId())
                .thenReturn(10L);

        when(reviewOwner.getId())
                .thenReturn(20L);

        when(review.getUser())
                .thenReturn(reviewOwner);

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.updateReview(
                        5L,
                        request,
                        "user@test.com"
                )
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    // =========================================================
    // DELETE REVIEW
    // =========================================================

    @Test
    void shouldDeleteReviewSuccessfully() {

        User user = mock(User.class);
        Review review = mock(Review.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.of(review));

        when(user.getId())
                .thenReturn(10L);

        when(review.getUser())
                .thenReturn(user);

        reviewService.deleteReview(
                5L,
                "user@test.com"
        );

        verify(reviewRepository)
                .delete(review);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileDeletingReview() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.deleteReview(
                        5L,
                        "unknown@test.com"
                )
        );

        verify(reviewRepository, never())
                .findById(5L);
    }


    @Test
    void shouldThrowExceptionWhenReviewNotFoundWhileDeletingReview() {

        User user = mock(User.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(
                        5L,
                        "user@test.com"
                )
        );

        verify(reviewRepository)
                .findById(5L);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnReviewWhileDeleting() {

        User currentUser = mock(User.class);
        User reviewOwner = mock(User.class);
        Review review = mock(Review.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(currentUser));

        when(reviewRepository.findById(5L))
                .thenReturn(Optional.of(review));

        when(currentUser.getId())
                .thenReturn(10L);

        when(reviewOwner.getId())
                .thenReturn(20L);

        when(review.getUser())
                .thenReturn(reviewOwner);

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.deleteReview(
                        5L,
                        "user@test.com"
                )
        );

        verify(reviewRepository, never())
                .delete(any(Review.class));
    }
}
