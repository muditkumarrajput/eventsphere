package com.eventsphere.eventsphere_backend.review.controller;

import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Create Review
    @PostMapping
    public ReviewResponse createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        return reviewService.createReview(
                request,
                authentication.getName()
        );
    }

    // Get all reviews for an event
    @GetMapping("/event/{eventId}")
    public List<ReviewResponse> getEventReviews(
            @PathVariable Long eventId) {

        return reviewService.getEventReviews(eventId);
    }

    // Get my reviews
    @GetMapping("/my")
    public List<ReviewResponse> getMyReviews(
            Authentication authentication) {

        return reviewService.getMyReviews(
                authentication.getName()
        );
    }

    // Get average rating for an event
    @GetMapping("/event/{eventId}/average")
    public Double getAverageRating(
            @PathVariable Long eventId) {

        return reviewService.getAverageRating(eventId);
    }

    // Update my review
    @PutMapping("/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        return reviewService.updateReview(
                reviewId,
                request,
                authentication.getName()
        );
    }

    // Delete my review
    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        reviewService.deleteReview(
                reviewId,
                authentication.getName()
        );
    }
}