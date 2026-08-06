package com.eventsphere.eventsphere_backend.review.service;

import com.eventsphere.eventsphere_backend.common.exception.*;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import com.eventsphere.eventsphere_backend.review.mapper.ReviewMapper;
import com.eventsphere.eventsphere_backend.review.repository.ReviewRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            ReviewMapper reviewMapper) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reviewMapper = reviewMapper;
    }

    // Create Review
    public ReviewResponse createReview(
            CreateReviewRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new EventNotFoundException(request.getEventId()));

        // Prevent duplicate review
        if (reviewRepository.existsByUserAndEvent(user, event)) {
            throw new ReviewAlreadyExistsException(event.getId());
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .event(event)
                .build();

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(savedReview);
    }

    // Get all reviews for an event
    public List<ReviewResponse> getEventReviews(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));

        return reviewRepository
                .findByEventOrderByCreatedAtDesc(event)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // Get my reviews
    public List<ReviewResponse> getMyReviews(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return reviewRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // Get average rating for an event
    public Double getAverageRating(Long eventId) {

        eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));

        Double averageRating =
                reviewRepository.getAverageRating(eventId);

        return averageRating != null
                ? averageRating
                : 0.0;
    }

    // Update Review
    public ReviewResponse updateReview(
            Long reviewId,
            CreateReviewRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId));

        // Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ReviewOwnershipException();
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(updatedReview);
    }

    // Delete Review
    public void deleteReview(
            Long reviewId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId));

        // Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ReviewOwnershipException();
        }

        reviewRepository.delete(review);
    }
}