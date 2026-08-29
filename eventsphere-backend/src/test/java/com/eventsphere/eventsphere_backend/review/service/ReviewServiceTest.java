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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldCreateReviewSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .build();

        Review savedReview =
                Review.builder()
                        .id(10L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .user(user)
                        .event(event)
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(10L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
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
                .thenReturn(savedReview);

        when(reviewMapper.toResponse(savedReview))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.createReview(
                        request,
                        email
                );

        assertNotNull(result);

        assertEquals(10L, result.getId());
        assertEquals(5L, result.getUserId());
        assertEquals(3L, result.getEventId());
        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent workshop!",
                result.getComment()
        );

        ArgumentCaptor<Review> reviewCaptor =
                ArgumentCaptor.forClass(Review.class);

        verify(reviewRepository)
                .save(reviewCaptor.capture());

        Review saved =
                reviewCaptor.getValue();

        assertEquals(5, saved.getRating());
        assertEquals(
                "Excellent workshop!",
                saved.getComment()
        );
        assertEquals(user, saved.getUser());
        assertEquals(event, saved.getEvent());

        verify(userRepository)
                .findByEmail(email);

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

        verify(reviewMapper)
                .toResponse(savedReview);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringCreateReview() {

        String email = "unknown@test.com";

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
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

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                eventRepository,
                bookingRepository,
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistDuringCreateReview() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(99L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.createReview(
                        request,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(eventRepository)
                .findById(99L);

        verifyNoInteractions(
                bookingRepository,
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void shouldThrowExceptionWhenUserHasNoConfirmedBooking() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(4)
                        .comment("Good event")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
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

        verify(bookingRepository)
                .existsByUserAndEventAndBookingStatus(
                        user,
                        event,
                        BookingStatus.CONFIRMED
                );

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void shouldThrowExceptionWhenReviewAlreadyExists() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
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

        verify(reviewRepository)
                .existsByUserAndEvent(user, event);

        verify(
                reviewRepository,
                never()
        ).save(any(Review.class));

        verifyNoInteractions(reviewMapper);
    }


    // =========================================================
    // GET EVENT REVIEWS
    // =========================================================

    @Test
    void shouldGetEventReviewsSuccessfully() {

        Event event = new Event();
        event.setId(3L);

        Review review1 =
                Review.builder()
                        .id(1L)
                        .event(event)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        Review review2 =
                Review.builder()
                        .id(2L)
                        .event(event)
                        .rating(4)
                        .comment("Very good!")
                        .build();

        ReviewResponse response1 =
                ReviewResponse.builder()
                        .id(1L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        ReviewResponse response2 =
                ReviewResponse.builder()
                        .id(2L)
                        .eventId(3L)
                        .rating(4)
                        .comment("Very good!")
                        .build();

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository
                .findByEventOrderByCreatedAtDesc(event))
                .thenReturn(
                        List.of(review1, review2)
                );

        when(reviewMapper.toResponse(review1))
                .thenReturn(response1);

        when(reviewMapper.toResponse(review2))
                .thenReturn(response2);

        List<ReviewResponse> result =
                reviewService.getEventReviews(3L);

        assertNotNull(result);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(eventRepository)
                .findById(3L);

        verify(reviewRepository)
                .findByEventOrderByCreatedAtDesc(event);

        verify(reviewMapper)
                .toResponse(review1);

        verify(reviewMapper)
                .toResponse(review2);
    }


    @Test
    void shouldReturnEmptyListWhenEventHasNoReviews() {

        Event event = new Event();
        event.setId(3L);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository
                .findByEventOrderByCreatedAtDesc(event))
                .thenReturn(List.of());

        List<ReviewResponse> result =
                reviewService.getEventReviews(3L);

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(reviewRepository)
                .findByEventOrderByCreatedAtDesc(event);

        verifyNoInteractions(reviewMapper);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistWhileGettingReviews() {

        when(eventRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getEventReviews(99L)
        );

        verify(eventRepository)
                .findById(99L);

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    // =========================================================
    // GET MY REVIEWS
    // =========================================================

    @Test
    void shouldGetMyReviewsSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Review review =
                Review.builder()
                        .id(10L)
                        .user(user)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(10L)
                        .userId(5L)
                        .rating(5)
                        .comment("Excellent!")
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

        assertNotNull(result);

        assertEquals(1, result.size());

        assertEquals(10L, result.get(0).getId());
        assertEquals(5, result.get(0).getRating());

        verify(userRepository)
                .findByEmail(email);

        verify(reviewRepository)
                .findByUserOrderByCreatedAtDesc(user);

        verify(reviewMapper)
                .toResponse(review);
    }


    @Test
    void shouldReturnEmptyListWhenUserHasNoReviews() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<ReviewResponse> result =
                reviewService.getMyReviews(email);

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(reviewRepository)
                .findByUserOrderByCreatedAtDesc(user);

        verifyNoInteractions(reviewMapper);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistWhileGettingMyReviews() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.getMyReviews(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    // =========================================================
    // GET AVERAGE RATING
    // =========================================================

    @Test
    void shouldGetAverageRatingSuccessfully() {

        Event event = new Event();
        event.setId(3L);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(3L))
                .thenReturn(4.5);

        Double result =
                reviewService.getAverageRating(3L);

        assertNotNull(result);

        assertEquals(
                4.5,
                result
        );

        verify(eventRepository)
                .findById(3L);

        verify(reviewRepository)
                .getAverageRating(3L);
    }


    @Test
    void shouldReturnZeroWhenEventHasNoRatings() {

        Event event = new Event();
        event.setId(3L);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(reviewRepository.getAverageRating(3L))
                .thenReturn(null);

        Double result =
                reviewService.getAverageRating(3L);

        assertNotNull(result);

        assertEquals(
                0.0,
                result
        );

        verify(eventRepository)
                .findById(3L);

        verify(reviewRepository)
                .getAverageRating(3L);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistWhileGettingAverageRating() {

        when(eventRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> reviewService.getAverageRating(99L)
        );

        verify(eventRepository)
                .findById(99L);

        verifyNoInteractions(reviewRepository);
    }


    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    @Test
    void shouldUpdateReviewSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Event event = new Event();
        event.setId(3L);

        Review review =
                Review.builder()
                        .id(10L)
                        .rating(3)
                        .comment("Good")
                        .user(user)
                        .event(event)
                        .build();

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(10L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        when(reviewRepository.save(review))
                .thenReturn(review);

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.updateReview(
                        10L,
                        request,
                        email
                );

        assertNotNull(result);

        assertEquals(5, review.getRating());

        assertEquals(
                "Excellent!",
                review.getComment()
        );

        assertEquals(5, result.getRating());

        assertEquals(
                "Excellent!",
                result.getComment()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(reviewRepository)
                .findById(10L);

        verify(reviewRepository)
                .save(review);

        verify(reviewMapper)
                .toResponse(review);
    }


    @Test
    void shouldThrowExceptionWhenReviewDoesNotExistDuringUpdate() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(99L))
                .thenReturn(Optional.empty());

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        99L,
                        request,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(reviewRepository)
                .findById(99L);

        verify(
                reviewRepository,
                never()
        ).save(any(Review.class));

        verifyNoInteractions(reviewMapper);
    }


    @Test
    void shouldThrowExceptionWhenAnotherUserUpdatesReview() {

        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(5L);
        owner.setEmail(ownerEmail);

        User otherUser = new User();
        otherUser.setId(10L);
        otherUser.setEmail(otherEmail);

        Review review =
                Review.builder()
                        .id(10L)
                        .rating(4)
                        .comment("Good")
                        .user(owner)
                        .build();

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        when(userRepository.findByEmail(otherEmail))
                .thenReturn(Optional.of(otherUser));

        when(reviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.updateReview(
                        10L,
                        request,
                        otherEmail
                )
        );

        verify(userRepository)
                .findByEmail(otherEmail);

        verify(reviewRepository)
                .findById(10L);

        verify(
                reviewRepository,
                never()
        ).save(any(Review.class));

        verifyNoInteractions(reviewMapper);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringUpdate() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        CreateReviewRequest request =
                CreateReviewRequest.builder()
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent!")
                        .build();

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.updateReview(
                        10L,
                        request,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    // =========================================================
    // DELETE REVIEW
    // =========================================================

    @Test
    void shouldDeleteReviewSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Review review =
                Review.builder()
                        .id(10L)
                        .rating(5)
                        .comment("Excellent!")
                        .user(user)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(
                10L,
                email
        );

        verify(userRepository)
                .findByEmail(email);

        verify(reviewRepository)
                .findById(10L);

        verify(reviewRepository)
                .delete(review);
    }


    @Test
    void shouldThrowExceptionWhenReviewDoesNotExistDuringDelete() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(reviewRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(
                        99L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(reviewRepository)
                .findById(99L);

        verify(
                reviewRepository,
                never()
        ).delete(any(Review.class));
    }


    @Test
    void shouldThrowExceptionWhenAnotherUserDeletesReview() {

        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(5L);
        owner.setEmail(ownerEmail);

        User otherUser = new User();
        otherUser.setId(10L);
        otherUser.setEmail(otherEmail);

        Review review =
                Review.builder()
                        .id(10L)
                        .rating(5)
                        .comment("Excellent!")
                        .user(owner)
                        .build();

        when(userRepository.findByEmail(otherEmail))
                .thenReturn(Optional.of(otherUser));

        when(reviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewOwnershipException.class,
                () -> reviewService.deleteReview(
                        10L,
                        otherEmail
                )
        );

        verify(userRepository)
                .findByEmail(otherEmail);

        verify(reviewRepository)
                .findById(10L);

        verify(
                reviewRepository,
                never()
        ).delete(any(Review.class));
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringDelete() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.deleteReview(
                        10L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }
}