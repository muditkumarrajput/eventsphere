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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    // CREATE REVIEW
    // =========================================================

    @Test
    void shouldCreateReview() {

        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(8L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Excellent event")
                        .build();

        Review review =
                Review.builder()
                        .id(10L)
                        .user(user)
                        .event(event)
                        .rating(5)
                        .comment("Excellent event")
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(10L)
                        .eventId(8L)
                        .userId(1L)
                        .rating(5)
                        .comment("Excellent event")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(8L))
                .thenReturn(Optional.of(event));

        when(bookingRepository
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                ))
                .thenReturn(true);

        when(reviewRepository.existsByUserAndEvent(
                user,
                event
        )).thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenReturn(review);

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.createReview(
                        request,
                        email
                );

        assertEquals(10L, result.getId());
        assertEquals(8L, result.getEventId());
        assertEquals(1L, result.getUserId());
        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent event",
                result.getComment()
        );

        verify(userRepository).findByEmail(email);
        verify(eventRepository).findById(8L);
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
        verify(reviewMapper).toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringCreate() {

        String email = "unknown@test.com";

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Good")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.createReview(
                        request,
                        email
                )
        );

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(reviewRepository);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistDuringCreate() {

        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(999L)
                        .rating(5)
                        .comment("Good")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.createReview(
                        request,
                        email
                )
        );

        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(reviewRepository);
    }


    @Test
    void shouldRejectReviewWhenUserHasNoConfirmedBooking() {

        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(8L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Good")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(8L))
                .thenReturn(Optional.of(event));

        when(bookingRepository
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                ))
                .thenReturn(false);

        assertThrows(
                ReviewNotAllowedException.class,
                () -> reviewService.createReview(
                        request,
                        email
                )
        );

        verify(reviewRepository, never())
                .existsByUserAndEvent(any(), any());

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    @Test
    void shouldRejectDuplicateReview() {

        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(8L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Good")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(8L))
                .thenReturn(Optional.of(event));

        when(bookingRepository
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                ))
                .thenReturn(true);

        when(reviewRepository.existsByUserAndEvent(
                user,
                event
        )).thenReturn(true);

        assertThrows(
                ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(
                        request,
                        email
                )
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    // =========================================================
    // GET EVENT REVIEWS
    // =========================================================

    @Test
    void shouldGetEventReviews() {

        Long eventId = 8L;

        Event event = new Event();
        event.setId(eventId);

        Review review1 =
                Review.builder()
                        .id(10L)
                        .event(event)
                        .rating(5)
                        .build();

        Review review2 =
                Review.builder()
                        .id(11L)
                        .event(event)
                        .rating(4)
                        .build();

        ReviewResponse response1 =
                ReviewResponse.builder()
                        .id(10L)
                        .eventId(8L)
                        .rating(5)
                        .build();

        ReviewResponse response2 =
                ReviewResponse.builder()
                        .id(11L)
                        .eventId(8L)
                        .rating(4)
                        .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(reviewRepository
                .findByEventOrderByCreatedAtDesc(event))
                .thenReturn(List.of(review1, review2));

        when(reviewMapper.toResponse(review1))
                .thenReturn(response1);

        when(reviewMapper.toResponse(review2))
                .thenReturn(response2);

        List<ReviewResponse> result =
                reviewService.getEventReviews(eventId);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(11L, result.get(1).getId());

        verify(eventRepository).findById(eventId);

        verify(reviewRepository)
                .findByEventOrderByCreatedAtDesc(event);

        verify(reviewMapper).toResponse(review1);
        verify(reviewMapper).toResponse(review2);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistForReviews() {

        Long eventId = 999L;

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getEventReviews(eventId)
        );

        verifyNoInteractions(reviewRepository);
        verifyNoInteractions(reviewMapper);
    }


    // =========================================================
    // GET MY REVIEWS
    // =========================================================

    @Test
    void shouldGetMyReviews() {

        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Review review =
                Review.builder()
                        .id(10L)
                        .user(user)
                        .rating(5)
                        .comment("Excellent")
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(10L)
                        .userId(1L)
                        .rating(5)
                        .comment("Excellent")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        List<ReviewResponse> result =
                reviewService.getMyReviews(email);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(5, result.get(0).getRating());

        verify(userRepository).findByEmail(email);

        verify(reviewRepository)
                .findByUserOrderByCreatedAtDesc(user);

        verify(reviewMapper).toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistForMyReviews() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.getMyReviews(email)
        );

        verifyNoInteractions(reviewRepository);
        verifyNoInteractions(reviewMapper);
    }


    // =========================================================
    // AVERAGE RATING
    // =========================================================

    @Test
    void shouldGetAverageRating() {

        Long eventId = 8L;

        Event event = new Event();
        event.setId(eventId);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(eventId))
                .thenReturn(4.5);

        Double result =
                reviewService.getAverageRating(eventId);

        assertEquals(4.5, result);

        verify(eventRepository).findById(eventId);
        verify(reviewRepository)
                .getAverageRating(eventId);
    }


    @Test
    void shouldReturnZeroWhenEventHasNoReviews() {

        Long eventId = 8L;

        Event event = new Event();
        event.setId(eventId);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(eventId))
                .thenReturn(null);

        Double result =
                reviewService.getAverageRating(eventId);

        assertEquals(0.0, result);

        verify(reviewRepository)
                .getAverageRating(eventId);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistForAverageRating() {

        Long eventId = 999L;

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getAverageRating(eventId)
        );

        verify(reviewRepository, never())
                .getAverageRating(anyLong());
    }


    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    @Test
    void shouldUpdateOwnReview() {

        Long reviewId = 10L;
        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(8L);

        Review review =
                Review.builder()
                        .id(reviewId)
                        .user(user)
                        .event(event)
                        .rating(3)
                        .comment("Average")
                        .build();

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Excellent")
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(reviewId)
                        .rating(5)
                        .comment("Excellent")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.save(review))
                .thenReturn(review);

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.updateReview(
                        reviewId,
                        request,
                        email
                );

        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent",
                result.getComment()
        );

        assertEquals(5, review.getRating());
        assertEquals(
                "Excellent",
                review.getComment()
        );

        verify(reviewRepository).save(review);
        verify(reviewMapper).toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenReviewDoesNotExistDuringUpdate() {

        Long reviewId = 999L;
        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Good")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        request,
                        email
                )
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    @Test
    void shouldRejectUpdatingAnotherUsersReview() {

        Long reviewId = 10L;
        String email = "user2@test.com";

        User owner = new User();
        owner.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Review review =
                Review.builder()
                        .id(reviewId)
                        .user(owner)
                        .rating(4)
                        .build();

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(8L)
                        .rating(5)
                        .comment("Updated")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.updateReview(
                        reviewId,
                        request,
                        email
                )
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }


    // =========================================================
    // DELETE REVIEW
    // =========================================================

    @Test
    void shouldDeleteOwnReview() {

        Long reviewId = 10L;
        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        Review review =
                Review.builder()
                        .id(reviewId)
                        .user(user)
                        .rating(5)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(
                reviewId,
                email
        );

        verify(reviewRepository).delete(review);
    }


    @Test
    void shouldThrowExceptionWhenReviewDoesNotExistDuringDelete() {

        Long reviewId = 999L;
        String email = "user@test.com";

        User user = new User();
        user.setId(1L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(
                        reviewId,
                        email
                )
        );

        verify(reviewRepository, never())
                .delete(any(Review.class));
    }


    @Test
    void shouldRejectDeletingAnotherUsersReview() {

        Long reviewId = 10L;
        String email = "user2@test.com";

        User owner = new User();
        owner.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Review review =
                Review.builder()
                        .id(reviewId)
                        .user(owner)
                        .rating(5)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.deleteReview(
                        reviewId,
                        email
                )
        );

        verify(reviewRepository, never())
                .delete(any(Review.class));
    }
}
